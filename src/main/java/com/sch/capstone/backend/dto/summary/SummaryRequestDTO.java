package com.sch.capstone.backend.dto.summary;

import com.sch.capstone.backend.dto.question.QuestionDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SummaryRequestDTO {
    private Long sttResultId;
    private String summaryText;
    private String modelUsed;
    private List<QuestionDTO> questions;
}
