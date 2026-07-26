package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aspose.gridjs.GridJsJakartaControllerBase;
import com.aspose.gridjs.GridJsService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * GridJs REST API under /api/excel.
 * Based on Aspose.Cells.Grid-for-Java Examples.GridJs.SpringBoot_v3.5.7
 */
@RestController
@RequestMapping("/api/excel")
public class GridJsController extends GridJsJakartaControllerBase {

    @Value("${app.files-directory}")
    private String filesDirectory;

    @Autowired
    public GridJsController(GridJsService gridJsService) {
        super(gridJsService);
    }

    @GetMapping("/LoadSpreadsheet")
    public ResponseEntity<String> loadSpreadsheet(
            @RequestParam String filename,
            @RequestParam String uid) throws Exception {
        String fullFilePath = getFullFilePath(filename);
        StringBuilder json = _gridJsService.detailFileJsonWithUid(fullFilePath, uid);
        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=UTF-8")
                .body(json.toString());
    }

    private String getFullFilePath(String filename) {
        String safeName = PathsSafe(filename);
        return java.nio.file.Paths.get(filesDirectory)
                .toAbsolutePath()
                .normalize()
                .resolve(safeName)
                .toString();
    }

    private static String PathsSafe(String filename) {
        String name = java.nio.file.Paths.get(filename).getFileName().toString();
        if (name.isBlank() || name.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }
        return name;
    }

    @PostMapping("/UpdateCell")
    public ResponseEntity<String> updateCell(HttpServletRequest request) {
        try {
            return super.updateCell(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/AddImage")
    public ResponseEntity<String> addImage(
            @RequestParam(value = "image", required = false) MultipartFile file,
            @RequestParam("uid") String uid,
            @RequestParam("p") String p,
            @RequestParam(value = "control", required = false) String isControl) {
        return super.addImage(file, uid, p, isControl);
    }

    @PostMapping("/CopyImage")
    public ResponseEntity<String> copyImage(HttpServletRequest request) {
        return super.copyImage(request);
    }

    @PostMapping("/AddImageByURL")
    public ResponseEntity<String> addImageByUrl(HttpServletRequest request) {
        return super.addImageByUrl(request);
    }

    @GetMapping("/Image")
    public ResponseEntity<InputStreamResource> getImage(HttpServletRequest request) {
        return super.getImage(request);
    }

    @GetMapping("/ImageUrl")
    public ResponseEntity<String> getImageUrl(
            @RequestParam String id,
            @RequestParam String uid) {
        return super.getImageUrl(id, uid);
    }

    @GetMapping("/Ole")
    public ResponseEntity<?> getOle(HttpServletRequest request) {
        return super.getOle(request);
    }

    @GetMapping("/GetFile")
    public ResponseEntity<?> getFile(@RequestParam("id") String id) {
        return super.getFile(id);
    }

    @PostMapping("/Download")
    public ResponseEntity<String> download(HttpServletRequest request) {
        return super.download(request);
    }
}
