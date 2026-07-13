package com.zivro.media;

public interface CloudinaryService {

    CloudinaryUploadResult uploadImage(byte[] data, String contentType, String publicIdPath);

    /** Best-effort delete; ignores null/blank ids. */
    void deletePublicId(String publicId);
}
