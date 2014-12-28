package ch.ethz.tik.graphgenerator;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ch.ethz.tik.graphgenerator.elements.Node;
import ch.ethz.tik.graphgenerator.elements.Graph;
import ch.ethz.tik.graphgenerator.elements.Adjacency;
import ch.ethz.tik.graphgenerator.elements.Route;
import ch.ethz.tik.graphgenerator.elements.SearchNode;
import ch.ethz.tik.graphgenerator.providers.GeocodeProvider;
import ch.ethz.tik.graphgenerator.providers.ShortestPathProvider;
import ch.ethz.tik.graphgenerator.util.GraphSerializerUtil;
import ch.ethz.tik.graphgenerator.algorithms.FullDijkstra;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GraphGenerator {

    public static final String SERIALIZED_FILE_GRAPH = "graphgenerator/res/graph.ser";
    public static final String SERIALIZED_FILE_ROUTES = "graphgenerator/res/routes.ser";
    private static final Node FULLY_CONNECTED_NODE = new Node(0,
            47.41967172599581, 8.584416169703587);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Graph graph = generateGraph();
        stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("Reading and creating graph took: " + stopwatch);

        createExampleRoutes(graph);

        stopwatch = Stopwatch.createStarted();
        GraphSerializerUtil.serializeGraph(graph, SERIALIZED_FILE_GRAPH);
        stopwatch.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("Serialized graph in " + stopwatch);
    }

    public static void createExampleRoutes(Graph graph) {

        List<Route> exRoutes = Lists.newArrayList();
        SearchNode nodeFrom, nodeTo;

        for(int i = 0; i < 7; ++i) {
            if (i == 0) {
                nodeFrom = GeocodeProvider.getGeocode("Wipkingen");
                nodeTo = GeocodeProvider.getGeocode("Limmatplatz");
            } else if (i == 1) {
                nodeFrom = GeocodeProvider.getGeocode("Werdinsel");
                nodeTo = GeocodeProvider.getGeocode("Wiedikon");
            } else if (i == 2) {
                nodeFrom = GeocodeProvider.getGeocode("Affoltern Zurich City");
                nodeTo = GeocodeProvider.getGeocode("Fluntern");
            } else if (i == 3) {
                nodeFrom = GeocodeProvider.getGeocode("Wipkingen");
                nodeTo = GeocodeProvider.getGeocode("Zurich");
            } else if (i == 4) {
                nodeFrom = GeocodeProvider.getGeocode("Hirzenbach");
                nodeTo = GeocodeProvider.getGeocode("Friesenberg");
            } else if (i == 5) {
                nodeFrom = GeocodeProvider.getGeocode("Opfikon");
                nodeTo = GeocodeProvider.getGeocode("Wollishofen");
            } else {
                nodeFrom = GeocodeProvider.getGeocode("Escherwyssplatz");
                nodeTo = GeocodeProvider.getGeocode("ETH Zurich");
            }

            if (nodeFrom == null || nodeTo == null) {
                System.out.println("Error getting location from the Google Geocoding");
                return;
            }

            List<Node> healthiestNodes = ShortestPathProvider.between(graph, nodeTo,
                    nodeFrom, true);
            List<Node> shortestNodes = ShortestPathProvider.between(graph, nodeTo,
                    nodeFrom, false);
            System.out.println(nodeFrom + " - " + nodeTo + ": healthiest route length " +
                    healthiestNodes.size());
            System.out.println(nodeFrom + " - " + nodeTo + ": shortest route length " + shortestNodes.size());

            // Compute scores
            List<Integer> scoresH = Lists.newArrayList();
            scoresH.add(0, graph.getScore(healthiestNodes, true));
            scoresH.add(0, graph.getScore(healthiestNodes, false));
            List<Integer> scoresS = Lists.newArrayList();
            scoresH.add(0, graph.getScore(shortestNodes, true));
            scoresH.add(0, graph.getScore(shortestNodes, false));

            Route route = new Route.Builder(nodeFrom, nodeTo)
                    .hOptPath(healthiestNodes)
                    .shortestPath(shortestNodes)
                    .hOptPathScores(graph.getScore(healthiestNodes, true), graph.getScore(healthiestNodes, false))
                    .shortestPathScores(graph.getScore(shortestNodes, true), graph.getScore(shortestNodes, false))
                    .build();
            exRoutes.add(0, route);
        }
        // Serialize routes
        try {
            FileOutputStream fileOut = new FileOutputStream(SERIALIZED_FILE_ROUTES);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(exRoutes);
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Serialized " + exRoutes.size() + " example routes");
    }

    public static Graph generateGraph() {
        List<Node> nodes = CsvHelper.readNodes("graphgenerator/res/coordinates.csv");
        Adjacency[][] adjacencies = CsvHelper.readEdges("graphgenerator/res/edges.csv", nodes);
        Graph graph = purgeUnreachableNodes(Graph.create(nodes, adjacencies));
        return graph;
    }

    private static Graph purgeUnreachableNodes(Graph graph) {
        Set<Integer> unreachable = new FullDijkstra().getUnreachableNodes(
                graph, FULLY_CONNECTED_NODE);
        Map<Integer, Integer> oldToNew = Maps.newHashMap();

        int newId = 1;
        for (int i = 1; i <= graph.getNrOfNodes(); i++) {
            if (!unreachable.contains(i)) {
                oldToNew.put(i, newId++);
            }
        }

        List<Node> newNodes = Lists.newLinkedList();
        Adjacency[][] adjacencies = new Adjacency[oldToNew.size()][];
        for (int i = 1; i <= graph.getNrOfNodes(); i++) {
            if (oldToNew.containsKey(i)) {
                Node oldNode = graph.getNode(i);
                Node newNode = new Node(oldToNew.get(i), oldNode.getLatitude(),
                        oldNode.getLongitude());
                Adjacency[] oldAdjacencies = graph.getAdjacenciesForNode(i);
                List<Adjacency> newAdjacencies = Lists.newArrayList();
                for (Adjacency oldAdjacency : oldAdjacencies) {
                    if (oldToNew.containsKey(oldAdjacency.getTarget())) {
                        Adjacency newAdjacency = Adjacency.create(
                                oldToNew.get(oldAdjacency.getTarget()),
                                oldAdjacency.getPollution(),
                                oldAdjacency.getDistance());
                        newAdjacencies.add(newAdjacency);
                    }
                }
                newNodes.add(newNode);
                adjacencies[newNode.getId() - 1] = newAdjacencies
                        .toArray(new Adjacency[newAdjacencies.size()]);
            }
        }
        System.out.println("Purged " + (graph.getNrOfNodes() - newNodes.size()) + " unreachable nodes.");
        return Graph.create(newNodes, adjacencies);
    }
}