package com.vdjoseluis.vdlogistics.maps;

import com.google.cloud.firestore.GeoPoint;
import com.vdjoseluis.vdlogistics.ConfigLoader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

public class GoogleMapsService {

    private static final String API_KEY = ConfigLoader.get("GOOGLE_API_KEY");

    public static class GeocodingResult {

        public String formattedAddress;
        public GeoPoint location;
        public String city;

//        public GeocodingResult(String formattedAddress, GeoPoint location) {
//            this.formattedAddress = formattedAddress;
//            this.location = location;
//        }
    }

    public static GeocodingResult geocodeAddress(String inputAddress) {
        try {
            String encodedAddress = URLEncoder.encode(inputAddress, "UTF-8");
            String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress + "&key=" + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());

            if (!json.getString("status").equals("OK")) {
                return null;
            }

            JSONObject firstResult = json.getJSONArray("results").getJSONObject(0);
            JSONObject location = firstResult.getJSONObject("geometry").getJSONObject("location");

            String city = null;
            JSONArray components = firstResult.getJSONArray("address_components");

            for (int i = 0; i < components.length(); i++) {
                JSONObject component = components.getJSONObject(i);
                JSONArray types = component.getJSONArray("types");

                for (int j = 0; j < types.length(); j++) {
                    if (types.getString(j).equals("locality")) {
                        city = component.getString("long_name");
                        break;
                    }
                }
            }

            GeocodingResult result = new GeocodingResult();
            result.formattedAddress = firstResult.getString("formatted_address");
            result.location = new GeoPoint(location.getDouble("lat"), location.getDouble("lng"));
            result.city = city;

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public static String getFormattedAddress(String inputAddress) {
//        try {
//            String encodedAddress = java.net.URLEncoder.encode(inputAddress, "UTF-8");
//            String urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=" + encodedAddress + "&key=" + API_KEY;
//
//            URL url = new URL(urlString);
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//
//            // Leer respuesta
//            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            String inputLine;
//            StringBuilder response = new StringBuilder();
//
//            while ((inputLine = in.readLine()) != null) {
//                response.append(inputLine);
//            }
//            in.close();
//
//            JSONObject json = new JSONObject(response.toString());
//            String status = json.getString("status");
//
//            if (!status.equals("OK")) {
//                System.err.println("❌ Error en la respuesta de Google Maps: " + status);
//                return null;
//            }
//
//            JSONObject firstResult = json.getJSONArray("results").getJSONObject(0);
//            JSONArray components = firstResult.getJSONArray("address_components");
//
//            boolean hasRoute = false;
//            boolean hasNumber = false;
//            boolean hasLocality = false;
//
//            for (int i = 0; i < components.length(); i++) {
//                JSONArray types = components.getJSONObject(i).getJSONArray("types");
//                for (int j = 0; j < types.length(); j++) {
//                    String type = types.getString(j);
//                    if (type.equals("route")) {
//                        hasRoute = true;
//                    }
//                    if (type.equals("street_number")) {
//                        hasNumber = true;
//                    }
//                    if (type.equals("locality")) {
//                        hasLocality = true;
//                    }
//                }
//            }
//
//            if (hasRoute && hasNumber && hasLocality) {
//                return firstResult.getString("formatted_address");
//            } else {
//                System.err.println("❌ Dirección incompleta: falta calle, número o localidad.");
//                return null;
//            }
//
//        } catch (IOException e) {
//            System.err.println("Error al conectar con Google Maps: " + e.getMessage());
//            return null;
//        }
//    }
}
