package com.sample;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import java.awt.Color;
import java.io.File;

public class ShapePdf {
    public static void main(String[] args) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, new File("C:/Windows/Fonts/malgun.ttf"));

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                // 배경 사각형
                cs.setNonStrokingColor(Color.LIGHT_GRAY);
                cs.addRect(50, 600, 500, 100);
                cs.fill();

                // 테두리 사각형
                cs.setStrokingColor(Color.DARK_GRAY);
                cs.addRect(50, 600, 500, 100);
                cs.stroke();

                // 텍스트
                cs.beginText();
                cs.setNonStrokingColor(Color.BLACK);
                cs.setFont(font, 18);
                cs.newLineAtOffset(60, 640);
                cs.showText("이 사각형 안에 들어있는 텍스트입니다.");
                cs.endText();
            }

            document.save("shape-demo.pdf");
            System.out.println("shape-demo.pdf 생성 완료!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
