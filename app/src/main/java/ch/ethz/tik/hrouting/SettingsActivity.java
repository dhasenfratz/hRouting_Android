package ch.ethz.tik.hrouting;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import ch.ethz.tik.hrouting.providers.HistoryDbHelper;

/**
 * The Class SettingsActivity.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new HistoryPreferenceFragment())
                .commit();
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        // loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public class HistoryPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_history);
            Preference button = (Preference) findPreference("delete_history");
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference arg0) {
                    deleteHistory();
                    return true;
                }
            });
        }
        Preference maxSize = (Preference) findPreference("size_history");
    }

    void deleteHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                SettingsActivity.this);
        builder.setMessage("Delete all history entries?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    new HistoryDbHelper(SettingsActivity.this).deleteHistory();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    // No button clicked
                    break;
            }
        }
    };

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

}
