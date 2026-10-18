package org.example.converterservice.service.convert;

import lombok.extern.slf4j.Slf4j;
import org.example.converterservice.service.util.FileNames;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


@Component
@Slf4j
public class ZipConverter implements ConverterStrategy{

    private final Map<String, ConverterStrategy> converters = new HashMap<>();

    public ZipConverter(List<ConverterStrategy> all) {
        for(ConverterStrategy s : all) {
            converters.put(s.extension(), s);
        }
    }
    @Override
    public byte[] convert(InputStream input) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipInputStream zipInput = new ZipInputStream(input);
        int count = 0;
        try(ZipOutputStream zipOut = new ZipOutputStream(out)) {
            ZipEntry entry;
            while((entry = zipInput.getNextEntry()) != null) {
                String name = entry.getName();
                if(entry.isDirectory() || isJunk(name)) {
                    continue;
                }
                ConverterStrategy converter = converters.get(FileNames.extensionOf(name));
                if(converter == null) {
                    log.warn("Пропускаю {}: формат не поддерживается",name);
                    continue;
                }
                try {
                    byte[] pdf = converter.convert(zipInput);
                    zipOut.putNextEntry(new ZipEntry(toPdfName(name)));
                    zipOut.write(pdf);
                    zipOut.closeEntry();
                    count++;
                } catch (Exception e) {
                    log.warn("Не удалось сконвертировать {}: {}", name, e.getMessage());
                    throw new RuntimeException("Не удалось сконвертировать " + name);
                }
            }
            if(count == 0) {
                throw new IllegalArgumentException("В архиве нет файлов, которые можно конвертировать");
            }
        }
        return out.toByteArray();
    }

    @Override
    public String extension() {
        return "zip";
    }

    @Override
    public String outputExtension() {
        return "zip";
    }

    @Override
    public String outputContentType() {
        return "application/zip";
    }
    private String toPdfName(String name) {
        int dot = name.lastIndexOf('.');
        int slash = name.lastIndexOf('/');
        return (dot > slash ? name.substring(0, dot) : name) + ".pdf";
    }

    private boolean isJunk(String name) {
        String fileName = name.substring(name.lastIndexOf('/') + 1);
        return name.startsWith("__MACOSX/") || fileName.startsWith("._") || fileName.equals(".DS_Store");
    }
}
