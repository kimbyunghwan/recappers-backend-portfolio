package com.sch.capstone.backend.dto.question;

import com.sch.capstone.backend.enums.QuestionType;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class QuestionDTO {
    private QuestionType questionType;
    private String questionText;
    private String answer;
    private List<String> options;
}
