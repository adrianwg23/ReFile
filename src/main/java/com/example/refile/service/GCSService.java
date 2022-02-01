package com.example.refile.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class GCSService {

    public static final String ATTACHMENTS_BUCKET = "syde-attachments";

    public final Storage storage;

    public GCSService() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    public void write(String bucket, String fileName, byte[] attachmentData) {
        BlobId blobId = BlobId.of(bucket, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, attachmentData);
    }

    public URL getSignedUrl(String bucket, String filename) {
        BlobId blobId = BlobId.of(bucket, filename);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        return storage.signUrl(blobInfo, 1, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
    }
}
