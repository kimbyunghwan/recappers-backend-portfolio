package com.sch.capstone.backend.service.nlp.impl;

import com.sch.capstone.backend.service.nlp.PdfService;
import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

// PDF -> 텍스트 추출하는 구현체
@Service
public class PdfServicePdfBox implements PdfService {
    @Override
    public String extractText(InputStream in) throws IOException {
        // PDF 파일을 로드하고 텍스트 추출
        try (PDDocument doc = PDDocument.load(in)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            //null이면 빈 문자열, 아니면 앞뒤 공백 제거
            return text == null ? "" : text.trim();
        }
    }
}
