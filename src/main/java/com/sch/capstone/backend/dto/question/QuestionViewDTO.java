package com.sch.capstone.backend.dto.question;

import com.sch.capstone.backend.enums.QuestionType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionViewDTO {
    private String questionText;
    private String answer;
    private QuestionType questionType;
    private List<String> options;
}
