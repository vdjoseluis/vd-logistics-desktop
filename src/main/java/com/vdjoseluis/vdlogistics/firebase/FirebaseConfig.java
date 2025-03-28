package com.vdjoseluis.vdlogistics.firebase;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;

public class FirebaseConfig {
    private static boolean initialized = false;
    private static Firestore firestore;
    private static Storage storage;

    public static void initializeFirebase() {
        if (!initialized) {
            try {
                // Cargar credenciales del archivo JSON
                FileInputStream serviceAccount = new FileInputStream("firebase-adminsdk.json");
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

                // Inicializar FirebaseApp si no está inicializado
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .build();
                    FirebaseApp.initializeApp(options);
                }

                // Inicializar Firestore
                firestore = FirestoreOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                // Inicializar Storage
                storage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                initialized = true;
                System.out.println("✅ Firebase inicializado correctamente.");
            } catch (IOException e) {
                System.err.println("❌ Error al inicializar Firebase: " + e.getMessage());
            }
        }
    }

    public static Firestore getFirestore() {
        if (!initialized) initializeFirebase();
        return firestore;
    }

    public static Storage getStorage() {
        if (!initialized) initializeFirebase();
        return storage;
    }
}
