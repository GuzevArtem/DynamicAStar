package edu.kpi.iasa.ai.gui.obstacles;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.gui.DrawZone;
import edu.kpi.iasa.ai.model.Edge;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class PointObstacle extends Obstacle {

    private double radius = 0.05;

    public PointObstacle(DrawZone parent) {
        super(parent);
    }

    public PointObstacle(DrawZone parent, Point2D.Double location) {
        super(parent);
        BorderPath bp = new BorderPath();
        bp.addPoint(location.getX(), location.getY());
        this.connectPoints = bp;
    }

    public PointObstacle(DrawZone parent, Point2D.Double location, double radius) {
        super(parent);
        BorderPath bp = new BorderPath();
        bp.addPoint(location.getX(), location.getY());
        this.connectPoints = bp;
        this.radius = radius;
    }

    public PointObstacle(DrawZone parent, BorderPath connectPoints, double radius) {
        super(parent, connectPoints);
        this.radius = radius;
    }

    @Override
    public boolean isInBounds(final Point2D.Double point) {
        return Utils.length(Utils.diff(this.connectPoints.getPath().get(0), point)) < radius;
    }

    @Override
    public boolean isOnBorder(final Point2D.Double point) {
        return Utils.length(Utils.diff(this.connectPoints.getPath().get(0), point)) - radius < 0.0001f;
    }

    @Override
    public List<Edge> getOuterEdges(){
        List<Edge> result = new ArrayList<>(1);
        result.add(new Edge(getPivot(),getPivot()));
        return result;
    }

    @Override
    protected void createPolygonFromPath(){
        //create round polygon
        final int N = 32;
        int npoints = N+1;
        int[] xpoints = new int[npoints];
        int[] ypoints = new int[npoints];
        int i = 0;
        while(i < N){
            final double angle = 2.0 * Math.PI * i / N;
            final double x = radius*Math.cos(angle) + connectPoints.getPath().get(0).x;
            final double y = radius*Math.sin(angle) + connectPoints.getPath().get(0).y;
            Point p = Utils.calcCoordsAbs(parent, new Point2D.Double(x,y));
            xpoints[i] = p.x;
            ypoints[i] = p.y;
            i++;
        }
        xpoints[npoints-1] = xpoints[0];
        ypoints[npoints-1] = ypoints[0];

        polygon = new Polygon(xpoints, ypoints, npoints);
    }

    @Override
    public void move(final Point2D.Double newPivot) {
        connectPoints.getPath().get(0).x = newPivot.x;
        connectPoints.getPath().get(0).y = newPivot.y;
        setPivot(null);
        invalidate();
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
