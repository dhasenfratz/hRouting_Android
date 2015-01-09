//
//  TreeBuilder.java
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

package ch.ethz.tik.graphgenerator.generator;

import java.util.List;

import ch.ethz.tik.graphgenerator.elements.Node;

import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;

/**
 * Builds a kd-tree for a given list of {@link Node}s.
 */
public class TreeBuilder {
    public static final int NULL_NODE = -1;

    private final int[] left;
    private final int[] right;
    private final int rootId;
    private boolean isInitialized = false;

    Ordering<Node> orderingLat = null;
    Ordering<Node> orderingLng = null;

    public TreeBuilder(List<Node> nodes) {
        left = new int[nodes.size()];
        right = new int[nodes.size()];
        isInitialized = true;
        rootId = build(nodes, 0);
        isInitialized = true;
    }

    /**
     * Recursively builds a balanced two dimensional kd-tree and returns the id
     * of the root node.
     *
     * @param nodes
     *            the nodes
     * @param depth
     *            the depth
     * @return the id of the root node.
     */
    private int build(List<Node> nodes, int depth) {
        if (nodes.isEmpty()) {
            return NULL_NODE;
        }
        nodes = getOrdering(depth).sortedCopy(nodes);
        int idxMedian = nodes.size() / 2;
        Node nodeMedian = nodes.get(idxMedian);
        getLefts()[nodeMedian.getId() - 1] = build(nodes.subList(0, idxMedian),
                depth + 1);
        getRights()[nodeMedian.getId() - 1] = build(
                nodes.subList(idxMedian + 1, nodes.size()), depth + 1);
        return nodeMedian.getId();
    }

    /**
     * Gets the ordering function for the current depth (x or y split).
     *
     * @param depth
     *            the depth
     * @return the ordering
     */
    private Ordering<Node> getOrdering(int depth) {
        Ordering<Node> ordering;
        if (depth % 2 == 0) { // X-Split
            initOrderingLat();
            ordering = orderingLat;
        } else { // Y-Split
            initOrderingLng();
            ordering = orderingLng;
        }
        return ordering;
    }

    /**
     * Initializes the field {@link #orderingLng} if it is {@code null}.
     */
    private void initOrderingLng() {
        if (orderingLng == null) {
            orderingLng = new Ordering<Node>() {

                @Override
                public int compare(Node left, Node right) {
                    return Doubles.compare(left.getLongitude(),
                            right.getLongitude());
                }
            };
        }
    }

    /**
     * Initializes the field {@link #orderingLat} if it is {@code null}.
     */
    private void initOrderingLat() {
        if (orderingLat == null) { // Lazy initialization
            orderingLat = new Ordering<Node>() {
                @Override
                public int compare(Node left, Node right) {
                    return Doubles.compare(left.getLatitude(),
                            right.getLatitude());
                }
            };
        }
    }

    public int[] getLefts() {
        checkState();
        return left;
    }

    public int[] getRights() {
        checkState();
        return right;
    }

    public int getRootId() {
        checkState();
        return rootId;
    }

    private void checkState() {
        if (!isInitialized) {
            throw new IllegalStateException(
                    "Tree is not initialized. Ensure build() was called.");
        }
    }
}


