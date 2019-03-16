package edu.kpi.iasa.ai.gui.drawable;

import edu.kpi.iasa.ai.Utils;

import java.awt.*;

public class ALine implements Drawable{

    public Color diffuse = new Color(90,90,90);

    private APoint from;
    private APoint to;

    private double alpha;

    public Stroke stroke = new BasicStroke(2);

    public ALine(APoint from, APoint to) {
        this.from = from;
        this.to = to;
        this.alpha = 1.0;
    }

    public ALine(APoint from, APoint to, double alpha) {
        this.from = from;
        this.to = to;
        this.alpha = alpha;
    }

    @Override
    public void draw(Graphics graphics) {
        alpha = Utils.clamp(alpha, 0.0, 1.0);

        int r = (diffuse.getRed());
        int g = (diffuse.getGreen());
        int b = (diffuse.getBlue());
        Color c = new Color(r, g, b,
                Utils.lerp(alpha,0,255)
        );
        Graphics2D g2 = (Graphics2D) graphics;
        g2.setStroke(stroke);
        g2.setColor(c);
        g2.drawLine(from.getCoordsAbs().x, from.getCoordsAbs().y,
                    to.getCoordsAbs().x, to.getCoordsAbs().y);

    }

    @Override
    public void update(Graphics g) {
        draw(g);
        //redraw Points to be on top
        from.draw(g);
        to.draw(g);
    }

    public APoint getFromPoint() {
        return from;
    }

    public void setFromPoint(APoint from) {
        this.from = from;
    }

    public APoint getToPoint() {
        return to;
    }

    public void setToPoint(APoint to) {
        this.to = to;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public double length(){
        return Utils.length(Utils.diff(from.coords, to.coords));
    }

}
