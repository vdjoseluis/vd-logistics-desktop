/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vdjoseluis.vdlogistics.maps;
import javax.swing.*;
import java.awt.*;

public class GoogleMapsViewer {
    public static void showLocation(double lat, double lng) {
        JFrame frame = new JFrame("Ubicación en Google Maps");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String mapsUrl = "https://www.google.com/maps?q=" + lat + "," + lng;
        JEditorPane webView = new JEditorPane();
        webView.setEditable(false);
        
        try {
            webView.setPage(mapsUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.add(new JScrollPane(webView), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    /*public static void main(String[] args) {
        showLocation(37.7749, -122.4194); // Muestra San Francisco en el mapa
    }*/
}

