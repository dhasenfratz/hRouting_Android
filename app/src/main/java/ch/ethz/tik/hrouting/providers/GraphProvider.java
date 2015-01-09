//
//  GraphProvider.java
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.util.GraphSerializerUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

public class GraphProvider {

    private static final String TAG = "GraphProvider";
    public static final String GRAPH_FILE_NAME = "graph.ser";

    private static Graph graph = null;
    private static boolean startedInit = false;

    public static boolean isInitialized() {
        return graph != null;
    }

    public static boolean startedInitialization() {
        return startedInit;
    }

    public static void init(Graph newGraph) {
        graph = newGraph;
    }

    public static Graph getGraph() {
        return graph;
    }

    public static Graph loadGraphFromAssets(Context context) {
        startedInit = true;
        Stopwatch stopwatch = Stopwatch.createStarted();
        graph = GraphSerializerUtil
                .deSerialize(getBufferedInputStream(context));
        stopwatch.elapsed(TimeUnit.MILLISECONDS);
        Log.i(TAG, "Deserialized graph in " + stopwatch);
        return graph;
    }

    private static BufferedInputStream getBufferedInputStream(Context context) {
        AssetManager assets = context.getAssets();
        BufferedInputStream inputStream;
        try {
            inputStream = new BufferedInputStream(assets.open(GRAPH_FILE_NAME));
        } catch (IOException e) {
            Log.e(TAG, "Could not open input stream");
            e.printStackTrace();
            return null;
        }
        Preconditions.checkNotNull(inputStream);
        return inputStream;
    }


}

