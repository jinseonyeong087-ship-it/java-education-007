package com.sample;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

public class PdfBoxExample {
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = formatter.format(now);
        String message = "환영합니다! 현재 시각은 " + dateTime;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // ✅ 한글 지원 폰트 로드 (맑은 고딕)
            File fontFile = new File("C:/Windows/Fonts/malgun.ttf");
            PDType0Font font = PDType0Font.load(document, fontFile);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 18); // ← 한글 폰트 사용
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText(message);
                contentStream.endText();
            }

            document.save(new File("welcome.pdf"));
            System.out.println("PDF 생성 완료: welcome.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
