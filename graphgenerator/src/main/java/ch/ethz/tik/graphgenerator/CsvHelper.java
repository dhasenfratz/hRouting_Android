package ch.ethz.tik.graphgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import ch.ethz.tik.graphgenerator.elements.Adjacency;
import ch.ethz.tik.graphgenerator.elements.Node;

public class CsvHelper {

    private static int skipped = 0;

    public static List<Node> readNodes(String pathToCsv) {
        File file = new File(pathToCsv);
        System.out.println("Input file: " + file.getAbsolutePath());
        List<Node> nodes = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            scanner.nextLine();
            System.out.println("Reading " + pathToCsv);
            while (scanner.hasNextLine()) {
                nodes.add(readNode(scanner.nextLine()));
            }
            System.out.println("Created " + nodes.size() + " nodes.");
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    public static Adjacency[][] readEdges(String pathToCsv,
                                          List<Node> nodes) {
        Adjacency[][] adjacencies = new Adjacency[nodes.size()][];
        File file = new File(pathToCsv);
        int i = 0;
        try {
            Scanner scanner = new Scanner(file);
            scanner.nextLine();
            System.out.println("Reading " + pathToCsv);
            String line = "";
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                Scanner lineScanner = new Scanner(line);
                lineScanner.useDelimiter(",");
                try {
                    int from = lineScanner.nextInt();
                    int to = lineScanner.nextInt();
                    int distance = lineScanner.nextInt();
                    int pollution = lineScanner.nextInt();
                    Adjacency adjacency = Adjacency.create(to, pollution,
                            distance);
                    if (adjacencies[from-1] == null) {
                        Adjacency[] adjacencyArray = new Adjacency[1];
                        adjacencyArray[0] = adjacency;
                        adjacencies[from-1] = adjacencyArray;
                    } else {
                        adjacencies[from-1] = Arrays.copyOf(adjacencies[from-1], adjacencies[from-1].length + 1);
                        adjacencies[from-1][adjacencies[from-1].length -1] = adjacency;
                    }
                    lineScanner.close();
                    i++;
                } catch (InputMismatchException e) {
                    skipped++;
                }
            }
            System.out.println("Created " + i + " edges, skipped "+ skipped +".");
            scanner.close();
        } catch (FileNotFoundException e) {
        }
        return adjacencies;
    }


    private static Node readNode(String line) {
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(",");
        int id = scanner.nextInt();
        double latitude = scanner.nextDouble();
        double longitude = scanner.nextDouble();
        scanner.close();
        return new Node(id, latitude, longitude);
    }
}
