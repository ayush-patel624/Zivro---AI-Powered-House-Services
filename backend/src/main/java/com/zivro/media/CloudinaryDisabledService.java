package com.zivro.media;

import com.zivro.exception.BadRequestException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class CloudinaryDisabledService implements CloudinaryService {

    private static final Path ROOT =
            Path.of(System.getProperty("java.io.tmpdir"), "zivro-local-media");

    @Override
    public CloudinaryUploadResult uploadImage(byte[] data, String contentType, String publicIdPath) {
        String safeType = (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType;
        String publicId = "local/" + (publicIdPath == null || publicIdPath.isBlank() ? "image" : publicIdPath);
        String key =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(publicId.getBytes(StandardCharsets.UTF_8));

        try {
            Files.createDirectories(ROOT);
            Files.write(ROOT.resolve(key + ".bin"), data);
            Files.writeString(ROOT.resolve(key + ".ct"), safeType, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BadRequestException("Could not store uploaded image locally.");
        }

        // Keep DB URL small; controller serves bytes from temp dir.
        String url = "/api/local-media/" + key;
        return new CloudinaryUploadResult(url, publicId);
    }

    @Override
    public void deletePublicId(String publicId) {
        // no-op
    }
}
