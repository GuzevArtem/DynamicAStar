package edu.kpi.iasa.ai.gui.obstacles;

import edu.kpi.iasa.ai.gui.DrawZone;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RectangleObstacle extends Obstacle {

    public RectangleObstacle(DrawZone parent, Rectangle2D rect) {
        super(parent);
        BorderPath bp = new BorderPath();
        //CCW to screen axis
        bp.addPoint(rect.getX(), rect.getY());
        bp.addPoint(rect.getX(), rect.getY() + rect.getWidth());
        bp.addPoint(rect.getX()+ rect.getHeight(), rect.getY() + rect.getWidth());
        bp.addPoint(rect.getX()+ rect.getHeight(), rect.getY());
        this.connectPoints = bp;
    }

    public RectangleObstacle(DrawZone parent, BorderPath connectPoints) {
        super(parent, connectPoints);
    }

    @Override
    public boolean isInBounds(final Point2D.Double point) {
        Point2D.Double TopLeft = new Point2D.Double(); //(min x, min y)
        Point2D.Double BottomRight = new Point2D.Double();//(max x, max y)
        getBoundPoints(TopLeft, BottomRight);

        return point.x > TopLeft.x && point.x < BottomRight.x
                && point.y > TopLeft.y && point.y < BottomRight.y;
    }

    @Override
    public boolean isOnBorder(final Point2D.Double point) {
        Point2D.Double TopLeft = new Point2D.Double(); //(min x, min y)
        Point2D.Double BottomRight = new Point2D.Double();//(max x, max y)
        getBoundPoints(TopLeft, BottomRight);

        return point.x >= TopLeft.x && point.x <= BottomRight.x
                && point.y >= TopLeft.y && point.y <= BottomRight.y
                && //and NOT inside
                !(point.x > TopLeft.x && point.x < BottomRight.x
                && point.y > TopLeft.y && point.y < BottomRight.y);
    }

    private void getBoundPoints(/*out*/Point2D.Double TopLeft, /*out*/Point2D.Double BottomRight) {
        assert TopLeft != null;
        assert BottomRight != null;
        //(min x, min y)
        TopLeft.x = connectPoints.getPath().get(0).x;
        TopLeft.y = connectPoints.getPath().get(0).y;
        //(max x, max y)
        BottomRight.x = connectPoints.getPath().get(0).x;
        BottomRight.y = connectPoints.getPath().get(0).y;
        for(int i = 1; i < connectPoints.getPath().size(); i++) {
            if(TopLeft.x > connectPoints.getPath().get(i).x
                    && TopLeft.y > connectPoints.getPath().get(i).y) {
                TopLeft.x = connectPoints.getPath().get(i).x;
                TopLeft.y = connectPoints.getPath().get(i).y;
            }
            else if(BottomRight.x < connectPoints.getPath().get(i).x
                    && BottomRight.y < connectPoints.getPath().get(i).y) {
                BottomRight.x = connectPoints.getPath().get(i).x;
                BottomRight.y = connectPoints.getPath().get(i).y;
            }
        }
    }
}
