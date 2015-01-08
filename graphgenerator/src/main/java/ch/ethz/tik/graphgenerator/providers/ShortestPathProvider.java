//
//  ShortestPathProvider.java
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

package ch.ethz.tik.graphgenerator.providers;

import java.util.List;

import ch.ethz.tik.graphgenerator.elements.Node;
import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.algorithms.IRoutingAlgorithm;
import ch.ethz.tik.graphgenerator.algorithms.BidirectionalSearch;

import com.google.common.base.Preconditions;

public class ShortestPathProvider {

    public static final Class<? extends IRoutingAlgorithm<Graph, Node>> ROUTING_ALGORITHM = BidirectionalSearch.class;

    public static List<Node> between(Graph graph, Node source, Node target, boolean pollution) {
        return between(ROUTING_ALGORITHM, graph, source, target, pollution);
    }

    public static List<Node> between(Class<? extends IRoutingAlgorithm<Graph, Node>> clazz , Graph graph, Node source, Node target, boolean pollution) {
        Preconditions.checkNotNull(graph);
        IRoutingAlgorithm<Graph, Node> routingAlgorithm;
        try {
            routingAlgorithm = clazz.newInstance();
            return routingAlgorithm.getShortestPath(graph, source, target,
                    pollution);
        } catch (InstantiationException | IllegalAccessException e) {
            System.out.println("Error ShortestPath: Could not get instance");
        }
        return null;
    }

}
