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
        Comparator<Integer> distanceComparator = new Comparator<Integer>() {
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
        return distanceComparator;
    }
}
