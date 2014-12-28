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
