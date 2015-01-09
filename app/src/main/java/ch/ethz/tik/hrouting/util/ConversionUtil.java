//
//  ConversionUtil.java
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

package ch.ethz.tik.hrouting.util;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.tik.graphgenerator.elements.Node;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;

public class ConversionUtil {

    private ConversionUtil() {
    }

    public static ArrayList<LatLng> getLatLngPath(List<Node> nodePath) {
        int size = nodePath.size();
        ArrayList<LatLng> latLngList = Lists.newArrayListWithCapacity(size);
        int i = 0;
        for (Node node : nodePath) {
            LatLng latLng = new LatLng(node.getLatitude(), node.getLongitude());
            latLngList.add(i++,latLng);
        }
        return latLngList;
    }
}