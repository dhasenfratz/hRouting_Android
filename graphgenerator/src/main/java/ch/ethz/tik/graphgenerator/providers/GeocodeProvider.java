//
//  GeocodeProvider.java
//  hRouting
//
//  Created by Ivo de Concini, David Hasenfratz on 08/01/15.
//  Copyright (c) 2015 TIK, ETH Zurich. All rights reserved.
//
//  hRouting is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  hRouting is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with hRouting.  If not, see <http://www.gnu.org/licenses/>.
//

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

                return new SearchNode(
                        lineScanner.next(),
                        latitudeLongitude.getLat().doubleValue(),
                        latitudeLongitude.getLng().doubleValue());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
