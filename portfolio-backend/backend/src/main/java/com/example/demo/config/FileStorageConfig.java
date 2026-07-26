package com.example.demo.config;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.SaveFormat;

@Configuration
public class FileStorageConfig implements ApplicationRunner {

    @Value("${app.files-directory}")
    private String filesDirectory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path filesPath = Paths.get(filesDirectory).toAbsolutePath().normalize();
        Files.createDirectories(filesPath);

        Path sampleTarget = filesPath.resolve("Sample.xlsx");
        if (!Files.exists(sampleTarget)) {
            ClassPathResource sample = new ClassPathResource("files/Sample.xlsx");
            try (InputStream in = sample.getInputStream()) {
                Files.copy(in, sampleTarget, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Path sampleDocx = filesPath.resolve("Sample.docx");
        if (!Files.exists(sampleDocx)) {
            Document doc = new Document();
            DocumentBuilder builder = new DocumentBuilder(doc);
            builder.getParagraphFormat().setSpaceAfter(12);
            builder.getFont().setSize(28);
            builder.getFont().setBold(true);
            builder.writeln("Sample Document");
            builder.getFont().setSize(12);
            builder.getFont().setBold(false);
            builder.writeln(
                    "This Word file was generated with Aspose.Words and converted to HTML for editing in Jodit.");
            builder.writeln("Upload your own .docx files from the Docx Viewer toolbar to open them here.");
            doc.save(sampleDocx.toString(), SaveFormat.DOCX);
        }
    }

    public Path getFilesDirectory() {
        return Paths.get(filesDirectory).toAbsolutePath().normalize();
    }
}
