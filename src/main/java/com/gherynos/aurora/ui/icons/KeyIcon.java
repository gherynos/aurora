package com.gherynos.aurora.ui.icons;

import javax.swing.Icon;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * This class has been automatically generated using
 * <a href="http://ebourg.github.io/flamingo-svg-transcoder/">Flamingo SVG transcoder</a>.
 */
@SuppressWarnings("PMD")
public class KeyIcon implements Icon {

    /**
     * Paints the transcoded SVG image on the specified graphics context. You
     * can install a custom transformation on the graphics context to scale the
     * image.
     *
     * @param g Graphics context.
     */
    public static void paint(Graphics2D g) {
        GeneralPath shape = null;

        float origAlpha = 1.0f;
        Composite origComposite = g.getComposite();
        if (origComposite instanceof AlphaComposite) {
            AlphaComposite origAlphaComposite = (AlphaComposite)origComposite;
            if (origAlphaComposite.getRule() == AlphaComposite.SRC_OVER) {
                origAlpha = origAlphaComposite.getAlpha();
            }
        }

        java.util.LinkedList<AffineTransform> transformations = new java.util.LinkedList<AffineTransform>();


        //
        transformations.push(g.getTransform());
        g.transform(new AffineTransform(1.6666666f, 0, 0, 1.6666666f, 0, 0));

        // _0

        // _0_0
        shape = new GeneralPath();
        shape.moveTo(16.5, 24.0);
        shape.curveTo(16.244, 24.0, 15.988, 23.902, 15.793, 23.707);
        shape.curveTo(15.402, 23.316, 15.402, 22.684, 15.793, 22.293001);
        shape.lineTo(17.629, 20.457);
        shape.curveTo(17.817999, 20.268002, 17.921999, 20.017, 17.921999, 19.75);
        shape.curveTo(17.921999, 19.483, 17.817999, 19.232, 17.629, 19.043);
        shape.lineTo(9.785, 11.199999);
        shape.curveTo(9.394, 10.808999, 9.394, 10.176999, 9.785, 9.785999);
        shape.curveTo(10.176, 9.3949995, 10.808, 9.3949995, 11.198999, 9.785999);
        shape.lineTo(19.043, 17.629);
        shape.curveTo(19.609, 18.195, 19.921999, 18.949, 19.921999, 19.75);
        shape.curveTo(19.921999, 20.551, 19.609, 21.305, 19.043, 21.872);
        shape.lineTo(17.206999, 23.707);
        shape.curveTo(17.012, 23.902, 16.755999, 24.0, 16.499998, 24.0);
        shape.closePath();

        g.setPaint(new Color(0x78909C));
        g.fill(shape);

        // _0_1
        shape = new GeneralPath();
        shape.moveTo(12.5, 20.0);
        shape.curveTo(12.244, 20.0, 11.988, 19.902, 11.793, 19.707);
        shape.curveTo(11.402, 19.316, 11.402, 18.684, 11.793, 18.293001);
        shape.lineTo(15.043, 15.043001);
        shape.curveTo(15.434, 14.652001, 16.066, 14.652001, 16.457, 15.043001);
        shape.curveTo(16.848001, 15.434001, 16.848001, 16.066002, 16.457, 16.457);
        shape.lineTo(13.207001, 19.707);
        shape.curveTo(13.012001, 19.902, 12.7560005, 20.0, 12.500001, 20.0);
        shape.closePath();

        g.fill(shape);

        // _0_2
        shape = new GeneralPath();
        shape.moveTo(6.5, 0.0);
        shape.curveTo(2.916, 0.0, 0.0, 2.916, 0.0, 6.5);
        shape.curveTo(0.0, 10.084, 2.916, 13.0, 6.5, 13.0);
        shape.curveTo(10.084, 13.0, 13.0, 10.084, 13.0, 6.5);
        shape.curveTo(13.0, 2.9160004, 10.084, 0.0, 6.5, 0.0);
        shape.closePath();

        g.setPaint(new Color(0xFFC107));
        g.fill(shape);

        // _0_3
        shape = new GeneralPath();
        shape.moveTo(1.901, 1.907);
        shape.curveTo(-0.6329999, 4.441, -0.6329999, 8.565, 1.901, 11.099);
        shape.curveTo(4.435, 13.633, 8.559, 13.632999, 11.093, 11.099);
        shape.closePath();

        g.setPaint(new Color(0xDEA806));
        g.fill(shape);

        g.setTransform(transformations.pop()); // _0

    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     *
     * @return The X of the bounding box of the original SVG image.
     */
    public static int getOrigX() {
        return 0;
    }

    /**
     * Returns the Y of the bounding box of the original SVG image.
     *
     * @return The Y of the bounding box of the original SVG image.
     */
    public static int getOrigY() {
        return 0;
    }

    /**
     * Returns the width of the bounding box of the original SVG image.
     *
     * @return The width of the bounding box of the original SVG image.
     */
    public static int getOrigWidth() {
        return 34;
    }

    /**
     * Returns the height of the bounding box of the original SVG image.
     *
     * @return The height of the bounding box of the original SVG image.
     */
    public static int getOrigHeight() {
        return 40;
    }

    /**
     * The current width of this resizable icon.
     */
    int width;

    /**
     * The current height of this resizable icon.
     */
    int height;

    /**
     * Creates a new transcoded SVG image.
     */
    public KeyIcon() {
        this.width = getOrigWidth();
        this.height = getOrigHeight();
    }

    @Override
    public int getIconHeight() {
        return height;
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.pushingpixels.flamingo.api.common.icon.ResizableIcon#setDimension(java.awt.Dimension)
     */
    public void setDimension(Dimension dimension) {
        this.width = dimension.width;
        this.height = dimension.height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(x, y);

        double coef1 = (double) this.width / (double) getOrigWidth();
        double coef2 = (double) this.height / (double) getOrigHeight();
        double coef = Math.min(coef1, coef2);
        g2d.scale(coef, coef);
        paint(g2d);
        g2d.dispose();
    }
}
