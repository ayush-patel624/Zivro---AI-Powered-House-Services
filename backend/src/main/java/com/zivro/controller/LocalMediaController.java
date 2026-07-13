package com.zivro.controller;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/local-media")
public class LocalMediaController {

    private static final Path ROOT =
            Path.of(System.getProperty("java.io.tmpdir"), "zivro-local-media");

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> get(@PathVariable String key) throws Exception {
        if (!StringUtils.hasText(key) || key.length() > 200) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        // only allow base64url chars
        if (!key.matches("^[A-Za-z0-9_-]+$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Path bin = ROOT.resolve(key + ".bin").normalize();
        Path ct = ROOT.resolve(key + ".ct").normalize();
        if (!bin.startsWith(ROOT) || !ct.startsWith(ROOT)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (!Files.exists(bin) || !Files.isRegularFile(bin)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String contentType = "application/octet-stream";
        if (Files.exists(ct) && Files.isRegularFile(ct)) {
            String s = Files.readString(ct, StandardCharsets.UTF_8).trim();
            if (!s.isBlank()) {
                contentType = s;
            }
        }
        byte[] data = Files.readAllBytes(bin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}

