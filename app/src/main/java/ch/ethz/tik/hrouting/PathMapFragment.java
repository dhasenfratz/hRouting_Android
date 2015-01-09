//
//  PathMapFragment.java
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

package ch.ethz.tik.hrouting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.ethz.tik.graphgenerator.elements.Route;
import ch.ethz.tik.graphgenerator.util.Constants;
import ch.ethz.tik.hrouting.util.ConversionUtil;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A {@link MapFragment} that can display paths.
 */
public class PathMapFragment extends MapFragment implements OnMapReadyCallback {

    private static final int WIDTH_THIN = 5;
    private static final int WIDTH_THICK = 14;
    private Route route;

    private GoogleMap map;

    public PathMapFragment() {
        super();
        setArguments(new Bundle());
    }

    public static PathMapFragment newInstance(Route route) {
        PathMapFragment frag = new PathMapFragment();
        frag.route = route;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            route = (Route)getArguments().getSerializable(Constants.ROUTE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater arg0, ViewGroup arg1, Bundle arg2) {
        View v = super.onCreateView(arg0, arg1, arg2);
        getMapAsync(this);
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getArguments().putSerializable(Constants.ROUTE, route);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        drawPaths();
    }

    private void drawPaths() {
        Preconditions.checkNotNull(map);

        ArrayList<LatLng> healthiestPath = ConversionUtil.getLatLngPath(route.getHOptPath());
        ArrayList<LatLng> shortestPath = ConversionUtil.getLatLngPath(route.getShortestPath());

        MarkerOptions sourceMO = new MarkerOptions()
                .position(healthiestPath.get(healthiestPath.size() - 1))
                .title(route.getFrom().getName()).visible(true);
        MarkerOptions targetMO = new MarkerOptions()
                .position(healthiestPath.get(0))
                .title(route.getTo().getName()).visible(true);
        map.clear();
        map.setMyLocationEnabled(true);
        map.addMarker(sourceMO);
        map.addMarker(targetMO);
        zoomAndDrawLines(healthiestPath, shortestPath);
    }

    private void zoomAndDrawLines(ArrayList<LatLng> healthiestPath,
                                  ArrayList<LatLng> shortestPath) {
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        Set<LatLng> duplicates = getDuplicates(healthiestPath, shortestPath);
        List<PolylineOptions> healthiestList = getPolyLineOptions(
                healthiestPath, duplicates, builder, true);
        List<PolylineOptions> shortestList = getPolyLineOptions(shortestPath,
                duplicates, builder, false);

        List<PolylineOptions> mergedShortest = mergePolylineOptions(shortestList);
        List<PolylineOptions> mergedHealthiest = mergePolylineOptions(healthiestList);

        // Zoom to path once the map has a layout.

        map.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition arg0) {
                // Move camera.
                getMap().moveCamera(
                        CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
                // Remove listener to prevent position reset on camera move.
                getMap().setOnCameraChangeListener(null);
            }
        });
        addPolylinesToMap(mergedShortest);
        addPolylinesToMap(mergedHealthiest);
    }

    private List<PolylineOptions> mergePolylineOptions(
            List<PolylineOptions> shortestList) {
        List<PolylineOptions> mergedShortest = Lists.newLinkedList();
        PolylineOptions segment = new PolylineOptions();
        float width = -1;
        for (PolylineOptions shortSegment : shortestList) {
            if (width != shortSegment.getWidth()) {
                width = shortSegment.getWidth();
                segment = new PolylineOptions();
                if (width != -1) {
                    mergedShortest.add(segment);
                }
            }
            segment.addAll(shortSegment.getPoints());
            segment.width(width);
            segment.color(shortSegment.getColor());
        }
        return mergedShortest;
    }

    private void addPolylinesToMap(List<PolylineOptions> options) {
        for (PolylineOptions polyline : options) {
            map.addPolyline(polyline);
        }
    }

    public Set<LatLng> getDuplicates(ArrayList<LatLng> healthiestPath,
                                     ArrayList<LatLng> shortestPath) {
        Set<LatLng> duplicates = Sets.newHashSet();
        Set<LatLng> all = Sets.newHashSet();
        iterateAndFindDuplicates(healthiestPath, duplicates, all);
        iterateAndFindDuplicates(shortestPath, duplicates, all);
        return duplicates;
    }

    private void iterateAndFindDuplicates(ArrayList<LatLng> path,
                                          Set<LatLng> duplicates, Set<LatLng> all) {
        for (LatLng element : path) {
            if (all.contains(element)) {
                duplicates.add(element);
            }
            all.add(element);
        }
    }

    private List<PolylineOptions> getPolyLineOptions(
            ArrayList<LatLng> latLngPath, Set<LatLng> duplicates,
            LatLngBounds.Builder builder, boolean healthiest) {
        int color = healthiest ? Color.rgb(0, 204, 204) : Color
                .rgb(227, 74, 51);
        List<PolylineOptions> optionsList = Lists.newLinkedList();

        LatLng previous = null;
        for (LatLng current : latLngPath) {
            builder.include(current);
            if (previous != null) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.add(previous).add(current);
                polylineOptions.color(color);
                if (healthiest && duplicates.contains(previous)
                        && duplicates.contains(current)) {
                    polylineOptions.width(WIDTH_THIN);
                } else {
                    polylineOptions.width(WIDTH_THICK);
                }
                optionsList.add(polylineOptions);
            }
            previous = current;
        }
        return optionsList;
    }
}