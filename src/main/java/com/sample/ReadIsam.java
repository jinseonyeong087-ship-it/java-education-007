package com.sample;

// ReadIsam.java
import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class ReadIsam {

    static final String DATA_PATH = "voyage_data.isam";
    static final String INDEX_PATH = "voyage_index.txt";
    static final int RECORD_SIZE = 32;

    static Map<String, Long> loadIndex() throws IOException {
        Map<String, Long> map = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(INDEX_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split("\t");
                map.put(parts[0], Long.parseLong(parts[1]));
            }
        }
        return map;
    }

    static class Record {
        int dateYmd;
        double lat;
        double lon;
        int legId;          // unsigned short
        double legProgress;
        int portFrom;       // unsigned byte
        int portTo;         // unsigned byte

        @Override
        public String toString() {
            return String.format(Locale.US,
                "Record{date=%d, lat=%.6f, lon=%.6f, legId=%d, legProgress=%.1f, from=%d, to=%d}",
                dateYmd, lat, lon, legId, legProgress, portFrom, portTo);
        }
    }

    static Record readAtOffset(long offset) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(DATA_PATH, "r");
             FileChannel ch = raf.getChannel()) {

            ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            ch.position(offset);
            int n = ch.read(buf);
            if (n < RECORD_SIZE) throw new EOFException("Incomplete record");
            buf.flip();

            Record r = new Record();
            r.dateYmd = buf.getInt();
            r.lat = buf.getDouble();
            r.lon = buf.getDouble();
            int legUnsignedShort = Short.toUnsignedInt(buf.getShort());
            r.legId = legUnsignedShort;
            r.legProgress = buf.getDouble();
            r.portFrom = Byte.toUnsignedInt(buf.get());
            r.portTo = Byte.toUnsignedInt(buf.get());
            return r;
        }
    }

    static List<Record> scanFirstN(int limit) throws IOException {
        List<Record> out = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(DATA_PATH, "r");
             FileChannel ch = raf.getChannel()) {
            for (int i = 0; i < limit; i++) {
                ByteBuffer buf = ByteBuffer.allocate(RECORD_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                int n = ch.read(buf);
                if (n < RECORD_SIZE) break;
                buf.flip();

                Record r = new Record();
                r.dateYmd = buf.getInt();
                r.lat = buf.getDouble();
                r.lon = buf.getDouble();
                r.legId = Short.toUnsignedInt(buf.getShort());
                r.legProgress = buf.getDouble();
                r.portFrom = Byte.toUnsignedInt(buf.get());
                r.portTo = Byte.toUnsignedInt(buf.get());
                out.add(r);
            }
        }
        return out;
    }

    public static void main(String[] args) throws Exception {
        Map<String, Long> idx = loadIndex();

        // 예: 2025-10-01 레코드 읽기
        long offset = Optional.ofNullable(idx.get("2025-10-01"))
                .orElseThrow(() -> new IllegalArgumentException("Date not in index"));
        Record r = readAtOffset(offset);
        System.out.println("Record @ 2025-10-01: " + r);

        // 예: 앞에서 5개 스캔
        System.out.println("\nFirst 5 records:");
        for (Record x : scanFirstN(5)) {
            System.out.println(x);
        }
    }
}
