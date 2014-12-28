package ch.ethz.tik.hrouting;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The Class SettingsActivity.
 */
public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about);

        TextView textView = (TextView)findViewById(R.id.text_view);
        textView.setText("We use novel high-resolution pollution maps of ultrafine particles to " +
                "help " +
                "urban dwellers in Zurich (Switzerland) to reduce their air pollution exposure. " +
                "This is achieved by not taking the shortest path between origin and destination " +
                "but a healthier and slightly longer alternative route computed by this " +
                "application.\n" +
                "\n" +
                "This application was developed as part of the OpenSense project at ETH Zurich " +
                "funded by NanoTera.ch with Swiss Confederation financing and supported by LUNGE " +
                "ZÜRICH.\n" +
                "\n" +
                "Webpage: http://www.opensense.ethz.ch\n" +
                "Developer: I. de Concini, D. Hasenfratz\n" +
                "Contact: hasenfratz@tik.ee.ethz.ch\n" +
                "\n" +
                "Background image: M. Richi, flickr.com");

        ImageView imageView = (ImageView)findViewById(R.id.image_view);
        imageView.setImageResource(R.drawable.os_eth_logo);
    }
}
