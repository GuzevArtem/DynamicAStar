package edu.kpi.iasa.ai.gui;

import edu.kpi.iasa.ai.Utils;
import edu.kpi.iasa.ai.configuration.ConfiguredObstacle;
import edu.kpi.iasa.ai.gui.drawable.ALine;
import edu.kpi.iasa.ai.gui.drawable.APoint;
import edu.kpi.iasa.ai.gui.obstacles.Obstacle;
import edu.kpi.iasa.ai.gui.obstacles.PointObstacle;
import edu.kpi.iasa.ai.gui.obstacles.RectangleObstacle;
import edu.kpi.iasa.ai.model.ObstacleContainer;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class DrawZone extends JPanel implements MouseInputListener{

    public static Color BACKGROUND_COLOR = Color.lightGray.brighter();
    public static Color BORDER_COLOR = Color.BLACK;
    public static Color GRAPH_USUAL_COLOR = Color.GRAY;
    public static Color PATH_USUAL_COLOR = Color.GREEN;
    public static Color PATH_COLOR = Color.BLUE;
    public static Color SHADOW_COLOR = Color.lightGray;

    private ObstacleContainer obstacleContainer;

    private Map<APoint,APoint> pathTree;
    private Map<APoint,List<APoint>> graph;
    private List<APoint> bestPath;

    private Rectangle drawZoneSize;
    private int padding;

    private Obstacle shadow;
    private ConfiguredObstacle start;
    private ConfiguredObstacle end;

    private int shapeSize;

    private boolean isAnyObstacleUnderCursor = false;

    public DrawZone() {
        this.drawZoneSize = new Rectangle(0,0,500,500);
        init();
    }

    public DrawZone(Rectangle drawZoneSize) {
        this.drawZoneSize = drawZoneSize;
        init();
    }

    public DrawZone(Rectangle drawZoneSize, ObstacleContainer obstacleContainer) {
        this.drawZoneSize = drawZoneSize;
        this.obstacleContainer = obstacleContainer;
        init();
    }

    public DrawZone(int x1, int y1, int x2, int y2) {
        this.drawZoneSize = new Rectangle(x1,y1,x2-x1, y2-y1);
        init();
    }

    public DrawZone(int x1, int y1, int x2, int y2, ObstacleContainer obstacleContainer) {
        this.drawZoneSize = new Rectangle(x1,y1,x2-x1, y2-y1);
        this.obstacleContainer = obstacleContainer;
        init();
    }

    public DrawZone(int padding) {
        this.drawZoneSize = new Rectangle(padding,padding,500,500);
        this.padding = padding;
        init();
    }

    public DrawZone(int padding, ObstacleContainer obstacleContainer) {
        this.drawZoneSize = new Rectangle(padding,padding,500,500);
        this.padding = padding;
        this.obstacleContainer = obstacleContainer;
        init();
    }

    public DrawZone(Rectangle drawZoneSize, int padding) {
        this.drawZoneSize = drawZoneSize;
        this.padding = padding;
        init();
    }

    public DrawZone(Rectangle drawZoneSize, int padding, ObstacleContainer obstacleContainer) {
        this.drawZoneSize = drawZoneSize;
        this.padding = padding;
        this.obstacleContainer = obstacleContainer;
        init();
    }

    public DrawZone(int x1, int y1, int x2, int y2, int padding) {
        this.drawZoneSize = new Rectangle(x1,y1,x2-x1, y2-y1);
        this.padding = padding;
        init();
    }

    public DrawZone(int x1, int y1, int x2, int y2, int padding, ObstacleContainer obstacleContainer) {
        this.drawZoneSize = new Rectangle(x1,y1,x2-x1, y2-y1);
        this.padding = padding;
        this.obstacleContainer = obstacleContainer;
        init();
    }

    public void init() {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.setBounds(drawZoneSize.x-padding,drawZoneSize.y-padding,drawZoneSize.width+padding,drawZoneSize.height+padding);
        this.setSize(drawZoneSize.width+2*padding,drawZoneSize.height+2*padding);
        this.setMinimumSize(this.getSize());
        this.setPreferredSize(this.getSize());
    }

    public void setPoints(final ConfiguredObstacle start, final ConfiguredObstacle end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void paint(Graphics g) {
        paintComponent(g);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setClip(drawZoneSize.x-padding,drawZoneSize.y-padding,drawZoneSize.width+2*padding,drawZoneSize.height+2*padding);

        g.setColor(Color.GRAY);
        g.drawRect(drawZoneSize.x-padding,drawZoneSize.y-padding,drawZoneSize.width+2*padding-1,drawZoneSize.height+2*padding-1);
        g.clearRect(drawZoneSize.x, drawZoneSize.y, drawZoneSize.width, drawZoneSize.height);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(drawZoneSize.x, drawZoneSize.y, drawZoneSize.width, drawZoneSize.height);

        g.setColor(BORDER_COLOR);
        g.drawRect (drawZoneSize.x, drawZoneSize.y, drawZoneSize.width, drawZoneSize.height);
        g.drawRect (drawZoneSize.x-1, drawZoneSize.y-1, drawZoneSize.width+2, drawZoneSize.height+2);//draw outline

        if(obstacleContainer != null) {
            for(ConfiguredObstacle obst : obstacleContainer.getObstacles()) {
                obst.draw(g);
            }
            for(ConfiguredObstacle obst : obstacleContainer.getNonBlocking()) {
                obst.draw(g);
            }
        }
        drawGraph(g);
        drawPathTree(g);
        drawBestPath(g);
        drawPoints(g);

        if(obstacleContainer.isModifiable()){
            drawShadow(g);
        }
    }

    public void drawGraph(Graphics g) {
        if(graph == null) return;
        for(APoint key : graph.keySet()) {
            final List<APoint> values = graph.get(key);
            for(APoint value : values) {
                if (value != null) {
                    //todo:cache this values
                    ALine line = new ALine(key, value);
                    line.diffuse = GRAPH_USUAL_COLOR;
                    line.draw(g);
                }
            }
        }
    }

    public void drawPathTree(Graphics g) {
        if(pathTree == null) return;
        for(APoint key : pathTree.keySet()) {
            final APoint value = pathTree.get(key);
            if(value != null) {
                //todo:cache this values
                ALine line = new ALine(key, value);
                line.stroke = new BasicStroke(3);
                line.diffuse = PATH_USUAL_COLOR;
                line.draw(g);
            }
        }
    }

    public void drawBestPath(Graphics g) {
        if(bestPath == null) return;
        for(int i = 0; i < bestPath.size()-1; i++) {
            //todo:cache this values
            ALine line = new ALine(bestPath.get(i), bestPath.get(i+1));
            line.stroke = new BasicStroke(5);
            line.diffuse = PATH_COLOR;
            line.draw(g);
        }
    }

    public void drawShadow(Graphics g) {
        if(shadow == null) return;
        shadow.obstacleColor = new Color(
                (float)Utils.fromRange(SHADOW_COLOR.getRed(),0,255),
                (float)Utils.fromRange(SHADOW_COLOR.getGreen(),0,255),
                (float)Utils.fromRange(SHADOW_COLOR.getBlue(),0,255),
                (float)0.5);
        shadow.draw(g);
    }


    public void drawPoints(Graphics g) {
        if(start != null) {
            start.getValue().obstacleColor = Color.CYAN;
            start.draw(g);
        }
        if(end != null) {
            end.getValue().obstacleColor = Color.RED;
            end.draw(g);
        }
    }

    public Rectangle getAZoneSize() {
        return drawZoneSize;
    }

    public void setAZoneSize(Rectangle antZoneSize) {
        this.drawZoneSize = antZoneSize;
        obstacleContainer.forceRedraw();
    }

    public ObstacleContainer getObstacleContainer() {
        return obstacleContainer;
    }

    public void setObstacleContainer(ObstacleContainer obstacleContainer) {
        this.obstacleContainer = obstacleContainer;
    }

    public Map<APoint, APoint> getPathTree() {
        return pathTree;
    }

    public void setPathTree(Map<APoint, APoint> pathTree) {
        this.pathTree = pathTree;
    }

    public Map<APoint, List<APoint>> getGraph() {
        return graph;
    }

    public void setGraph(Map<APoint, List<APoint>> graph) {
        this.graph = graph;
    }

    public List<APoint> getBestPath() {
        return bestPath;
    }

    public void setBestPath(List<APoint> bestPath) {
        this.bestPath = bestPath;
    }

    public void clear(){
        graph = null;
        pathTree = null;
        bestPath = null;
    }

    private ConfiguredObstacle getObstacleAt(Point point) {
        for(int i = obstacleContainer.getObstacles().size()-1; i >= 0; i--) {
            if(Utils.isInBorder(
                    obstacleContainer.getObstacles().get(i).getValue().getPolygon().getBounds(),
                    point)) {
                return obstacleContainer.getObstacles().get(i);
            }
        }
        for(int i = obstacleContainer.getNonBlocking().size()-1; i >= 0; i--) {
            if(obstacleContainer.getNonBlocking().get(i).getValue().isInBounds(
                    Utils.calcCoordsRelative(drawZoneSize, point))) {
                return obstacleContainer.getNonBlocking().get(i);
            }
        }
        return null;
    }

    public int getShapeSize() {
        return shapeSize;
    }

    public void setShapeSize(int shapeSize) {
        this.shapeSize = shapeSize;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(!e.isConsumed()) {
            final Point point = e.getPoint();
            if (Utils.isInBorder(drawZoneSize, e.getPoint()) && obstacleContainer.isModifiable()) {
                ConfiguredObstacle obst = getObstacleAt(point);
                if(obst != null) {
                    obstacleContainer.getObstacles().remove(obst);
                    mouseMoved(e);
                    this.repaint(drawZoneSize.getBounds());
                    return;
                }

                final Point2D.Double relative = Utils.calcCoordsRelative(drawZoneSize, point);
                final double size = Utils.clamp(Utils.fromRange(shapeSize, 1, 100), Double.MIN_NORMAL, 1.0);
                obstacleContainer.addObstacle(
                        new ConfiguredObstacle(
                                new RectangleObstacle(this,
                                        new Rectangle2D.Double(relative.x - size / 2, relative.y - size / 2, size, size)
                                    )
                            )
                    );
                mouseMoved(e);
            }
            this.repaint(drawZoneSize.getBounds());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        shadow = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(!e.isConsumed()) {
            final Point point = e.getPoint();
            if (Utils.isInBorder(drawZoneSize, e.getPoint()) && obstacleContainer.isModifiable()) {
                ConfiguredObstacle obst = getObstacleAt(point);
                if (obst != null) {
                    e.consume();
                    final Point2D.Double relative = Utils.calcCoordsRelative(drawZoneSize, point);
                    obst.getValue().move(relative);
                    this.repaint();
                }
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        final Point point = e.getPoint();
        if (Utils.isInBorder(drawZoneSize, e.getPoint()) && obstacleContainer.isModifiable()) {
            //draw obst shadow
            final Point2D.Double relative = Utils.calcCoordsRelative(drawZoneSize, point);
            final double size = Utils.clamp(Utils.fromRange(shapeSize, 1, 100), Double.MIN_NORMAL, 1.0);
            shadow = new RectangleObstacle(this,
                    new Rectangle2D.Double(relative.x - size / 2, relative.y - size / 2, size, size)
            );

            ConfiguredObstacle obst = getObstacleAt(point);
            if (obst != null) {
                for(ConfiguredObstacle o : obstacleContainer.getObstacles()){
                    o.getValue().obstacleBorderColor = Color.BLACK;
                }
                for(ConfiguredObstacle o : obstacleContainer.getNonBlocking()){
                    o.getValue().obstacleBorderColor = Color.BLACK;
                }
                obst.getValue().obstacleBorderColor = Color.BLUE;
                isAnyObstacleUnderCursor = true;
                shadow = new RectangleObstacle(this, obst.getValue().getConnectPoints());
                shadow.obstacleBorderColor = Color.BLUE;
            } else if(isAnyObstacleUnderCursor) {
                for(ConfiguredObstacle o : obstacleContainer.getObstacles()){
                    o.getValue().obstacleBorderColor = Color.BLACK;
                }
                for(ConfiguredObstacle o : obstacleContainer.getNonBlocking()){
                    o.getValue().obstacleBorderColor = Color.BLACK;
                }
                isAnyObstacleUnderCursor = false;
            }
        } else {
            shadow = null;
        }
        this.repaint();
    }
}
