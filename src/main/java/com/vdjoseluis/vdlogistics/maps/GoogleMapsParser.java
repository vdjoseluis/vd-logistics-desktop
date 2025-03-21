
package com.vdjoseluis.vdlogistics.maps;

public class GoogleMapsParser {
    public static double[] getCoordinatesFromUrl(String url) {
        if (url.contains("/maps/place/")) {
            String[] parts = url.split("/maps/place/")[1].split(",");
            try {
                double lat = Double.parseDouble(parts[0]);
                double lng = Double.parseDouble(parts[1]);
                return new double[]{lat, lng};
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
