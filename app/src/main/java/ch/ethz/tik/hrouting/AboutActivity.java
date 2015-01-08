//
//  AboutActivity.java
//  hRouting
//
//  Created by David Hasenfratz on 08/01/15.
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
                "Developers: I. de Concini, D. Hasenfratz\n" +
                "Contact: hasenfratz@tik.ee.ethz.ch\n" +
                "\n" +
                "Background image: M. Richi, flickr.com");

        ImageView imageView = (ImageView)findViewById(R.id.image_view);
        imageView.setImageResource(R.drawable.os_eth_logo);

        // Set background image.
        getWindow().setBackgroundDrawableResource(R.drawable.bg_img);
    }
}
