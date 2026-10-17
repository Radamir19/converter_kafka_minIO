package org.example.converterservice.convert;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class TxtConverter implements ConverterStrategy{
    @Override
    public byte[] convert(InputStream input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try(PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf)) {
            byte[] fontBytes = getClass().getResourceAsStream("/font/DejaVuSans.ttf").readAllBytes();
            document.setFont(PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H));
            BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
            String line;
            boolean hasLines = false;
            while((line = reader.readLine()) != null) {
                String text = line.isEmpty() ? "\u00A0" : line.replace("\t", "    ");
                document.add(new Paragraph(text));
                hasLines = true;
            }
            if(!hasLines) {
                document.add(new Paragraph("\u00A0"));
            }
        }
        return outputStream.toByteArray();
    }

    @Override
    public String extension() {
        return "txt";
    }

    @Override
    public String outputExtension() {
        return "pdf";
    }
}
