package com.sch.capstone.backend.dto.nlp;

import com.sch.capstone.backend.dto.question.QuestionViewDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NlpResultDTO {
    private Long uploadId; // 어떤 업로드 파일에 대한 결과인지
    private String stage; // 현재 단계 (예: STT, SUMMARY, QA)
    private List<String> stt; // 음성 인식된 문장 리스트
    private String summary; // 요약 결과
    private List<String> questions;// 생성된 질문 리스트
    private List<QuestionViewDTO> qa; // 문항+정답+타입
}