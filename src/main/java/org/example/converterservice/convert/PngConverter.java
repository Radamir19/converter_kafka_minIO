package org.example.converterservice.convert;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class PngConverter implements ConverterStrategy{
    @Override
    public byte[] convert(InputStream input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try(PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf)) {
            Image image = new Image(ImageDataFactory.create(input.readAllBytes()));
            float maxW = PageSize.A4.getWidth() - document.getLeftMargin() - document.getRightMargin();
            float maxH = PageSize.A4.getHeight() - document.getTopMargin() - document.getBottomMargin();
            if (image.getImageWidth() > maxW || image.getImageHeight() > maxH) {
                image.setAutoScale(true);
            }
            document.add(image);
        }
        return outputStream.toByteArray();
    }

    @Override
    public String extension() {
        return "png";
    }

    @Override
    public String outputExtension() {
        return "pdf";
    }
}
