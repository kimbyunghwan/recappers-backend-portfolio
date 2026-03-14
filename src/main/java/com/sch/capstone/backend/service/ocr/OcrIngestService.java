package com.sch.capstone.backend.service.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sch.capstone.backend.config.StorageProps;
import com.sch.capstone.backend.dto.ocr.OcrResponseDTO;
import com.sch.capstone.backend.entity.ExtractedTextMeta;
import com.sch.capstone.backend.entity.Upload;
import com.sch.capstone.backend.enums.ProcessingStage;
import com.sch.capstone.backend.repository.jpa.ExtractedTextMetaRepository;
import com.sch.capstone.backend.repository.jpa.UploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrIngestService {

    private final StorageProps storageProps;
    private final ExtractedTextMetaRepository metaRepository;
    private final UploadRepository uploadRepository;

//    @Transactional
//    public void save(Long uploadId, OcrResponseDTO dto) {
//        // 1) 업로드 조회
//        var upload = uploadRepository.findById(uploadId)
//                .orElseThrow(() -> new IllegalArgumentException("upload not found: " + uploadId));
//
//        // (로컬만 처리)
//        if (!"local".equalsIgnoreCase(storageProps.getType())) {
//            throw new IllegalStateException("Only local storage path handled here");
//        }
//
//        // 2) 저장 디렉터리: ./capstone-storage/<prefix.extracted>/<uploadId>/
//        Path dir = Paths.get(
//                storageProps.getLocal().getBaseDir(),
//                storageProps.getPrefix().getExtracted(),
//                String.valueOf(uploadId)
//        );
//
//        try {
//            Files.createDirectories(dir);
//            String ts = String.valueOf(System.currentTimeMillis());
//
//            // 3) 원본 JSON 저장 (보존용)
//            ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//            String jsonString = om.writeValueAsString(dto);
//            String jsonName = "ocr-" + ts + ".json";
//            Path jsonPath = dir.resolve(jsonName);
//            Files.writeString(jsonPath, jsonString, StandardCharsets.UTF_8);
//
//            // 4) 텍스트 생성 (all_text 우선, 없으면 pages[].text 이어붙임)
//            String raw = dto.getAll_text();
//            if (raw == null || raw.isBlank()) {
//                raw = (dto.getPages() == null) ? "" :
//                        dto.getPages().stream()
//                                .map(p -> p.getText() == null ? "" : p.getText())
//                                .reduce((a, b) -> a + "\n\n--- Page Break ---\n\n" + b)
//                                .orElse("");
//            }
//
//            // 5) 텍스트 클린업
//            String cleaned = cleanOcrText(raw);
//
//            // 6) TXT 저장
//            String txtName = "ocr-" + ts + ".txt";
//            Path txtPath = dir.resolve(txtName);
//            Files.writeString(txtPath, cleaned, StandardCharsets.UTF_8);
//
//            // 7) DB에 저장할 상대 경로 키들
//            String relTxtKey = Paths.get(
//                    storageProps.getPrefix().getExtracted(),
//                    String.valueOf(uploadId),
//                    txtName
//            ).toString();
//
//            String relJsonKey = Paths.get(
//                    storageProps.getPrefix().getExtracted(),
//                    String.valueOf(uploadId),
//                    jsonName
//            ).toString();
//
//            // 8) 메타 저장 (jsonKey 컬럼이 있으면 주석 해제)
//            ExtractedTextMeta meta = ExtractedTextMeta.builder()
//                    .upload(upload)
//                    .storageKey(relTxtKey)
//                    //.jsonKey(relJsonKey) // ← 엔티티/테이블에 컬럼이 있으면 사용
//                    .charCount(cleaned.length())
//                    .pageCount(dto.getTotal_pages())
//                    .build();
//            metaRepository.save(meta);
//
//            // 9) 단계 갱신
//            upload.setStage(ProcessingStage.OCR_DONE);
//
//        } catch (IOException e) {
//            upload.setStage(ProcessingStage.OCR_FAILED);
//            throw new UncheckedIOException(e);
//        }
//    }


//    /** OCR 텍스트 정리 */
//    private String cleanOcrText(String s) {
//        if (s == null) return "";
//        s = Normalizer.normalize(s, Normalizer.Form.NFKC); // 전각/호환 정규화
//        s = s.replace("\r\n", "\n");// CRLF -> LF
//        s = s.replaceAll("(?<=\\p{Alnum})-\\n(?=\\p{Alnum})", ""); // 하이픈 줄바꿈 연결
//        s = s.replaceAll("(?<![.!?…:;])\\n(?=\\p{L})", " "); // 문장 중간 줄바꿈을 공백으로
//        s = s.replaceAll("[ \\t]+\\n", "\n"); // 줄 끝 공백 제거
//        s = s.replaceAll("\\n{3,}", "\n\n"); // 빈 줄 정리
//        s = s.replaceAll("([=~^_\\-–—><]{3,})", "$1".substring(0,1));
//        return s.trim();
//    }

    @Transactional
    public void save(Long uploadId, OcrResponseDTO dto) {
        // 1) 업로드 조회
        Upload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("upload not found: " + uploadId));

        // (로컬만 처리; S3 등은 분기)
        if (!"local".equalsIgnoreCase(storageProps.getType())) {
            throw new IllegalStateException("Only local storage path handled here");
        }

        // 2) 경로 계산 (resolve만 사용)
        Path baseDir = Paths.get(storageProps.getLocal().getBaseDir());          // ./capstone-storage
        String extractedPrefix = storageProps.getPrefix().getExtracted();        // texts/extracted  (앞뒤 / 금지)
        Path dir = baseDir.resolve(extractedPrefix).resolve(String.valueOf(uploadId));

        try {
            Files.createDirectories(dir);
            String ts = String.valueOf(System.currentTimeMillis());

            // 3) 원본 JSON 저장 (보존용)
            ObjectMapper om = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            Path jsonPath = dir.resolve("ocr-" + ts + ".json");
            Files.writeString(jsonPath, om.writeValueAsString(dto), StandardCharsets.UTF_8);

            // 4) 텍스트 소스 생성 (all_text 우선, 없으면 pages[].text 이어붙임)
            String raw = dto.getAll_text();
            if (raw == null || raw.isBlank()) {
                raw = (dto.getPages() == null) ? "" :
                        dto.getPages().stream()
                                .map(p -> p.getText() == null ? "" : p.getText())
                                .reduce((a, b) -> a + "\n\n--- Page Break ---\n\n" + b)
                                .orElse("");
            }

            // 5) 텍스트 클린업
            String cleaned = cleanOcrText(raw);

            // 6) TXT 저장
            Path txtPath = dir.resolve("ocr-" + ts + ".txt");
            Files.writeString(txtPath, cleaned, StandardCharsets.UTF_8);

            // 7) DB에 저장할 상대 키 (항상 forward slash 로)
            String relBase = extractedPrefix.replace('\\', '/'); // texts/extracted
            String relTxtKey  = relBase + "/" + uploadId + "/ocr-" + ts + ".txt";
            String relJsonKey = relBase + "/" + uploadId + "/ocr-" + ts + ".json";

            // 경로 로그(디버깅용)
            log.info("[OCR-SAVE] dir={}, txtPath={}, jsonPath={}, relTxtKey={}, relJsonKey={}",
                    dir.toAbsolutePath(), txtPath.toAbsolutePath(), jsonPath.toAbsolutePath(),
                    relTxtKey, relJsonKey);

            // 8) 메타 저장 (jsonKey 컬럼이 있으면 주석 해제)
            ExtractedTextMeta meta = ExtractedTextMeta.builder()
                    .upload(upload)
                    .storageKey(relTxtKey)
                    // .jsonKey(relJsonKey)
                    .charCount(cleaned.length())
                    .pageCount(dto.getTotal_pages())
                    .build();
            metaRepository.save(meta);

            // 9) 단계 갱신
            upload.setStage(ProcessingStage.OCR_DONE);

        } catch (IOException e) {
            upload.setStage(ProcessingStage.OCR_FAILED);
            throw new UncheckedIOException(e);
        }
    }

    /** OCR 텍스트 정리 (기호 노이즈 라인 제거 + 문장/개행 정리) */
    private String cleanOcrText(String s) {
        if (s == null) return "";

        // 1) 정규화 + 제어문자 정리
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC)
                .replace("\r\n", "\n")
                .replaceAll("\\p{Cntrl}&&[^\n\t]", ""); // 개행/탭 제외 제어문자 제거

        // 2) 단어 하이픈 줄바꿈 제거: exam-\nple -> example
        s = s.replaceAll("(?<=\\p{Alnum})-\\n(?=\\p{Alnum})", "");

        // 3) 문장 중간 강제 줄바꿈을 공백으로(문장부호 없이 끊긴 줄)
        s = s.replaceAll("(?<![.!?…:;])\\n(?=\\p{L})", " ");

        // 4) “같은 기호가 3개 이상 연속” → “1개로 축약”
        s = s.replaceAll("([=~^_\\-–—><])\\1{2,}", "$1");

        // 5) 라인 단위 필터링
        String[] lines = s.split("\\n", -1);
        StringBuilder out = new StringBuilder(s.length());
        String prevTrim = null;
        int blankRun = 0;

        for (String rawLine : lines) {
            String line = rawLine.replaceAll("[ \\t]+$", ""); // 줄 끝 공백 제거
            String trimmed = line.trim();

            if (trimmed.isEmpty()) {
                // 빈 줄 2개까지만 유지
                if (++blankRun <= 2) out.append('\n');
                prevTrim = "";
                continue;
            }
            blankRun = 0;

            // 불릿/번호 목록은 유지
            boolean bullet = trimmed.matches("^[•◦▪·*-]\\s+.*")
                    || trimmed.matches("^\\d+([.)]|\\))\\s+.*");

            // 수식(문자+연산기호 혼합)은 유지
            boolean mathy = trimmed.matches(".*[=+\\-×÷∑∫√≈≠≤≥<>].*") && trimmed.matches(".*[\\p{L}\\p{N}].*");

            // 기호 위주 노이즈 라인 제거
            if (!bullet && !mathy && looksLikeSymbolNoise(trimmed)) {
                continue;
            }

            // 중복 라인 제거(바로 직전과 동일하면 스킵)
            if (trimmed.equals(prevTrim)) continue;

            out.append(line).append('\n');
            prevTrim = trimmed;
        }

        String result = out.toString();

        // 6) 과다 공백 줄 최종 축약
        result = result.replaceAll("\\n{3,}", "\n\n");

        return result.trim();
    }

    /** 기호 노이즈 판별: 문자(한글/영문/숫자)가 거의 없고 특수기호 비율이 높은 짧은 줄 */
    private boolean looksLikeSymbolNoise(String line) {
        int total = 0, letters = 0, digits = 0, symbols = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c)) continue;
            total++;
            if (Character.isLetter(c) || isHangul(c)) letters++;
            else if (Character.isDigit(c)) digits++;
            else symbols++;
        }
        if (total == 0) return true;

        // 아주 짧고(<=3) 글자/숫자 전혀 없으면 노이즈
        if (total <= 3 && (letters + digits) == 0) return true;

        double symbolRatio = (double) symbols / total;
        // 기호 비율이 높고(>=0.6) 글자 수가 적으면 노이즈로 판단
        return symbolRatio >= 0.6 && (letters + digits) <= 2;
    }

    private boolean isHangul(char c) {
        Character.UnicodeBlock b = Character.UnicodeBlock.of(c);
        return b == Character.UnicodeBlock.HANGUL_SYLLABLES
                || b == Character.UnicodeBlock.HANGUL_JAMO
                || b == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
                || b == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_A
                || b == Character.UnicodeBlock.HANGUL_JAMO_EXTENDED_B;
    }
}