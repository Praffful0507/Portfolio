package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.aspose.words.CssStyleSheetType;
import com.aspose.words.Document;
import com.aspose.words.HtmlSaveOptions;
import com.aspose.words.SaveFormat;

@RestController
@RequestMapping("/api/docs")
public class DocxController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".docx", ".doc", ".rtf", ".odt", ".dotx", ".dot");
    private static final Pattern BODY_PATTERN = Pattern.compile("(?is)<body[^>]*>(.*)</body>");

    @Value("${app.files-directory}")
    private String filesDirectory;

    @GetMapping
    public List<Map<String, Object>> listFiles() throws IOException {
        Path dir = resolveFilesDir();
        if (!Files.exists(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> isAllowed(p.getFileName().toString()))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)))
                    .map(p -> {
                        try {
                            return Map.<String, Object>of(
                                    "name", p.getFileName().toString(),
                                    "size", Files.size(p));
                        } catch (IOException e) {
                            return Map.<String, Object>of(
                                    "name", p.getFileName().toString(),
                                    "size", 0L);
                        }
                    })
                    .collect(Collectors.toList());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file)
            throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String original = file.getOriginalFilename();
        if (original == null || !isAllowed(original)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported file type. Allowed: " + ALLOWED_EXTENSIONS);
        }

        String safeName = Paths.get(original).getFileName().toString();
        Path target = resolveFilesDir().resolve(safeName);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok(Map.of(
                "name", safeName,
                "message", "Uploaded successfully"));
    }

    /**
     * Convert a Word document to HTML via Aspose.Words for editing in Jodit.
     */
    @GetMapping(value = "/html", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> convertToHtml(@RequestParam String filename)
            throws Exception {
        Path filePath = resolveExistingFile(filename);

        Document doc = new Document(filePath.toString());

        HtmlSaveOptions options = new HtmlSaveOptions(SaveFormat.HTML);
        options.setExportImagesAsBase64(true);
        options.setCssStyleSheetType(CssStyleSheetType.INLINE);
        options.setPrettyFormat(true);
        options.setExportRoundtripInformation(false);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        doc.save(out, options);

        String fullHtml = out.toString(StandardCharsets.UTF_8.name());
        String bodyHtml = extractBody(fullHtml);

        return ResponseEntity.ok(Map.of(
                "filename", filePath.getFileName().toString(),
                "html", bodyHtml));
    }

    private Path resolveExistingFile(String filename) {
        String safeName = Paths.get(filename).getFileName().toString();
        if (safeName.isBlank() || safeName.contains("..") || !isAllowed(safeName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename");
        }
        Path path = resolveFilesDir().resolve(safeName).normalize();
        if (!path.startsWith(resolveFilesDir()) || !Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + safeName);
        }
        return path;
    }

    private static String extractBody(String fullHtml) {
        Matcher matcher = BODY_PATTERN.matcher(fullHtml);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return fullHtml;
    }

    private Path resolveFilesDir() {
        return Paths.get(filesDirectory).toAbsolutePath().normalize();
    }

    private boolean isAllowed(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }
}
