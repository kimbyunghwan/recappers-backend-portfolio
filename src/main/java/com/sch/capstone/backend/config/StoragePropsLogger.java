package com.sch.capstone.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"local","dev"})
@RequiredArgsConstructor
public class StoragePropsLogger {
    // properties에 정의된 저장소 설정을 주입받음
    private final StorageProps props;

    // 스프링 빈 초기화 후 실행되는 메서드
    @PostConstruct
    void logProps() {
        // 설정된 저장소 타입과 경로 정보를 로그에 출력
        // 로컬(local), 개발(dev) 환경에서만 동작하도록 Profile 제한
        log.info("Storage type={}, baseDir={}, prefixes [extracted={}, stt={}, uploads={}]",
                props.getType(), // 저장소 타입
                props.getLocal().getBaseDir(), // 로컬 저장소 경로
                props.getPrefix().getExtracted(), // 추출 텍스트 저장 prefix
                props.getPrefix().getStt(), // STT 결과 저장 prefix
                props.getPrefix().getUploads()); // 업로드 원본 파일 저장 prefix
    }
}