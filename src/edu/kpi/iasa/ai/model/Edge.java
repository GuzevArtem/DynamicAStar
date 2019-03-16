package edu.kpi.iasa.ai.model;

import edu.kpi.iasa.ai.Utils;

import java.awt.geom.Point2D;
import java.util.Objects;

public class Edge {
    public Point2D.Double a;
    public Point2D.Double b;

    public Edge() {
    }

    public Edge(Edge e) {
        this.a = new Point2D.Double(e.a.x, e.a.y);
        this.b = new Point2D.Double(e.b.x, e.b.y);
    }

    public Edge(Point2D.Double a, Point2D.Double b) {
        this.a = a;
        this.b = b;
    }

    public void setEdge(Point2D.Double a, Point2D.Double b) {
        this.a = a;
        this.b = b;
    }

    public Point2D.Double getA() {
        return a;
    }

    public void setA(Point2D.Double a) {
        this.a = a;
    }

    public Point2D.Double getB() {
        return b;
    }

    public void setB(Point2D.Double b) {
        this.b = b;
    }

    public boolean contains(Point2D.Double point) {
        if(point.x > Math.max(a.x, b.x)
                || point.y > Math.max(a.y, b.y)
                || point.x < Math.min(a.x, b.x)
                || point.y < Math.min(a.y, b.y))
            return false;   //out of bounds
        return Math.abs(Utils.cross(Utils.diff(a,point),Utils.diff(b,point))) <= 0.00001f;
    }

    public boolean isIntersect(Edge e) {
        return Utils.isIntersect(this, e);
    }

    public Point2D.Double findIntersection(Edge l1) {
        double a1 = l1.b.y - l1.a.y;
        double b1 = l1.a.x - l1.b.x;
        double c1 = a1*(l1.a.x) + b1*(l1.a.y);

        // Line CD represented as a2x + b2y = c2
        double a2 = this.b.y - this.a.y;
        double b2 = this.a.x - this.b.x;
        double c2 = a2*(this.a.x)+ b2*(this.a.y);

        double determinant = a1*b2 - a2*b1;

        if (determinant == 0)
        {
            return null;
        }
        else
        {
            double x = (b2*c1 - b1*c2)/determinant;
            double y = (a1*c2 - a2*c1)/determinant;
            return  new Point2D.Double(x, y);
        }
    }

    public double intersectDistance(Edge e) {
        final Point2D.Double E = Utils.diff(b,a);
        final Point2D.Double F = Utils.diff(e.b, e.a);
        final Point2D.Double P = new Point2D.Double(-E.y,E.x);
        final double FP = Utils.dot(F,P);
        if(FP == 0) return Double.POSITIVE_INFINITY;
        return (Utils.dot(Utils.diff(e.a,a),P)/FP);
    }

    public double length() {
        return Utils.length(Utils.diff(a,b));//a.distance(b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return (Objects.equals(a, edge.a)
                    && Objects.equals(b, edge.b))
                ||
                (Objects.equals(b, edge.a)
                        && Objects.equals(a, edge.b));
    }

    @Override
    public int hashCode() {
        return Double.hashCode(a.x) ^ Double.hashCode(a.y) ^ Double.hashCode(b.x) ^ Double.hashCode(b.y);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}
