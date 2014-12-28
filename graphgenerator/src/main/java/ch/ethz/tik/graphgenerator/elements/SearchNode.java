package ch.ethz.tik.graphgenerator.elements;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents a node that is not in the graph, but which can be used to find the
 * closest node in the graph and contains a name field, to display in the search
 * suggestions.
 */
public class SearchNode extends Node implements Externalizable {

    /** ID for all search nodes SEARCH_NODE_ID. */
    public static final int SEARCH_NODE_ID = -1;

    private String name;

    public SearchNode() {
    }

    public SearchNode(String name, double latitude, double longitude) {
        super(SEARCH_NODE_ID, latitude, longitude);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeChars(name);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        this.name = in.readLine();
    }

}
