package ch.ethz.tik.graphgenerator.providers;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;
import com.google.code.geocoder.model.LatLngBounds;

import java.math.BigDecimal;
import java.io.IOException;
import java.util.Scanner;

import ch.ethz.tik.graphgenerator.elements.SearchNode;

public class GeocodeProvider {

    private static final double BOUNDS_NORTHEAST_LON = 8.610260422;
    private static final double BOUNDS_SOUTHWEST_LON = 8.464171646;
    private static final double BOUNDS_NORTHEAST_LAT = 47.43669326;
    private static final double BOUNDS_SOUTHWEST_LAT = 47.32839174;

    static public SearchNode getGeocode(String location) {

        if (location == null)
            return null;

        Geocoder geocoder = new Geocoder();
        LatLngBounds bounds = new LatLngBounds(
                new LatLng(new BigDecimal(BOUNDS_SOUTHWEST_LAT), new BigDecimal(BOUNDS_SOUTHWEST_LON)),
                new LatLng(new BigDecimal(BOUNDS_NORTHEAST_LAT), new BigDecimal(BOUNDS_NORTHEAST_LON)));
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
                .setAddress(location)
                .setLanguage("de")
                .setBounds(bounds)
                .getGeocoderRequest();
        GeocodeResponse geocoderResponse;

        try {
            geocoderResponse = geocoder.geocode(geocoderRequest);

            if (geocoderResponse.getStatus() == GeocoderStatus.OK
                    & !geocoderResponse.getResults().isEmpty()) {
                GeocoderResult geocoderResult =
                        geocoderResponse.getResults().iterator().next();
                LatLng latitudeLongitude =
                        geocoderResult.getGeometry().getLocation();

                // Only use first part of the address.
                Scanner lineScanner = new Scanner(geocoderResult.getFormattedAddress());
                lineScanner.useDelimiter(",");

                SearchNode node = new SearchNode(
                        lineScanner.next(),
                        latitudeLongitude.getLat().doubleValue(),
                        latitudeLongitude.getLng().doubleValue());
                return node;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
