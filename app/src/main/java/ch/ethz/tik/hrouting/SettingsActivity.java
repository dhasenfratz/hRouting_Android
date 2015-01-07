package ch.ethz.tik.hrouting;

import android.app.Activity;
import android.os.Bundle;

/**
 * The Class SettingsActivity.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set background image.
        getWindow().setBackgroundDrawableResource(R.drawable.bg_img);

        if (savedInstanceState == null) {
            // Display the setting fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }
}
