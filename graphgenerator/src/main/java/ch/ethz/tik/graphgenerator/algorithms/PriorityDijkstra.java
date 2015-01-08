//
//  PriorityDijkstra.java
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

package ch.ethz.tik.graphgenerator.algorithms;

import java.util.Comparator;
import java.util.PriorityQueue;

public class PriorityDijkstra extends AbstractDijkstra {

    private PriorityQueue<Integer> unvisited;

    protected void initCustom() {
        unvisited = new PriorityQueue<>(11,
                getDistanceComparator());
        unvisited.add(source.getId());
    }

    protected int getNext() {
        return unvisited.poll();
    }

    protected boolean visitedAllNodes() {
        return unvisited.isEmpty();
    }

    protected int peekNext() {
        return unvisited.peek();
    }

    protected void updatePathTo(int nodeId, int previousNode, int newDistance) {
        unvisited.remove(nodeId);
        previous[nodeId-1] = previousNode;
        potentials[nodeId-1] = newDistance;
        unvisited.add(nodeId);
    }

    private Comparator<Integer> getDistanceComparator() {
        return new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                if (potentials[lhs-1] < potentials[rhs-1]) {
                    return -1;
                } else if (potentials[lhs-1] > potentials[rhs-1]) {
                    return 1;
                }
                return 0;
            }
        };
    }
}
