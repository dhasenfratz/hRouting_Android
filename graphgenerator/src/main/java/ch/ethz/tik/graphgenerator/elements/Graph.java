//
//  Graph.java
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

package ch.ethz.tik.graphgenerator.elements;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import ch.ethz.tik.graphgenerator.generator.TreeBuilder;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

/**
 * Represents a weighted directed road-graph containing pollution and distance
 * data.
 */
public class Graph implements Externalizable {

    // Graph size
    private int nrOfNodes;
    private int nrAdjacenciesForNode[];

    // Data
    private Node[] nodeArray;
    private Adjacency[][] adjacencies;

    // Values for KD-Tree
    public static final int NULL_NODE = -1;
    private int[] kdLeft;
    private int[] kdRight;
    private int kdRootId;

    /**
     * Creates the graph.
     *
     * @param nodes
     *            the nodes
     * @param adjacencies
     *            the adjacencies
     * @return the graph
     */
    public static Graph create(List<Node> nodes, Adjacency[][] adjacencies) {
        return new Graph(nodes, adjacencies);
    }

    /**
     * Empty constructor needed for de-serialization.
     */
    public Graph() {
    }

    /**
     * Instantiates a new graph and creates the kd-tree.
     *
     * @param nodes
     *            the nodes
     * @param adjacencies
     *            the adjacencies
     */
    private Graph(List<Node> nodes, Adjacency[][] adjacencies) {
        Preconditions.checkArgument(nodes.size() == adjacencies.length,
                "Every node must have at least an empty set of adjacencies");

        nrOfNodes = nodes.size();
        this.nodeArray = new Node[nrOfNodes];
        fillNodeArray(nodes);

        this.adjacencies = adjacencies;
        this.nrAdjacenciesForNode = new int[nrOfNodes];
        initializeAdjacencies(adjacencies);
        initializeKdTree(nodes);
    }

    /**
     * Initialize kd tree.
     *
     * @param nodes
     *            the nodes
     */
    private void initializeKdTree(List<Node> nodes) {
        TreeBuilder builder = new TreeBuilder(nodes);
        kdLeft = builder.getLefts();
        kdRight = builder.getRights();
        kdRootId = builder.getRootId();
    }

    /**
     * Stores the number of Adjacencies for each node and initializes entries
     * with no adjacencies with an empty array.
     *
     * @param adjacencies
     *            the adjacencies
     */
    private void initializeAdjacencies(Adjacency[][] adjacencies) {
        for (int i = 0; i < adjacencies.length; i++) {
            if (adjacencies[i] == null) {
                adjacencies[i] = new Adjacency[0];
            }
            this.nrAdjacenciesForNode[i] = adjacencies[i].length;
        }
    }

    /**
     * Utility method to fill the {@link #nodeArray} with the nodes from a list.
     *
     * @param nodes
     *            the nodes
     */
    private void fillNodeArray(List<Node> nodes) {
        for (Node node : nodes) {
            this.nodeArray[node.getId() - 1] = node;
        }
    }

    /**
     * Gets the adjacencies for a certain node by mapping the {@link Node#id} to
     * the internal representation.
     *
     * @param nodeId
     *            the node id
     * @return the adjacencies for node
     */
    public Adjacency[] getAdjacenciesForNode(int nodeId) {
        return adjacencies[nodeId - 1];
    }

    /**
     * Gets the node inside the graph, which is closest to the given node.
     *
     * @param node
     *            the node
     * @return the closest node to
     */
    public Node getClosestNodeTo(Node node) {
        return findNearestNode(nodeArray[kdRootId - 1], node, 0);
    }

    public Node getNode(int nodeId) {
        return nodeArray[nodeId - 1];
    }

    public int getNrOfNodes() {
        return nodeArray.length;
    }

    /**
     * Recursive Nearest Node Search on kd-tree.
     *
     * @param current the current
     * @param search the search
     * @param depth the depth
     * @return the node
     */
    private Node findNearestNode(Node current, Node search, int depth) {
        // Go down tree
        int direction;
        if (depth % 2 == 0) {
            direction = Doubles.compare(search.latitude, current.latitude);
        } else {
            direction = Doubles.compare(search.longitude, current.longitude);
        }
        int next = direction < 0 ? kdLeft[current.id - 1]
                : kdRight[current.id - 1];
        int other = direction < 0 ? kdRight[current.id - 1]
                : kdLeft[current.id - 1];

        // Go to a leaf node and mark it as best!
        Node best = next == NULL_NODE ? current : findNearestNode(
                nodeArray[next - 1], search, depth + 1);

        // Compare current node to best node
        if (current.squaredDistance(search) < best.squaredDistance(search)) {
            best = current; // Set best as required
        }

        if (other != NULL_NODE) {
            if (current.axisSquaredDistance(search, depth % 2) < best
                    .axisSquaredDistance(search, depth % 2)) {
                Node possibleBest = findNearestNode(nodeArray[other - 1],
                        search, depth + 1);
                if (possibleBest.squaredDistance(search) < best
                        .squaredDistance(search)) {
                    best = possibleBest;
                }
            }
        }
        // Go up tree.
        return best;
    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // Write nr of nodes and adjacencies per node
        out.writeInt(nrOfNodes);
        for (int i = 0; i < nrOfNodes; i++) {
            out.writeInt(nrAdjacenciesForNode[i]);
        }

        // Write node array
        for (int i = 0; i < nrOfNodes; i++) {
            out.writeObject(nodeArray[i]);
        }

        for (int i = 0; i < nrOfNodes; i++) {
            for (int j = 0; j < nrAdjacenciesForNode[i]; j++) {
                out.writeObject(adjacencies[i][j]);
            }
        }

        // Write kd-tree
        out.writeInt(kdRootId);
        for (int left : kdLeft) {
            out.writeInt(left);
        }
        for (int right : kdRight) {
            out.writeInt(right);
        }

    }

    /* (non-Javadoc)
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        this.nrOfNodes = in.readInt();
        this.nrAdjacenciesForNode = new int[nrOfNodes];
        for (int i = 0; i < nrOfNodes; i++) {
            nrAdjacenciesForNode[i] = in.readInt();
        }

        this.nodeArray = new Node[nrOfNodes];
        for (int i = 0; i < nrOfNodes; i++) {
            nodeArray[i] = (Node) in.readObject();
        }

        this.adjacencies = new Adjacency[nrOfNodes][];
        for (int i = 0; i < nrOfNodes; i++) {
            if (nrAdjacenciesForNode[i] == 0) {
                adjacencies[i] = new Adjacency[0];
            } else {
                adjacencies[i] = new Adjacency[nrAdjacenciesForNode[i]];
                for (int j = 0; j < nrAdjacenciesForNode[i]; j++) {
                    adjacencies[i][j] = (Adjacency) in.readObject();
                }
            }
        }

        // Read kd-tree
        this.kdRootId = in.readInt();
        this.kdLeft = new int[nrOfNodes];
        for (int i = 0; i < nrOfNodes; i++) {
            kdLeft[i] = in.readInt();
        }
        this.kdRight = new int[nrOfNodes];
        for (int i = 0; i < nrOfNodes; i++) {
            kdRight[i] = in.readInt();
        }
    }

    public int getScore(List<Node> path, boolean pollution) {
        int sum = 0;
        Node previous = null;

        for (Node current : path) {
            if (current.getId() == SearchNode.SEARCH_NODE_ID) {
                continue;
            }
            if (previous == null) {
                previous = current;
                continue;
            }
            Adjacency[] adjacenciesPrevious = getAdjacenciesForNode(previous.getId());
            boolean found = false;
            for (int i = 0; i < adjacenciesPrevious.length; i++) {
                if (adjacenciesPrevious[i].getTarget() == current.getId()) {
                    found = true;
                    int temp = sum;
                    sum += pollution ? adjacenciesPrevious[i].getPollution()
                            : adjacenciesPrevious[i].getDistance();
                    Preconditions.checkArgument(temp <= sum);
                    break;
                }
            }
            Preconditions.checkArgument(found);
            previous = current;
        }
        return sum;
    }

}
