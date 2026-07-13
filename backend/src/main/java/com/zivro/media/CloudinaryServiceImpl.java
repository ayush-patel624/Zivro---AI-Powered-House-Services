package com.zivro.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.zivro.config.CloudinaryProperties;
import com.zivro.exception.BadRequestException;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    private final String folderPrefix;

    public CloudinaryServiceImpl(CloudinaryProperties properties) {
        this.folderPrefix = properties.uploadFolder().replaceAll("/+$", "");
        this.cloudinary =
                new Cloudinary(
                        ObjectUtils.asMap(
                                "cloud_name", properties.cloudName(),
                                "api_key", properties.apiKey(),
                                "api_secret", properties.apiSecret()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CloudinaryUploadResult uploadImage(byte[] data, String contentType, String publicIdPath) {
        String publicId = folderPrefix + "/" + publicIdPath.replaceAll("^/+", "");
        try {
            Map<String, Object> options =
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", true,
                            "resource_type", "image");
            Map<String, Object> result = cloudinary.uploader().upload(data, options);
            String url = (String) result.get("secure_url");
            String pid = (String) result.get("public_id");
            if (url == null || pid == null) {
                throw new BadRequestException("Cloudinary upload returned an unexpected payload.");
            }
            return new CloudinaryUploadResult(url, pid);
        } catch (IOException e) {
            log.error("Cloudinary upload failed", e);
            throw new BadRequestException("Image upload failed. Please try again.");
        }
    }

    @Override
    public void deletePublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            log.warn("Cloudinary delete failed for {}", publicId, e);
        }
    }
}
