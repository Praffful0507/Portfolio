package com.example.demo.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".xlsx", ".xls", ".xlsm", ".xlsb", ".csv", ".ods");

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

    private Path resolveFilesDir() {
        return Paths.get(filesDirectory).toAbsolutePath().normalize();
    }

    private boolean isAllowed(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }
}
