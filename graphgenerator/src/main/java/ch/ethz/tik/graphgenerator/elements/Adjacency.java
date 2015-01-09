//
//  Adjacency.java
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

package ch.ethz.tik.graphgenerator.elements;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Adjacency implements Externalizable {

    private int target;
    private int pollution;
    private int distance;

    public static Adjacency create(int target, int pollution, int distance) {
        return new Adjacency(target, pollution, distance);
    }

    public Adjacency() {
    }

    public Adjacency(int target, int pollution, int distance) {
        this.setTarget(target);
        this.pollution = pollution;
        this.distance = distance;
    }

    public int getPollution() {
        return pollution;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(getTarget());
        out.writeInt(pollution);
        out.writeInt(distance);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        this.setTarget(in.readInt());
        this.pollution = in.readInt();
        this.distance = in.readInt();
    }

    @Override
    public String toString() {
        return getTarget() + " " + pollution + " " + distance;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }
}
