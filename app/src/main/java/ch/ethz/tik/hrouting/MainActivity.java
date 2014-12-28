package ch.ethz.tik.hrouting;

import android.app.AlertDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called");
        setContentView(R.layout.activity_main);

        route = new Route.Builder(null,null).build();

        context = getApplicationContext();

        // If started first time say hello to the user
        firstTime = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("first_time", true);
        if (firstTime) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().
                    putBoolean("first_time", false).commit();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Welcome")
                    .setMessage("Enter origin and destination to compute " +
                            "a health-optimal route between two locations" +
                            "in the area of Zurich (Switzerland). Check out the " +
                            "exemplary routes stored in the list below.")
                    .setCancelable(false)
                    .setPositiveButton("OK", null);
            AlertDialog alert = builder.create();
            alert.show();
        }

        initGraph();
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }
        initHistory();
        initInputFields();

        //final ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayShowHomeEnabled (true);
        //actionBar.setIcon(R.drawable.ic_launcher);
    }

    private void initGraph() {
        // If serialized graph is not loaded in memory, load it in a background
        // task.
        if (!GraphProvider.isInitialized()) {
            new LoadGraphTask().execute(context);
        }
    }

    @SuppressWarnings("unchecked")
    // Warnings because of *safe* cast to generic type
    private void restoreState(Bundle savedInstanceState) {
        Log.i(TAG, "Restore app state");
        route = (Route)savedInstanceState.get(Constants.ROUTE);
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
                route.setFrom((SearchNode)parent.getItemAtPosition(position));
                inputFrom.setError(null);
                inputTo.requestFocus();
                computePaths();
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
                inputTo.clearFocus();
                computePaths();

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
            Toast.makeText(this, "No internet connection available.",
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
                    route.setTo(null);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
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
                Route route = routeList.get(i);
                HistoryEntry.addHistoryEntry(dbHelper,
                        route,
                        MainActivity.this);
            }
        }


        final ListView historyView = (ListView) findViewById(R.id.history);
        Cursor cursor = dbHelper.getHistory();
        // TODO: Change to CursorLoader and check how it is used correctly.
        // http://www.androiddesignpatterns.com/2012/07/loaders-and-loadermanager-background.html
        startManagingCursor(cursor);
        CustomCursorAdapter cursorAdapter = new CustomCursorAdapter(this,
                cursor, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        historyView.setAdapter(cursorAdapter);
        historyView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                route = HistoryEntry.deserialize(
                        cursor.getBlob(cursor.getColumnIndex(HistoryEntry.COLUMN_ROUTE)));
                switchToMap();
                HistoryEntry.addHistoryEntry(dbHelper, route, MainActivity.this);
                resetInstanceState();
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
        inputFrom.setText("");
        inputFrom.setError(null);
        inputTo.setText("");
        inputTo.setError(null);
        route.reset();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i(TAG, "Store app state");
        outState.putSerializable(Constants.ROUTE, route);
        super.onSaveInstanceState(outState);
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
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

    public void computePaths() {
        if (inputValid()) {
            hideKeyboard();
            findViewById(R.id.progress_load_graph).setVisibility(View.VISIBLE);
            if (!GraphProvider.isInitialized()) {
                // If the serialized wasn't already loaded from assets, do
                // nothing and request to be called, once the graph is loaded.
                requestComputation = true;
                return;
            }
            new ComputePathsTask().execute();
        }
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
            HistoryEntry.addHistoryEntry(dbHelper, route, MainActivity.this);
            resetInstanceState();
            findViewById(R.id.progress_load_graph).setVisibility(View.GONE);
            // Enable input
            inputFrom.setEnabled(true);
            inputTo.setEnabled(true);
        }

    }
}
