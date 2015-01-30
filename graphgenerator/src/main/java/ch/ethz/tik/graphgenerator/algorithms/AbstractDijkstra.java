//
//  AbstractDijkstra.java
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

package ch.ethz.tik.graphgenerator.algorithms;

import java.util.Arrays;
import java.util.List;

import ch.ethz.tik.graphgenerator.elements.Node;
import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.elements.Adjacency;

import com.google.common.collect.Lists;

public abstract class AbstractDijkstra implements
        IRoutingAlgorithm<Graph, Node> {

    protected Graph graph;
    protected Node realSource;
    protected Node realTarget;
    protected Node source;
    protected Node target;
    protected boolean pollution;

    protected int[] potentials = null;
    protected int[] previous = null;
    protected boolean[] visited;

    public final List<Node> getShortestPath(Graph graph, Node source,
                                            Node target, boolean pollution) {
        init(graph, source, target, pollution);
        dijkstra();
        return traceBackPath();
    }

    protected final void init(Graph graph, Node realSource, Node realTarget,
                              boolean pollution) {
        // Set the fields
        this.graph = graph;
        this.realSource = realSource;
        this.realTarget = realTarget;
        this.source = graph.getClosestNodeTo(this.realSource);
        this.target = graph.getClosestNodeTo(this.realTarget);
        this.pollution = pollution;

        // Make sure no parameter is null
        if (!isCorrectlyInitialized()) {
            throw new IllegalArgumentException(
                    "Algorithm was initialized with null parameter");
        }

        // Initialize Arrays
        this.potentials = new int[graph.getNrOfNodes()];
        this.previous = new int[graph.getNrOfNodes()];
        this.visited = new boolean[graph.getNrOfNodes()];

        // Initialize values
        Arrays.fill(potentials, Integer.MAX_VALUE);
        Arrays.fill(previous, -1);
        potentials[source.getId()-1] = 0;

        // Special initializations for consumers. This is not very nice but I
        // would prefer not to allow to override this method, since it contains
        // many important initializations.
        initCustom();
    }

    protected final boolean isCorrectlyInitialized() {
        return graph != null && realSource != null && realTarget != null
                && source != null && target != null;
    }

    protected void dijkstra() {
        while (!visitedAllNodes()) {
            if (doDijkstraStep()) {
                return;
            }
        }
    }

    /**
     * A single step of the dijkstra algorithm.
     *
     * @return true, if the target was reached in this step.
     */
    protected boolean doDijkstraStep() {
        int u = getNext();
        if (reachedTarget(u)) {
            return true;
        }
        visited[u-1] = true;
        for (Adjacency v : graph.getAdjacenciesForNode(u)) {
            scanArc(u, v);
        }
        return false;
    }

    protected void scanArc(int u, Adjacency v) {
        if (!visited[v.getTarget()-1]) {
            int edgeWeight = pollution ? v.getPollution() : v.getDistance();
            int dTroughU = potentials[u-1] + edgeWeight;
            if (dTroughU < potentials[v.getTarget()-1]) {
                updatePathTo(v.getTarget(), u, dTroughU);
            }
        }
    }


    abstract protected boolean visitedAllNodes();

    abstract protected int getNext();

    @SuppressWarnings("unused")
    abstract protected int peekNext();

    protected boolean reachedTarget(int u) {
        return u == target.getId();
    }

    abstract protected void updatePathTo(int nodeId, int previousNode,
                                         int newDistance);

    protected void initCustom() {
        // Empty, consumers might override.
    }

    protected List<Node> traceBackPath() {
        List<Node> path = Lists.newLinkedList();
        path.add(realTarget);
        insertNodes(source.getId(), target.getId(), path);
        // Finally, insert closest source and actual source at beginning
        path.add(0, realSource);
        return path;
    }

    /**
     * Iterates over the previous list and inserts new elements first.
     *
     * @param to
     *            id of node where to start iteration
     * @param from
     *            id of node where to stop iteration
     * @param path
     *            with nodes (from, ..., to)
     */
    protected void insertNodes(int from, int to, List<Node> path) {
        while (to != from) {
            path.add(0, graph.getNode(to));
            to = previous[to-1];
        }
        path.add(0, graph.getNode(from));
    }
}