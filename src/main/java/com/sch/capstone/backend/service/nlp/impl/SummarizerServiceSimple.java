package com.sch.capstone.backend.service.nlp.impl;

import com.sch.capstone.backend.service.nlp.SummarizerService;
import org.springframework.stereotype.Service;

// 임시 요약 구현체(텍스트에서 앞의 최대 3문장을 잘라서 요약처럼 더미 데이터 출력)
@Service
public class SummarizerServiceSimple implements SummarizerService {
    @Override
    public String summarize(String text) {
        // 입력이 없거나 공백만 있으면 요약 불가
        if (text == null || text.isBlank()) return "[요약 불가: 내용 없음]";

        // 줄바꿈을 공백으로 치환 후, 문장 단위로 분리
        String[] parts = text.replace("\r"," ").replace("\n"," ").split("(?<=[.!?])\\s+");

        // 앞 3문장까지만 사용
        int take = Math.min(3, parts.length);

        // 최종 요약 문자열 생성
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < take; i++) sb.append(parts[i]).append(" ");

        return sb.toString().trim();
    }
}
