//
//  UnreachableNodeProvider.java
//  hRouting
//
//  Created by Ivo de Concini, David Hasenfratz on 23/01/15.
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

import com.google.common.collect.Sets;

import java.util.Set;

import ch.ethz.tik.graphgenerator.algorithms.PriorityDijkstra;
import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.elements.Node;

/**
 *
 */
public class UnreachableNodeProvider {

    public static Set<Integer> getUnreachableNodeIds(Graph graph, Node fullyConnected) {
        Set<Integer> unreachable = Sets.newHashSet();
        FullPriorityDijkstra dijkstra = new FullPriorityDijkstra();
        dijkstra.getShortestPath(graph,fullyConnected,fullyConnected,true);
        int nrOfNodes = graph.getNrOfNodes();
        boolean[] visited = dijkstra.getVisited();
        for (int i = 0; i < nrOfNodes; i++) {
            if(!visited[i]) {
                unreachable.add(graph.getNode(i+1).getId());
            }
        }
        return unreachable;
    }

    private static class FullPriorityDijkstra extends PriorityDijkstra {

        public boolean[] getVisited() {
            return visited;
        }


        @Override
        protected boolean reachedTarget(int u) {
            return false;
        }
    }
}
