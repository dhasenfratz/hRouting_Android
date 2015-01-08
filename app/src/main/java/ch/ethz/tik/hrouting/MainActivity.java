package ch.ethz.tik.hrouting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import ch.ethz.tik.graphgenerator.elements.Node;
import ch.ethz.tik.graphgenerator.elements.SearchNode;
import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.providers.ShortestPathProvider;
import ch.ethz.tik.graphgenerator.util.Constants;
import ch.ethz.tik.graphgenerator.elements.Route;
import ch.ethz.tik.hrouting.providers.HistoryDbHelper;
import ch.ethz.tik.hrouting.providers.GraphProvider;
import ch.ethz.tik.hrouting.util.HistoryDBContract.HistoryEntry;
import ch.ethz.tik.hrouting.providers.CustomCursorAdapter;
import ch.ethz.tik.hrouting.providers.PlacesAutoCompleteAdapter;

public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";
    public static final String ROUTES_FILE_NAME = "routes.ser";

    private static Route route;

    private boolean requestComputation = false;
    private boolean firstTime = true;

    private TextWatcher fromWatcher = null;
    private TextWatcher toWatcher = null;

    private AutoCompleteTextView inputFrom;
    private AutoCompleteTextView inputTo;

    public static Context context = null;
    private HistoryDbHelper dbHelper;

    private static AlertDialog alertFirstTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            route = new Route.Builder(null, null).build();
        }

        context = getApplicationContext();

        // If started first time say hello to the user
        firstTime = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("first_time", true);
        if (firstTime) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().
                    putBoolean("first_time", false).apply();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Welcome")
                    .setMessage("Enter origin and destination to compute " +
                            "a health-optimal route between two locations" +
                            "in the area of Zurich (Switzerland). Check out the " +
                            "exemplary routes stored in the list below.")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            alertFirstTime = null;
                        }
                    });
            alertFirstTime = builder.create();
            alertFirstTime.show();
        }

        // Set background image.
        getWindow().setBackgroundDrawableResource(R.drawable.bg_img);

        initGraph();
        initHistory();
        initInputFields();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        if (inputFrom.hasFocus() || inputTo.hasFocus()) {
            showKeyboard(false);
            inputFrom.clearFocus();
            inputTo.clearFocus();
        }
        if(alertFirstTime != null)
            alertFirstTime.dismiss();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if(alertFirstTime != null)
            alertFirstTime.show();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Constants.ROUTE, route);
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("unchecked")
    // Warnings because of *safe* cast to generic type
    private void restoreState(Bundle savedInstanceState) {
        Log.i(TAG, "Restore app state");
        route = (Route)savedInstanceState.get(Constants.ROUTE);
    }

    private void initGraph() {
        // If serialized graph is not loaded in memory, load it in a background
        // task.
        if (!GraphProvider.startedInitialization()) {
            new LoadGraphTask().execute(context);
        }
    }

    private void initInputFields() {
        inputFrom = (AutoCompleteTextView) findViewById(R.id.autocomplete_from);
        inputFrom.setAdapter(new PlacesAutoCompleteAdapter(this,
                android.R.layout.simple_dropdown_item_1line));
        inputFrom.addTextChangedListener(getFromTextWatcher());

        inputTo = (AutoCompleteTextView) findViewById(R.id.autocomplete_to);
        inputTo.setAdapter(new PlacesAutoCompleteAdapter(this,
                android.R.layout.simple_dropdown_item_1line));
        inputTo.addTextChangedListener(getToTextWatcher());

        // Sets the from field and moves focus to the toView
        inputFrom.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                route.setFrom((SearchNode) parent.getItemAtPosition(position));
                inputFrom.setError(null);
                if (!computePaths())
                    inputTo.requestFocus();
                else
                    inputFrom.clearFocus();
            }
        });

        inputFrom.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE && route.getFrom() == null){
                    Toast.makeText(MainActivity.this, "Select origin from list", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        inputFrom.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                checkNetworkConnection();
                if (!hasFocus && route.getFrom() == null) {
                    inputFrom.setError("Select origin from list.");
                }
            }
        });

        // Sets the from field and moves focus to the toView
        inputTo.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                route.setTo((SearchNode)parent.getItemAtPosition(position));
                inputTo.setError(null);
                if (!computePaths())
                    inputFrom.requestFocus();
                else
                    inputTo.clearFocus();
            }
        });

        inputTo.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE && route.getTo() == null){
                    Toast.makeText(MainActivity.this, "Select destination from list", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });

        inputTo.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                checkNetworkConnection();
                if (!hasFocus && route.getTo() == null) {
                    inputTo.setError("Select destination from list.");
                }
            }
        });

        checkNetworkConnection();
    }

    public void checkNetworkConnection() {
        boolean isNetwork = isNetworkAvailable();

        if (!isNetwork) {
            inputFrom.setError("No internet connection");
            inputTo.setError("No internet connection");
            Toast.makeText(this, "No internet connection available",
                    Toast.LENGTH_LONG).show();
        } else {
            inputFrom.setError(null);
            inputTo.setError(null);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Returns a {@link TextWatcher} that resets the given node and sets the
     * given ImageView to the invalid icon, when the input text changes.
     *
     * @return the from text watcher
     */
    private TextWatcher getFromTextWatcher() {
        if (fromWatcher == null) {
            fromWatcher = new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (before != 0)
                        route.setFrom(null);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
        }
        return fromWatcher;
    }

    /**
     * Returns a {@link TextWatcher} that resets the given node and sets the
     * given ImageView to the invalid icon, when the input text changes.
     *
     * @return the from text watcher
     */
    private TextWatcher getToTextWatcher() {
        if (toWatcher == null) {
            toWatcher = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (before != 0) {
                        route.setTo(null);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            };
        }
        return toWatcher;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int[] getScores(List<Node> nodes) {
        Graph graph = GraphProvider.getGraph();
        int[] array = new int[2];
        array[Constants.INDEX_POLLUTION] = graph.getScore(nodes, true);
        array[Constants.INDEX_DISTANCE] = graph.getScore(nodes, false);
        return array;
    }

    @SuppressWarnings("unchecked")
    private List<Route> loadExampleRoutes() {
        List<Route> routeList = null;
        AssetManager assets = context.getAssets();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(assets.open(ROUTES_FILE_NAME));
            routeList = (List<Route>)inputStream.readObject();
        } catch (IOException e) {
            Log.e(TAG, "Could not open input stream with example routes");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not cast input stream object");
            e.printStackTrace();
        }

        return routeList;
    }

    private void initHistory() {
        dbHelper = new HistoryDbHelper(this);

        // Load exemplary routes if app is started the first time

        if (firstTime) {
            dbHelper.deleteHistory();
            List <Route> routeList = loadExampleRoutes();

            for (int i = 0; i < routeList.size(); i++) {
                Route r = routeList.get(i);
                HistoryEntry.addHistoryEntry(dbHelper,
                        r,
                        MainActivity.this);
            }
        }

        final ListView historyView = (ListView) findViewById(R.id.history);
        Cursor cursor = dbHelper.getHistory();
        // TODO: Replace this with CursorLoader
        startManagingCursor(cursor);
        CustomCursorAdapter cursorAdapter = new CustomCursorAdapter(this,
                cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        historyView.setAdapter(cursorAdapter);
        historyView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Disable input while loading route.
                inputFrom.setEnabled(false);
                inputTo.setEnabled(false);

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                route = HistoryEntry.deserialize(
                        cursor.getBlob(cursor.getColumnIndex(HistoryEntry.COLUMN_ROUTE)));
                switchToMap();

                // Use background thread to add entry to DB.
                new AddEntryToDbTask().execute();
            }
        });
    }

    /**
     * Create a new intent to switch to the map activity.
     */
    public void switchToMap() {
        Intent intent = new Intent(this, PathInfoActivity.class);
        intent.putExtra(Constants.ROUTE, route);
        startActivity(intent);
    }

    private void resetInstanceState() {
        inputFrom.getText().clear();
        inputFrom.setError(null);
        inputTo.getText().clear();
        inputTo.setError(null);
        route.reset();
    }

    private void showKeyboard(boolean show) {
        InputMethodManager inputManager = (InputMethodManager)getSystemService(Context
                .INPUT_METHOD_SERVICE);

        // Check if any view has focus.
        View view = getCurrentFocus();
        if (view != null) {
            if (show) {
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                //inputManager.showSoftInputFromInputMethod(view.getWindowToken(),
                //        InputMethodManager.SHOW_IMPLICIT);
            } else {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    private boolean inputValid() {
        boolean fromIsValid = !(route.getFrom() == null);
        boolean toIsValid = !(route.getTo() == null);
        boolean namesEqual = true;
        if (fromIsValid && toIsValid) {
            namesEqual = route.getFrom().getName().equals(route.getTo().getName());
            if (namesEqual) {
                Toast.makeText(this, "Start and destination are equal.",
                        Toast.LENGTH_LONG).show();
            }
        }
        return fromIsValid && toIsValid && !namesEqual;
    }

    private boolean computePaths() {
        if (inputValid()) {
            showKeyboard(false);
            findViewById(R.id.progress_load_graph).setVisibility(View.VISIBLE);
            if (!GraphProvider.isInitialized()) {
                // If the serialized wasn't already loaded from assets, do
                // nothing and request to be called, once the graph is loaded.
                requestComputation = true;
                // Deactivate input and wait for the graph to be loaded.
                findViewById(R.id.progress_load_graph).setVisibility(View.VISIBLE);
                inputFrom.setEnabled(false);
                inputTo.setEnabled(false);
                return true;
            }
            new ComputePathsTask().execute();
            return true;
        }
        return false;
    }

    /**
     * Background task to load graph.
     */
    private class LoadGraphTask extends AsyncTask<Context, Void, Void> {
        @Override
        protected void onPreExecute() {
            findViewById(R.id.progress_load_graph).setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Context... params) {
            GraphProvider.init(GraphProvider.loadGraphFromAssets(params[0]));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            findViewById(R.id.progress_load_graph).setVisibility(View.GONE);
            if (requestComputation) {
                new ComputePathsTask().execute();
            }
            super.onPostExecute(result);
        }
    }

    /**
     * Background task to compute paths.
     */
    private class ComputePathsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progress_load_graph).setVisibility(View.VISIBLE);
            // Disable input while computing route
            inputFrom.setEnabled(false);
            inputTo.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Graph graph = GraphProvider.getGraph();
            List<Node> healthiestNodes = ShortestPathProvider.between(graph, route.getTo(),
                    route.getFrom(), true);
            List<Node> shortestNodes = ShortestPathProvider.between(graph, route.getTo(),
                    route.getFrom(), false);
            route.setHOptPath(healthiestNodes);
            route.setShortestPath(shortestNodes);
            route.setHOptPathScores(getScores(healthiestNodes));
            route.setShortestPathScores(getScores(shortestNodes));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            switchToMap();
            requestComputation = false;
            // Use background thread to add entry to DB.
            new AddEntryToDbTask().execute();

        }

    }

    /**
     * Background task to add entry to database.
     */
    private class AddEntryToDbTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            findViewById(R.id.progress_load_graph).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HistoryEntry.addHistoryEntry(dbHelper, route, MainActivity.this);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            inputFrom.setEnabled(true);
            inputTo.setEnabled(true);
            resetInstanceState();
            findViewById(R.id.progress_load_graph).setVisibility(View.GONE);
        }
    }
}
