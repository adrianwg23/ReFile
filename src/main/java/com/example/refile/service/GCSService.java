package com.example.refile.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.stereotype.Service;

@Service
public class GCSService {

    public static final String ATTACHMENTS_BUCKET = "syde-attachments";

    public Storage storage;

    public GCSService(Storage storage) {
        this.storage = storage;
    }

    public void write(String bucket, String fileName, byte[] attachmentData) {
        BlobId blobId = BlobId.of(bucket, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, attachmentData);
    }
}
