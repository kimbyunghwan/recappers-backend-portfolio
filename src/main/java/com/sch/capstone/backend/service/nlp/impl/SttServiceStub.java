package com.sch.capstone.backend.service.nlp.impl;

import com.sch.capstone.backend.service.nlp.SttService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
@Profile("stt-ok")
public class SttServiceStub implements SttService {

    @Override
    public String transcribe(InputStream in, String mime) throws IOException {
        return "STT 결과";
    }
}
