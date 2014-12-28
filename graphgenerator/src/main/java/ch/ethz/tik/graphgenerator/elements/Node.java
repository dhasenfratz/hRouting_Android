package ch.ethz.tik.graphgenerator.elements;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Node implements Externalizable {

    public static final int POINT_DIM = 2;

    protected int id;
    protected double latitude;
    protected double longitude;
    private double point[] = new double[POINT_DIM];

    public Node() {
    }

    public Node(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        setPoint();
    }

    private void setPoint() {
        point[0] = cos(toRadians(latitude)) * cos(toRadians(longitude));
        point[1] = cos(toRadians(latitude)) * sin(toRadians(longitude));
    }

    @Override
    public String toString() {
        return "[id:" + id + " lat: " + latitude + " long: " + longitude + "]";
    }

    public Double squaredDistance(Object other) {
        Node location = (Node) other;
        double x = this.point[0] - location.point[0];
        double y = this.point[1] - location.point[1];
        return (x * x) + (y * y);
    }

    public Double axisSquaredDistance(Object other, Integer axis) {
        Node location = (Node) other;
        Double distance = point[axis] - location.point[axis];
        return distance * distance;
    }

    public int getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        for (int i = 0; i < POINT_DIM; i++) {
            out.writeDouble(point[i]);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        this.id = in.readInt();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        for (int i = 0; i < POINT_DIM; i++) {
            point[i] = in.readDouble();
        }

    }
}
