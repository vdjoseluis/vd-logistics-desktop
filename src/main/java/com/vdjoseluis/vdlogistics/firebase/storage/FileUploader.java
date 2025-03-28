package com.vdjoseluis.vdlogistics.firebase.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUploader {

    public static void uploadFile(File localFile, String storagePath) throws IOException {
        
        String bucketName = "vd-logistics.firebasestorage.app"; 
        Storage storage = StorageClient.getInstance().bucket(bucketName).getStorage();

        
        BlobId blobId = BlobId.of(bucketName, storagePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/octet-stream").build(); 

        
        try (FileInputStream fileInputStream = new FileInputStream(localFile)) {
            storage.create(blobInfo, fileInputStream.readAllBytes());
        }
    }

}
