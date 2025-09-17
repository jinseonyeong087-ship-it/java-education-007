package com.sample;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.io.File;

public class MultiLinePdf {
    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // 폰트 로드 (맑은 고딕)
            PDType0Font font = PDType0Font.load(document, new File("C:/Windows/Fonts/malgun.ttf"));

            String[] lines = {
                "PDFBox 여러 줄 예제",
                "한글도 정상 출력됩니다.",
                "페이지 중앙에 표시!"
            };

            float fontSize = 20;
            float pageWidth = page.getMediaBox().getWidth();
            float startY = page.getMediaBox().getHeight() - 150;

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                for (String line : lines) {
                    float stringWidth = font.getStringWidth(line) / 1000 * fontSize;
                    float startX = (pageWidth - stringWidth) / 2;

                    cs.beginText();
                    cs.setFont(font, fontSize);
                    cs.newLineAtOffset(startX, startY);
                    cs.showText(line);
                    cs.endText();

                    startY -= fontSize + 10; // 줄 간격
                }
            }

            document.save("multi-line.pdf");
            System.out.println("multi-line.pdf 생성 완료!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
