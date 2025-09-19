package com.sample;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;

public class GenerateVoyagePdf {

    // ====== 입력 파일 경로 ======
    static String DATA_PATH  = "voyage_data.isam";
    static String INDEX_PATH = "voyage_index.txt";

    // 한글 출력용 폰트(맑은고딕 권장) - 경로 맞추세요.
    // 예) "C:/Windows/Fonts/malgun.ttf"
    static String TTF_PATH   = "C:/Windows/Fonts/malgun.ttf";

    // ====== 출력 PDF 경로 ======
    static String OUTPUT_PDF = "voyage-report.pdf";

    // ====== ISAM 포맷 ======
    static final int RECORD_SIZE = 32; // bytes
    static final ByteOrder ORDER = ByteOrder.LITTLE_ENDIAN;

    static class Record {
        int dateYmd;
        double lat;
        double lon;
        int legId;          // 0..3
        double legProgress; // km
        int portFrom;       // 0..3
        int portTo;         // 0..3
    }

    static Map<String, Long> loadIndex(String path) throws IOException {
        Map<String, Long> map = new LinkedHashMap<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] p = line.split("\t");
                map.put(p[0], Long.parseLong(p[1]));
            }
        }
        return map;
    }

    static Record readAtOffset(long offset) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(DATA_PATH, "r");
             FileChannel ch = raf.getChannel()) {
            ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE).order(ORDER);
            ch.position(offset);
            int n = ch.read(buf);
            if (n < RECORD_SIZE) throw new EOFException("Incomplete record");
            buf.flip();

            Record r = new Record();
            r.dateYmd     = buf.getInt();
            r.lat         = buf.getDouble();
            r.lon         = buf.getDouble();
            r.legId       = Short.toUnsignedInt(buf.getShort());
            r.legProgress = buf.getDouble();
            r.portFrom    = Byte.toUnsignedInt(buf.get());
            r.portTo      = Byte.toUnsignedInt(buf.get());
            return r;
        }
    }

    static String legName(int id) {
        return switch (id) {
            case 0 -> "KR→JP";
            case 1 -> "JP→US";
            case 2 -> "US→MX";
            case 3 -> "MX→KR";
            default -> "?";
        };
    }

    static String portName(int id) {
        return switch (id) {
            case 0 -> "Busan, KR";
            case 1 -> "Yokohama, JP";
            case 2 -> "Los Angeles, US";
            case 3 -> "Manzanillo, MX";
            default -> "?";
        };
    }

    public static void main(String[] args) throws Exception {
        if (args.length >= 1) DATA_PATH  = args[0];
        if (args.length >= 2) INDEX_PATH = args[1];
        if (args.length >= 3) OUTPUT_PDF = args[2];

        Map<String, Long> index = loadIndex(INDEX_PATH);

        // 샘플로: 특정 날짜(2025-10-01) + 앞 20건 테이블
        String sampleDate = "2025-10-01";
        Long off = index.get(sampleDate);
        if (off == null) throw new IllegalArgumentException("Date not in index: " + sampleDate);
        Record sample = readAtOffset(off);

        List<Record> firstRows = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, Long> e : index.entrySet()) {
            firstRows.add(readAtOffset(e.getValue()));
            if (++count >= 20) break;
        }

        // ====== PDF 생성 ======
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDType0Font font = PDType0Font.load(doc, new File(TTF_PATH));
            float margin = 50f;
            float y = page.getMediaBox().getHeight() - margin;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                // 제목
                cs.beginText();
                cs.setFont(font, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("부산 → 일본 → 미국 → 멕시코 컨테이너선 운항 보고서");
                cs.endText();
                y -= 28;

                // 메타 개요
                cs.beginText();
                cs.setFont(font, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("데이터 파일: " + Paths.get(DATA_PATH).toAbsolutePath().normalize().toString());
                cs.endText();
                y -= 16;

                cs.beginText();
                cs.setFont(font, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("인덱스 파일: " + Paths.get(INDEX_PATH).toAbsolutePath().normalize().toString());
                cs.endText();
                y -= 16;

                cs.beginText();
                cs.setFont(font, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("샘플 날짜: " + sampleDate + "  |  구간: " + legName(sample.legId)
                        + "  |  진행: " + String.format(Locale.US, "%.1f km", sample.legProgress));
                cs.endText();
                y -= 24;

                // 테이블 헤더
                float x = margin;
                float[] colW = {78, 80, 80, 46, 70, 70, 70};
                String[] head = {"Date", "Latitude", "Longitude", "Leg", "Progress(km)", "From", "To"};

                drawRow(cs, font, 11, x, y, colW, head, true);
                y -= 18;

                // 테이블 바디
                for (Record r : firstRows) {
                    String dateIso = ymdToIso(r.dateYmd);
                    String[] row = {
                        dateIso,
                        String.format(Locale.US, "%.6f", r.lat),
                        String.format(Locale.US, "%.6f", r.lon),
                        legName(r.legId),
                        String.format(Locale.US, "%.1f", r.legProgress),
                        portName(r.portFrom),
                        portName(r.portTo)
                    };
                    drawRow(cs, font, 10, x, y, colW, row, false);
                    y -= 16;
                    if (y < 80) break; // 간단히 1페이지 제한
                }
            }

            doc.save(OUTPUT_PDF);
        }

        System.out.println("PDF 생성 완료: " + Paths.get(OUTPUT_PDF).toAbsolutePath());
    }

    static void drawRow(PDPageContentStream cs, PDType0Font font, int fs,
                        float x, float y, float[] colW, String[] cells, boolean boldBorder) throws IOException {
        float cx = x;
        // 텍스트
        cs.beginText();
        cs.setFont(font, fs);
        cs.newLineAtOffset(cx + 2, y);
        for (int i = 0; i < cells.length; i++) {
            cs.showText(cells[i]);
            cs.endText();

            // 다음 셀 시작
            cx += colW[i];
            if (i < cells.length - 1) {
                // 셀 경계선
                cs.moveTo(cx, y - 2);
                cs.lineTo(cx, y + 12);
                cs.stroke();
            }
            if (i < cells.length - 1) {
                cs.beginText();
                cs.setFont(font, fs);
                cs.newLineAtOffset(cx + 2, y);
            }
        }

        // 행 하단 선
        cs.moveTo(x, y - 3);
        cs.lineTo(x + sum(colW), y - 3);
        cs.stroke();
        if (boldBorder) {
            cs.moveTo(x, y + 14);
            cs.lineTo(x + sum(colW), y + 14);
            cs.stroke();
        }
    }

    static float sum(float[] a) { float s=0; for (float v : a) s+=v; return s; }

    static String ymdToIso(int ymd) {
        int y = ymd / 10000;
        int m = (ymd / 100) % 100;
        int d = ymd % 100;
        return LocalDate.of(y, m, d).toString();
    }
}
