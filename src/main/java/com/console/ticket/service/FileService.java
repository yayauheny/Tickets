package com.console.ticket.service;


import com.console.ticket.constants.Constants;
import com.console.ticket.exception.FileException;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileService {
    private static String ticketInputLog;
    private static Path backgroundPdf;

    public static void writeReceipt(String receipt) throws FileException {
        File outputFile = new File((String.format("tickets/ticket%s.pdf", Constants.CASHIER_NUMBER)));
        try {
            buildPdfFile(outputFile, receipt);
        } catch (FileException e) {
            throw new FileException("Exception while writing to file: ", e);
        }
    }

    public static void readReceipt(String inputPath) throws FileException {
        try {
            ticketInputLog = Files.readString(Path.of(inputPath), StandardCharsets.UTF_8);
            System.out.println(ticketInputLog);
        } catch (IOException e) {
            throw new FileException("Exception while reading a file: ", e);
        }
    }


    private static File buildPdfFile(File file, String text) {
        backgroundPdf = Path.of("src/main/resources/Clevertec_Template.pdf");
        Paragraph paragraph;
        Document document = new Document();
        try {
            PdfWriter writer = PdfWriter.getInstance(document, Files.newOutputStream(file.toPath()));
            PdfReader reader = new PdfReader(backgroundPdf.toString());

            document.open();
            PdfImportedPage backgroundPage = writer.getImportedPage(reader, 1);
            PdfContentByte contentByte = writer.getDirectContent();
            contentByte.addTemplate(backgroundPage, 0, 0);

            Font font = new Font(Font.FontFamily.HELVETICA, 18);
            paragraph = new Paragraph();
            paragraph.setFont(font);
            paragraph.setLeading(24);
            paragraph.setAlignment(Element.ALIGN_CENTER);
            paragraph.add("\n".repeat(10));
            paragraph.add(text);
            document.add(paragraph);

            document.close();
            writer.close();
        } catch (IOException | DocumentException e) {
            throw new FileException("Exception parse file to pdf: ", e);
        }

        return null;
    }
}
