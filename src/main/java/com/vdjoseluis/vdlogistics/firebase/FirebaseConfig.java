package com.vdjoseluis.vdlogistics.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {

    private static boolean initialized = false;

    public static void initializeFirebase() {
        if (!initialized) {
            try {
                FileInputStream serviceAccount = new FileInputStream("firebase-adminsdk.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                initialized = true;
                System.out.println("✅ Firebase inicializado correctamente.");
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("❌ Error al inicializar Firebase: " + e.getMessage());
            }
        }
    }

    public static Firestore getFirestore() {
        if (!initialized) {
            initializeFirebase();  // Asegura que Firebase esté inicializado
        }
        return FirestoreClient.getFirestore();
    }
}
