package ch.ethz.tik.graphgenerator.algorithms;

import java.util.Collections;
import java.util.List;

import ch.ethz.tik.graphgenerator.elements.Adjacency;
import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.elements.Node;

import com.google.common.collect.Lists;

public class BidirectionalSearch implements IRoutingAlgorithm<Graph, Node> {

    protected BidirectionalDijkstra fromSource;
    protected BidirectionalDijkstra fromTarget;

    private int intersectionId;
    private int minPathScore;

    @Override
    public List<Node> getShortestPath(Graph graph, Node source, Node target,
                                      boolean pollution) {
        init(graph, source, target, pollution);
        findOptimalPath();
        return makeNodeList(source, target);
    }

    private void init(Graph graph, Node source, Node target, boolean pollution) {
        fromSource = new BidirectionalDijkstra(true);
        fromTarget = new BidirectionalDijkstra(false);
        intersectionId = -1;
        minPathScore = Integer.MAX_VALUE;

        fromSource.init(graph, source, target, pollution);
        fromTarget.init(graph, target, source, pollution);
    }

    protected void findOptimalPath() {
        while (true) {
            if (fromSource.doDijkstraStep() || fromTarget.doDijkstraStep()) {
                break;
            }
        }
    }

    protected List<Node> makeNodeList(Node source, Node target) {
        if (intersectionId == -1) {
            throw new IllegalStateException(
                    "Intersection must be set before computing optimal path.");
        }
        List<Node> path = Lists.newLinkedList();
        List<Node> pathFromSource = fromSource.traceBackPath();
        List<Node> pathFromTarget = fromTarget.traceBackPath();

        // Remove center from target-list and reverse it to obtain list
        // [center+1 ... target]
        pathFromTarget.remove(pathFromTarget.get(pathFromTarget.size() - 1));
        Collections.reverse(pathFromTarget);

        // Put everything together, including start and target nodes, that are
        // not in the graph.
        path.add(source);
        path.addAll(pathFromSource);
        path.addAll(pathFromTarget);
        path.add(target);
        return path;
    }

    class BidirectionalDijkstra extends PriorityDijkstra {

        final boolean isFromSource;

        private BidirectionalDijkstra(boolean isFromSource) {
            this.isFromSource = isFromSource;
        }

        @Override
        protected boolean reachedTarget(int w) {
            return potentials[w - 1]
                    + other().potentials[other().peekNext() - 1] >= minPathScore;
        }

        @Override
        protected void scanArc(int u, Adjacency v) {
            if (!visited[v.getTarget() - 1] && other().visited[v.getTarget() - 1]) {
                int edgeWeight = pollution ? v.getPollution() : v.getDistance();
                int newPathScore = potentials[u - 1] + edgeWeight
                        + +other().potentials[v.getTarget() - 1];
                if (newPathScore < minPathScore) {
                    minPathScore = newPathScore;
                    intersectionId = v.getTarget();
                }
            }
            super.scanArc(u, v);
        }

        @Override
        protected List<Node> traceBackPath() {
            List<Node> path = Lists.newLinkedList();
            insertNodes(source.getId(), intersectionId, path);
            return path;
        }

        private BidirectionalDijkstra other() {
            return (isFromSource ? fromTarget : fromSource);
        }
    }

}
