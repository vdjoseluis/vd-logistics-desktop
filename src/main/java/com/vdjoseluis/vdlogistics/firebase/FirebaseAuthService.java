package com.vdjoseluis.vdlogistics.firebase;

import com.vdjoseluis.vdlogistics.ConfigLoader;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FirebaseAuthService {
    private static final String API_KEY = ConfigLoader.get("FIREBASE_API_KEY");  

    public static boolean loginUser(String email, String password) {
        try {
            String firebaseAuthUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
            URL url = new URL(firebaseAuthUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Crear JSON con email y password
            JSONObject jsonParam = new JSONObject();
            jsonParam.put("email", email);
            jsonParam.put("password", password);
            jsonParam.put("returnSecureToken", true);

            OutputStream os = conn.getOutputStream();
            os.write(jsonParam.toString().getBytes());
            os.flush();
            os.close();

            // Leer la respuesta
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            // Si la respuesta contiene un token, el login fue exitoso
            JSONObject jsonResponse = new JSONObject(response.toString());
            if (jsonResponse.has("idToken")) {
                System.out.println("✅ Login exitoso: " + email);
                return true;
            }
        } catch (Exception e) {
            System.err.println("❌ Error en autenticación: " + e.getMessage());
        }
        return false;
    }
}