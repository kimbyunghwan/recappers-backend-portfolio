package com.sch.capstone.backend.service.nlp;

import java.io.IOException;
import java.io.InputStream;

public interface PdfService {
    String extractText(InputStream in) throws IOException; }