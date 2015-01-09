//
//  PlacesAutoCompleteAdapter.java
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

package ch.ethz.tik.hrouting.providers;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import ch.ethz.tik.graphgenerator.elements.Node;
import ch.ethz.tik.graphgenerator.elements.SearchNode;

import com.google.common.collect.ImmutableList;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<SearchNode>
        implements Filterable {
    
    private static final double BOUNDS_NORTHEAST_LON = 8.610260422;
    private static final double BOUNDS_SOUTHWEST_LON = 8.464171646;
    private static final double BOUNDS_NORTHEAST_LAT = 47.43669326;
    private static final double BOUNDS_SOUTHWEST_LAT = 47.32839174;

    private ArrayList<SearchNode> resultList;

    private static final String LOG_TAG = "PlacesAutoComplete";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "autocomplete";
    private static final String TYPE_DETAILS = "details";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    public PlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public int getCount() {
        return resultList != null ? resultList.size() : 0;
    }

    @Override
    public SearchNode getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the auto-complete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    if (resultList != null) {
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }

    private ArrayList<SearchNode> autocomplete(String input) {
        ArrayList<SearchNode> resultList = null;
        String encodedInput = getEncodedInput(input);
        List<String> parameters = new ImmutableList.Builder<String>()
                .add("components=country:ch").add("input=" + encodedInput)
                .build();

        StringBuilder jsonResults = getJsonResults(TYPE_AUTOCOMPLETE, parameters);

        if (jsonResults == null)
            return null;

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList<>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                String placeId = predsJsonArray.getJSONObject(i).getString("place_id");

                SearchNode result = getPlaceDetails(placeId);
                if (result != null && isInBoundaries(result)) {
                    resultList.add(result);
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }

    private boolean isInBoundaries(Node node) {
        double lat = node.getLatitude();
        double lng = node.getLongitude();
        return lat >= BOUNDS_SOUTHWEST_LAT && lng <= BOUNDS_NORTHEAST_LAT && lng >= BOUNDS_SOUTHWEST_LON && lng <= BOUNDS_NORTHEAST_LON;
    }

    private SearchNode getPlaceDetails(String placeId) {
        List<String> parameters = new ImmutableList.Builder<String>().add(
                "placeid=" + placeId).build();
        StringBuilder jsonResults = getJsonResults(TYPE_DETAILS, parameters);

        // Create a JSON object hierarchy from the results
        try {
            JSONObject jsonResult = new JSONObject(jsonResults.toString())
                    .getJSONObject("result");
            String name = jsonResult.getString("name");

            JSONObject jsonLocation = jsonResult.getJSONObject("geometry")
                    .getJSONObject("location");
            double latitude = jsonLocation.getDouble("lat");
            double longitude = jsonLocation.getDouble("lng");
            return new SearchNode(name, latitude, longitude);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return null;
    }

    private String getEncodedInput(String input) {
        String encodedInput = null;
        try {
            encodedInput = URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            Log.e(LOG_TAG, "Error encoding input", e1);
            e1.printStackTrace();
        }
        return encodedInput;
    }

    private StringBuilder getJsonResults(String requestType,
                                         List<String> parameters) {
        StringBuilder jsonResults = new StringBuilder();
        HttpURLConnection connection = null;
        String query = buildQuery(requestType, parameters);
        try {
            URL url = new URL(query);
            connection = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(
                    connection.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            e.printStackTrace();

            // Autocomplete is running in a thread, create a handler to post
            // message to the main thread
            Handler mHandler = new Handler(getContext().getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext().getApplicationContext(), "Not able to reach Google Place API", Toast.LENGTH_LONG).show();
                }
            });

            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return jsonResults;
    }

    private String buildQuery(String requestType, List<String> parameters) {
        StringBuilder query = new StringBuilder(PLACES_API_BASE + "/"
                + requestType + OUT_JSON).append("?key=" + API_KEY);
        for (String parameter : parameters) {
            query.append("&").append(parameter);
        }
        query.append("&language=de");
        return query.toString();
    }
}