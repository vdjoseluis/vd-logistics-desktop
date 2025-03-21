
package com.vdjoseluis.vdlogistics.maps;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GoogleMapsLinkResolver {

    public static String resolveShortLink(String shortUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(shortUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();
            String longUrl = connection.getHeaderField("Location"); // Obtiene la URL real
            connection.disconnect();
            return longUrl;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
