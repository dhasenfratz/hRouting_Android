package ch.ethz.tik.graphgenerator.algorithms;

import java.util.List;

public interface IRoutingAlgorithm<G,N> {

    public List<N> getShortestPath(G graph, N source, N target,boolean pollution);

}