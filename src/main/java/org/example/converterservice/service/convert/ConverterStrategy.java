package org.example.converterservice.service.convert;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ConverterStrategy {
    byte[] convert(InputStream file) throws IOException;
    String extension();

    String outputExtension();

    default String outputContentType() {
        return "application/pdf";
    }
}
