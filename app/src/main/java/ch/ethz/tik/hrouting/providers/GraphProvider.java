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
    // TODO: Can we split the file in multiple parts and load them in parallel?
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
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(assets.open(GRAPH_FILE_NAME));
        } catch (IOException e) {
            Log.e(TAG, "Could not open input stream");
            e.printStackTrace();
        }
        Preconditions.checkNotNull(inputStream);
        return inputStream;
    }


}

