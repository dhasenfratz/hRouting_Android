package ch.ethz.tik.hrouting;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import ch.ethz.tik.graphgenerator.elements.Route;
import ch.ethz.tik.graphgenerator.util.Constants;

public class PathInfoActivity extends ActionBarActivity {

    private static final String TAG = "PathInfoActivity";
    private Route route;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_info);
        if (savedInstanceState == null) {
            savedInstanceState = getIntent().getExtras();
        }
        setInstanceState(savedInstanceState);

        setTitleText();
        setInfoText();
        setMap();
    }

    /**
     * Sets all class fields to the values passed inside a {@link Bundle}.
     *
     * @param bundle
     *            the instance state
     */
    private void setInstanceState(Bundle bundle) {
        route = (Route)bundle.get(Constants.ROUTE);
    }

    /**
     * Sets the info text.
     */
    private void setInfoText() {
        TextView infoText = (TextView) findViewById(R.id.info_text);
        String infoTextString = getInfoTextString();
        infoText.setText(infoTextString);
    }

    /**
     * Sets the title of the activity.
     */
    private void setTitleText() {
        setTitle(route.getFrom().getName() + Constants.ARROW +  route.getTo().getName());
    }

    /**
     * Generates the info text using the path scores.
     *
     * @return the info text string
     */
    private String getInfoTextString() {
        StringBuilder builder = new StringBuilder();
        DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols();
        formatSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        df.setDecimalFormatSymbols(formatSymbols);
        int polSP = route.getShortestPathScores()[Constants.INDEX_POLLUTION];
        int polHP = route.getHOptPathScores()[Constants.INDEX_POLLUTION];
        float healthGain = polSP==0?0:(float)((polSP - polHP)) / polSP * 100;

        builder.append("The shortest path (red) is ")
                .append(df.format((float)route.getShortestPathScores()[Constants.INDEX_DISTANCE]/1000))
                .append("km long. ")
                .append("The health-optimal path (green) has ")
                .append(df.format(healthGain))
                .append("% less air pollution exposure and is ")
                .append(route.getHOptPathScores()[Constants.INDEX_DISTANCE]
                        - route.getShortestPathScores()[Constants.INDEX_DISTANCE])
                .append("m longer.");
        return builder.toString();
    }

    /**
     * Sets the map.
     */
    public void setMap() {
        PathMapFragment mapFragment = PathMapFragment.newInstance(route);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.map_container, mapFragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Constants.ROUTE, route);
        super.onSaveInstanceState(outState);
    }
}