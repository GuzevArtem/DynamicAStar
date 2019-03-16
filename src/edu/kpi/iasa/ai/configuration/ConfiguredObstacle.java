package edu.kpi.iasa.ai.configuration;

import edu.kpi.iasa.ai.gui.drawable.Drawable;
import edu.kpi.iasa.ai.gui.obstacles.Obstacle;

import java.awt.*;
import java.awt.geom.Point2D;

public class ConfiguredObstacle implements Configured<Obstacle>, Drawable {

    private static long KEY = 0;

    private String uniqueKey = Attribute.getObstacleAttribute(KEY++);
    private boolean isModifiable = true;
    private Obstacle obstacle;

    public ConfiguredObstacle(Obstacle obstacle) {
        this.obstacle = obstacle;
    }

    public boolean isInBounds(final Point2D.Double point) {
        return obstacle.isInBounds(point);
    }

    public boolean isOnBorder(final Point2D.Double point) {
        return obstacle.isOnBorder(point);
    }

    @Override
    public void setValue(Obstacle value) {
        this.obstacle = value;
    }

    @Override
    public Obstacle getValue() {
        return obstacle;
    }

    @Override
    public void setModifiable(boolean state) {
        this.isModifiable = state;
    }

    @Override
    public boolean isModifiable() {
        return isModifiable;
    }

    @Override
    public void draw(Graphics g) {
        obstacle.draw(g);
    }

    @Override
    public void update(Graphics g) {
        draw(g);
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

}
