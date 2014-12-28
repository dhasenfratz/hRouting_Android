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
