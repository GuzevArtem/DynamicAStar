package edu.kpi.iasa.ai.gui.obstacles;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.gui.DrawZone;
import edu.kpi.iasa.ai.gui.drawable.Drawable;
import edu.kpi.iasa.ai.model.Edge;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Obstacle implements Drawable {
    protected BorderPath connectPoints;
    protected Polygon polygon;

    public Color obstacleBorderColor = Color.BLACK;
    public Color obstacleColor = new Color(195,107,0);

    protected DrawZone parent;

    public Obstacle(DrawZone parent) {
        this.parent = parent;
    }

    public Obstacle(DrawZone parent, BorderPath connectPoints) {
        this.parent = parent;
        this.connectPoints = connectPoints;
        invalidate();
    }

    @Override
    public void draw(Graphics g) {
        if(polygon == null) {
            createPolygonFromPath();
        }
        g.setColor(obstacleColor);
        g.fillPolygon(polygon);
        g.setColor(obstacleBorderColor);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
    }

    @Override
    public void update(Graphics g) {
        draw(g);
    }

    public void invalidate(){
        polygon = null;
    }

    public Point2D.Double getPivot(){
        if(connectPoints.pivot == null) {
            calcPivot();
        }
        return connectPoints.pivot;
    }

    public List<Edge> getVisibleEdges(Point2D.Double from) {
        return Utils.getVisibleEdgesOfObject(from, getConnectPoints().getPath(), getPivot());
    }

    public BorderPath getConnectPoints() {
        return connectPoints;
    }

    public void setConnectPoints(BorderPath connectPoints) {
        this.connectPoints = connectPoints;
        invalidate();
    }

    public Polygon getPolygon() {
        if(polygon == null) createPolygonFromPath();
        return polygon;
    }

    public void move(Point2D.Double newPivot){
        invalidate();
        connectPoints.move(newPivot);
    }

    public void setPivot(Point2D.Double newPivot){
        this.connectPoints.pivot = newPivot;
    }

    public List<Edge> getOuterEdges(){
        List<Edge> result = new ArrayList<>();
        final List<Point2D.Double> ps = connectPoints.getPath();
        for(int i = 0; i < ps.size(); i++) {
            result.add(new Edge(ps.get(i), ps.get((i+1)%ps.size())));
        }
        return result;
    }

    public List<Edge> getInnerEdges() {
        List<Edge> result = new ArrayList<>();
        final List<Point2D.Double> ps = connectPoints.getPath();
        for(Point2D.Double p1 : ps) {
            for (Point2D.Double p2 : ps) {
                result.add(new Edge(p1, p2));
            }
        }
        result.removeAll(getOuterEdges());
        return result;
    }

    protected void createPolygonFromPath(){
        polygon = connectPoints.createPolygonFromPath();
    }

    private void calcPivot() {
        if(connectPoints!= null) {
            connectPoints.setPivot(
                    Utils.calcCoordsRelative(parent,
                        new Point((int)getPolygon().getBounds().getCenterX(),
                                (int)getPolygon().getBounds().getCenterY())
                    )
                );
        }
    }

    public boolean isInBounds(final Point2D.Double point) {
        return false;
    }

    public boolean isOnBorder(final Point2D.Double point) {
        return false;
    }

    public class BorderPath {
        private List<Point2D.Double> path = new ArrayList<>();

        private Point2D.Double pivot;

        BorderPath() {
        }

        public BorderPath(List<Point2D.Double> path) {
            this.path = path;
            calcPivot();
        }

        public void addPoint(Point2D.Double point) {
            path.add(point);
            calcPivot();
        }

        public void addPoint(double x, double y) {
            path.add(new Point2D.Double(x, y));
            calcPivot();
        }

        public boolean contains(Point2D.Double point) {
            return path.contains(point);
        }

        public boolean contains(double x, double y) {
            return path.contains(new Point2D.Double(x, y));
        }

        public List<Point2D.Double> getPath() {
            return path;
        }

        public void setPath(List<Point2D.Double> path) {
            this.path = path;
            calcPivot();
        }

        private void setPivot(double x, double y) {
            setPivot(new Point2D.Double(x,y));
        }

        private void setPivot(Point2D.Double newPivot) {
            pivot = newPivot;
        }

        public void move(Point2D.Double newPivot) {
            if(pivot == null) {
                calcPivot();
            }
            final Point2D.Double delta = new Point2D.Double(newPivot.x - pivot.x, newPivot.y - pivot.y);
            for (Point2D.Double p : path) {
                p.x += delta.x;
                p.y += delta.y;
            }
            pivot = newPivot;
        }

        private Polygon createPolygonFromPath() {
            int npoints = path.size()+1;
            int[] xpoints = new int[npoints];
            int[] ypoints = new int[npoints];

            int i = 0;
            for (Point2D.Double pd : path) {
                Point p = Utils.calcCoordsAbs(parent, pd);
                xpoints[i] = p.x;
                ypoints[i] = p.y;
                i++;
            }
            Point p = Utils.calcCoordsAbs(parent, path.get(0));
            xpoints[npoints-1] = p.x;
            ypoints[npoints-1] = p.y;
            return new Polygon(xpoints, ypoints, npoints);
        }
    }
}
