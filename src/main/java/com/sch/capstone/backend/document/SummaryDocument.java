package com.sch.capstone.backend.document;

import com.sch.capstone.backend.dto.question.QuestionDTO;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
// MongoDB "summaries" 컬렉션과 매핑되는 클래스
@Document(collection = "summaries")
public class SummaryDocument {

    @Id private String id;
    private Long uploadId;
    private Long sttResultId;
    private String summaryText;
    private String modelUsed;
    private List<QuestionDTO> questions;
    private Instant createdAt;
}