package com.vdjoseluis.vdlogistics.firebase.storage;

import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import com.vdjoseluis.vdlogistics.firebase.FirebaseConfig;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class FileOpener {

    private JList<String> sharedFileList;
    private DefaultListModel<String> listModel;
    private String serviceId;  // ID del servicio actual

    public FileOpener(JList<String> sharedFileList, String serviceId) {
        this.sharedFileList = sharedFileList;
        this.serviceId = serviceId;
        this.listModel = new DefaultListModel<>();
        sharedFileList.setModel(listModel);

        loadFileList();
    }

    private void loadFileList() {
        try {
            Storage storage = FirebaseConfig.getStorage();
            String bucketName = "vd-logistics.firebasestorage.app";
            String folderPath = "services/" + serviceId + "/";

            Bucket bucket = storage.get(bucketName);
            List<String> fileNames = new ArrayList<>();

            for (Blob blob : bucket.list(Storage.BlobListOption.prefix(folderPath)).iterateAll()) {
                String fileName = blob.getName().replace(folderPath, "");
                if (!fileName.isEmpty()) {
                    fileNames.add(fileName);
                }
            }

            // Actualizar JList con los nombres de archivo
            SwingUtilities.invokeLater(() -> {
                listModel.clear();
                for (String file : fileNames) {
                    listModel.addElement(file);
                }
            });

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo lista de archivos: " + e.getMessage());
        }
    }

    public static void openSelectedFile(JList fileList, String serviceId) {
        String selectedFile = (String) fileList.getSelectedValue();

        if (selectedFile == null || selectedFile.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No se ha seleccionado ningún archivo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String bucketName = "vd-logistics.firebasestorage.app";
            String filePathInBucket = "services/" + serviceId + "/" +  selectedFile;

            Bucket bucket = StorageClient.getInstance().bucket(bucketName);
            Blob blob = bucket.get(filePathInBucket);

            if (blob == null) {
                JOptionPane.showMessageDialog(null, "El archivo no existe en Firebase Storage: " + selectedFile, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File localFile = File.createTempFile("tempFile", selectedFile.substring(selectedFile.lastIndexOf(".")));
            blob.downloadTo(localFile.toPath());

            Desktop.getDesktop().open(localFile);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error al abrir el archivo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
