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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * This class has been automatically generated using
 * <a href="http://ebourg.github.io/flamingo-svg-transcoder/">Flamingo SVG transcoder</a>.
 */
@SuppressWarnings("PMD")
public class FileIcon implements Icon {

    /**
     * Paints the transcoded SVG image on the specified graphics context. You
     * can install a custom transformation on the graphics context to scale the
     * image.
     *
     * @param g Graphics context.
     */
    public static void paint(Graphics2D g) {
        Shape shape = null;

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
        g.transform(new AffineTransform(0.0781221f, 0, 0, 0.0781221f, 0, 0));

        // _0

        // _0_0
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(256.019, 394.679);
        ((GeneralPath) shape).curveTo(256.138, 330.494, 300.24503, 274.75998, 362.686, 259.895);
        ((GeneralPath) shape).lineTo(362.686, 10.679);
        ((GeneralPath) shape).curveTo(362.686, 4.788, 357.91, 0.012000084, 352.019, 0.012000084);
        ((GeneralPath) shape).lineTo(138.686, 0.012000084);
        ((GeneralPath) shape).curveTo(132.795, 0.012000084, 128.01901, 4.788, 128.01901, 10.679);
        ((GeneralPath) shape).lineTo(128.01901, 128.012);
        ((GeneralPath) shape).lineTo(10.686, 128.012);
        ((GeneralPath) shape).curveTo(4.795, 128.012, 0.019000053, 132.788, 0.019000053, 138.67899);
        ((GeneralPath) shape).lineTo(0.019000053, 458.679);
        ((GeneralPath) shape).curveTo(0.019000053, 464.56998, 4.795, 469.34598, 10.686, 469.34598);
        ((GeneralPath) shape).lineTo(278.035, 469.34598);
        ((GeneralPath) shape).curveTo(263.667, 447.092, 256.023, 421.167, 256.019, 394.679);
        ((GeneralPath) shape).closePath();

        g.setPaint(new Color(0xCFD8DC));
        g.fill(shape);

        // _0_1
        shape = new Ellipse2D.Double(277.3529968261719, 277.3459777832031, 234.66600036621094, 234.66600036621094);
        g.setPaint(new Color(0x2196F3));
        g.fill(shape);

        // _0_2

        // _0_2_0
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(437.352, 405.345);
        ((GeneralPath) shape).lineTo(352.01898, 405.345);
        ((GeneralPath) shape).curveTo(346.128, 405.345, 341.352, 400.569, 341.352, 394.678);
        ((GeneralPath) shape).curveTo(341.352, 388.78702, 346.128, 384.01102, 352.01898, 384.01102);
        ((GeneralPath) shape).lineTo(437.352, 384.01102);
        ((GeneralPath) shape).curveTo(443.24298, 384.01102, 448.01898, 388.78702, 448.01898, 394.678);
        ((GeneralPath) shape).curveTo(448.01898, 400.569, 443.243, 405.345, 437.352, 405.345);
        ((GeneralPath) shape).closePath();

        g.setPaint(new Color(0xFAFAFA));
        g.fill(shape);

        // _0_2_1
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(394.686, 448.012);
        ((GeneralPath) shape).curveTo(388.795, 448.012, 384.019, 443.236, 384.019, 437.345);
        ((GeneralPath) shape).lineTo(384.019, 352.012);
        ((GeneralPath) shape).curveTo(384.019, 346.121, 388.795, 341.345, 394.686, 341.345);
        ((GeneralPath) shape).curveTo(400.577, 341.345, 405.353, 346.121, 405.353, 352.012);
        ((GeneralPath) shape).lineTo(405.353, 437.345);
        ((GeneralPath) shape).curveTo(405.352, 443.236, 400.577, 448.012, 394.686, 448.012);
        ((GeneralPath) shape).closePath();

        g.fill(shape);

        // _0_3
        shape = new GeneralPath();
        ((GeneralPath) shape).moveTo(142.76, 0.823);
        ((GeneralPath) shape).curveTo(138.775, -0.83100003, 134.18599, 0.07800001, 131.133, 3.1269999);
        ((GeneralPath) shape).lineTo(3.1329956, 131.127);
        ((GeneralPath) shape).curveTo(-1.0380044, 135.287, -1.0460043, 142.041, 3.1139956, 146.212);
        ((GeneralPath) shape).curveTo(5.1199956, 148.223, 7.8449955, 149.35101, 10.684996, 149.34601);
        ((GeneralPath) shape).lineTo(138.685, 149.34601);
        ((GeneralPath) shape).curveTo(144.576, 149.34601, 149.35199, 144.57, 149.35199, 138.67902);
        ((GeneralPath) shape).lineTo(149.35199, 10.679016);
        ((GeneralPath) shape).curveTo(149.352, 6.362, 146.75, 2.471, 142.76, 0.823);
        ((GeneralPath) shape).closePath();

        g.setPaint(new Color(0xB0BEC5));
        g.fill(shape);

        // _0_4

        // _0_5

        // _0_6

        // _0_7

        // _0_8

        // _0_9

        // _0_10

        // _0_11

        // _0_12

        // _0_13

        // _0_14

        // _0_15

        // _0_16

        // _0_17

        // _0_18

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
        return 40;
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
    public FileIcon() {
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
