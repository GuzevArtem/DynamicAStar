package edu.kpi.iasa.ai;

import edu.kpi.iasa.ai.gui.DrawZone;
import edu.kpi.iasa.ai.model.Edge;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class Utils {

    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value,max),min);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value,max),min);
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(Math.min(value,max),min);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value,max),min);
    }

    public static double toRange(double value, double min, double max) {
        return value*(max - min) + min;
    }

    public static int toRange(int value, int min, int max) {
        return value*(max - min) + min;
    }

    public static double fromRange(double value, double min, double max) {
        return (value-min)/(max-min);
    }

    public static double fromRange(int value, int min, int max) {
        return (value-min)/(double)(max-min);
    }

    public static double intRangedValueToDoubleRange(int value, int min, int max, double mind, double maxd) {
        return toRange(fromRange(value,min,max), mind, maxd);
    }

    public static int doubleRangedValueToIntRange(double value, double min, double max, int mini, int maxi) {
        return (int) Math.round(toRange(fromRange(value,min,max),mini,maxi));
    }

    public static double lerp(double alpha, double a, double b){
        return b*alpha+(1-alpha)*a;
    }

    public static int lerp(double alpha, int a, int b){
        return (int)(b*alpha+(1-alpha)*a);
    }

    public static Point calcCoordsAbs(JComponent parent, Point2D.Double point){
        //making abs coords relative to parent location
        return new Point (
                Utils.doubleRangedValueToIntRange(point.x,0.0,1.0,
                        0, parent.getWidth()) + parent.getX(),
                Utils.doubleRangedValueToIntRange(point.y,0.0,1.0,
                        0, parent.getHeight()) + parent.getY()
        );
    }

    public static Point calcCoordsAbs(DrawZone parent, Point2D.Double point){
        //making abs coords relative to parent location
        return new Point (
                Utils.doubleRangedValueToIntRange(point.x,0.0,1.0,
                        0, parent.getAZoneSize().width) + parent.getAZoneSize().x,
                Utils.doubleRangedValueToIntRange(point.y,0.0,1.0,
                        0, parent.getAZoneSize().height) + parent.getAZoneSize().y
        );
    }

    public static Point2D.Double calcCoordsRelative(JComponent parent, Point point){
        return new Point2D.Double(
                Utils.intRangedValueToDoubleRange(point.x, parent.getX(), parent.getWidth() + parent.getX(),
                        0.0, 1.0)
                ,
                Utils.intRangedValueToDoubleRange(point.y, parent.getY(), parent.getHeight() + parent.getY(),
                        0.0, 1.0)

        );
    }

    public static Point2D.Double calcCoordsRelative(Rectangle parent, Point point){
        return new Point2D.Double(
                Utils.intRangedValueToDoubleRange(point.x, parent.x, parent.width + parent.x,
                        0.0, 1.0)
                ,
                Utils.intRangedValueToDoubleRange(point.y, parent.y, parent.height + parent.y,
                        0.0, 1.0)

        );
    }

    public static <T extends Comparable<T>>  boolean isInRange(T value, T min, T max) {
        return value.compareTo(min) >= 0 && max.compareTo(value) >= 0;
    }

    public static boolean isInBorder(JComponent area, Point p) {
        return isInBorder(area.getBounds(), p);
    }

    public static boolean isInBorder(Rectangle area, Point p) {
        return area.contains(p);
    }

    public static List<Edge> getVisibleEdgesOfObject(final Point2D.Double from, final List<Point2D.Double> points, final Point2D.Double pivot) {
        if(points.size() == 0) {
            return new ArrayList<>();
        }

        List<Point2D.Double> pool = new ArrayList<>();
        for (Point2D.Double p : points) {
            //if(!p.equals(from)) {//do not create loop edge
                pool.add(new Point2D.Double(p.x, p.y));
            //}
        }
        List<Edge> visibleEdges = new ArrayList<>();
        List<Edge> candidates = new ArrayList<>();

        for(int i = 0; i < pool.size(); i++){
            final Edge e = new Edge(pool.get(i),pool.get((i+1)%pool.size()));
            candidates.add(e);
        }

        for(Edge e : candidates) {
            Point2D.Double point = from;
            if(e.contains(from)) {
                point = Utils.summ(Utils.diff(pivot, from),from);
            }
            if(isEdgeVisible(point, e, candidates)){
                visibleEdges.add(e);
            }
        }

        return visibleEdges;
    }

    public static double dot(final Point2D.Double a, final Point2D.Double b) {
        return a.x*b.x+a.y*b.y;
    }

    public static double cross(final Point2D.Double a, final Point2D.Double b) {
        return a.x * b.y - b.x * a.y;
    }

    public static Point2D.Double diff(final Point2D.Double a, final Point2D.Double b) {
        return new Point2D.Double(a.x - b.x, a.y - b.y);
    }

    public static Point2D.Double summ(final Point2D.Double a,final  Point2D.Double b) {
        return new Point2D.Double(a.x + b.x, a.y + b.y);
    }

    public static Point2D.Double scale(final Point2D.Double a,final  double factor) {
        return new Point2D.Double(a.x * factor, a.y * factor);
    }

    public static double length(final Point2D.Double vec) {
        return Math.sqrt(vec.x*vec.x+vec.y*vec.y);
    }

    public static boolean isIntersect(final Edge l1, final Edge l2) {
        final Point2D.Double res = l1.findIntersection(l2);
        if(res == null) //is parallel
            return (l1.contains(l2.a) || l1.contains(l2.b)); //is overlapped?
        return (l1.contains(res) && l2.contains(res));  //is point in bounds?
    }

    public static boolean isEdgeVisible(final Point2D.Double from, final Edge tested, final Collection<Edge> allEdges) {
        return isEdgeVisible(from, tested, allEdges, false);
    }

    public static boolean isEdgeVisible(final Point2D.Double from, final Edge tested, final Collection<Edge> allEdges, boolean removeIfContainsFrom){
        if(allEdges.size() <= 1) {
            return true;
        }
        List<Edge> pool = new ArrayList<>(allEdges.size());
        for (Edge e : allEdges) {
            if (!removeIfContainsFrom || !e.contains(from)) {
                pool.add(e);
            }
        }
        final Edge testa = new Edge(from, tested.a);
        final Edge testb = new Edge(from, tested.b);
        for(Edge e: pool) {
            if (e != tested && isIntersect(e, testa) || isIntersect(e, testb))
                pool.remove(e);
        }
        if(removeIfContainsFrom) {
            pool.addAll(containsPoint(from, allEdges));
        }
        return pool.contains(tested);
    }

    public static List<Edge> containsPoint(final Point2D.Double point, final Collection<Edge> allEdges) {
        List<Edge> result = new ArrayList<>();
        for(Edge v : allEdges) {
            if(v.contains(point)){
                result.add(v);
            }
        }
        return result;
    }

}
