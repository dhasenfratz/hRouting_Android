package ch.ethz.tik.hrouting.util;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.tik.graphgenerator.elements.Node;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;

public final class ConversionUtil {

    private ConversionUtil() {
    }

    public static final ArrayList<LatLng> getLatLngPath(List<Node> nodePath) {
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