package com.vdjoseluis.vdlogistics.firebase;

import com.google.cloud.storage.*;
import java.util.ArrayList;
import java.util.List;

public class FirebaseStorage {
    
//    public static List<String> getFileList(String serviceId) {
//        List<String> fileList = new ArrayList<>();
//        try {
//            Storage storage = FirebaseConfig.getStorage();
//            String bucketName = "vd-logistics.firebasestorage.app"; 
//            Bucket bucket = storage.get(bucketName);
//
//            if (bucket == null) {
//                System.err.println("❌ Error: No se encontró el bucket " + bucketName);
//                return fileList;
//            }
//
//            String folderPath = "services/" + serviceId + "/"; 
//
//            for (Blob blob : bucket.list().iterateAll()) {
//                if (blob.getName().startsWith(folderPath)) { 
//                    fileList.add(blob.getName().replace(folderPath, "")); 
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("❌ Error obteniendo lista de archivos: " + e.getMessage());
//        }
//        return fileList;
//    }

}
