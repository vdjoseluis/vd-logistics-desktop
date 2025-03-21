package com.vdjoseluis.vdlogistics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream(".env"));
        } catch (IOException e) {
            System.err.println("❌ Error cargando archivo .env: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return properties.getProperty(key).trim();
    }
}
