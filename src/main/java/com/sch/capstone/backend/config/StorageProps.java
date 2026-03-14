package com.sch.capstone.backend.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// 파일 저장 관련 설정값을 관리하는 클래스

@Component
@ConfigurationProperties(prefix = "app.storage")
@Data
public class StorageProps {
    // 저장소 타입
    private String type; // local, s3 ...

    // 로컬 저장소 관련 설정
    private Local local = new Local();

    @Data
    public static class Local {
        // 로컬 저장소의 루트 경로
        private String baseDir; 
    }

    // 파일 저장 경로에 사용할 prefix
    private Prefix prefix = new Prefix();

    @Data
    public static class Prefix {
        // 추출된 텍스트 저장 경로
        private String extracted; // texts/extracted
        
        // STT 결과 저장 경로
        private String stt;       // texts/stt
        
        // 업로드 원본 파일 저장 경로
        private String uploads;
    }
}
