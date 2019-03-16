package edu.kpi.iasa.ai.model;

import edu.kpi.iasa.ai.Utils;

import java.awt.geom.Point2D;
import java.util.Objects;

public class MapNode {

    private Point2D.Double location;
    private double distanceToStart;
    private double predictedDistance;

    public MapNode() {
    }

    public MapNode(Point2D.Double location) {
        this.location = location;
    }

    public void setLocation(Point2D.Double location) {
        this.location = location;
    }

    public Point2D.Double getLocation() {
        return location;
    }

    public void setDistanceToStart(double distanceToStart) {
        this.distanceToStart = distanceToStart;
    }

    public double getDistanceToStart() {
        return distanceToStart;
    }

    public double calculateDistance(MapNode neighbor) {
        return Utils.length(Utils.diff(this.getLocation(),neighbor.getLocation()));
        //return neighbor.getLocation().distance(neighbor.getLocation());
    }

    public void setPredictedDistance(double predictedDistance) {
        this.predictedDistance = predictedDistance;
    }

    public double getPredictedDistance() {
        return predictedDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapNode mapNode = (MapNode) o;
        return Objects.equals(location, mapNode.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @Override
    public String toString() {
        return "MapNode{" +
                "location=" + location +
                '}';
    }
}
