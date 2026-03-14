package com.sch.capstone.backend.service.nlp;

import java.io.IOException;
import java.io.InputStream;

public interface SttService {
    String transcribe(InputStream in, String mime) throws IOException;
}
