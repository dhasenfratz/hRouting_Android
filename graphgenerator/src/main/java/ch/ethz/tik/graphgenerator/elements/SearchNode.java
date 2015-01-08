//
//  SearchNode.java
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
        out.writeUTF(name);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        this.name = in.readUTF();
    }

}
