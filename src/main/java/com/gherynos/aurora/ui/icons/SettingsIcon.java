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
public class SettingsIcon implements Icon {

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
        g.transform(new AffineTransform(0.078125f, 0, 0, 0.078125f, 0, 0));

        // _0

        // _0_0

        // _0_0_0
        shape = new GeneralPath();
        shape.moveTo(256.0, 512.0);
        shape.curveTo(244.20312, 512.0, 234.66797, 502.4414, 234.66797, 490.66797);
        shape.lineTo(234.66797, 21.332031);
        shape.curveTo(234.66797, 9.558595, 244.20312, 0.0, 256.0, 0.0);
        shape.curveTo(267.79688, 0.0, 277.33203, 9.558594, 277.33203, 21.332031);
        shape.lineTo(277.33203, 490.66797);
        shape.curveTo(277.33203, 502.4414, 267.79688, 512.0, 256.0, 512.0);
        shape.closePath();

        g.setPaint(new Color(0x607D8B));
        g.fill(shape);

        // _0_0_1
        shape = new GeneralPath();
        shape.moveTo(426.66797, 512.0);
        shape.curveTo(414.8711, 512.0, 405.33203, 502.4414, 405.33203, 490.66797);
        shape.lineTo(405.33203, 21.332031);
        shape.curveTo(405.33203, 9.558595, 414.8711, 0.0, 426.66797, 0.0);
        shape.curveTo(438.46484, 0.0, 448.0, 9.558594, 448.0, 21.332031);
        shape.lineTo(448.0, 490.66797);
        shape.curveTo(448.0, 502.4414, 438.46484, 512.0, 426.66797, 512.0);
        shape.closePath();

        g.fill(shape);

        // _0_0_2
        shape = new GeneralPath();
        shape.moveTo(85.33203, 512.0);
        shape.curveTo(73.53516, 512.0, 64.0, 502.4414, 64.0, 490.66797);
        shape.lineTo(64.0, 21.332031);
        shape.curveTo(64.0, 9.558595, 73.53516, 0.0, 85.33203, 0.0);
        shape.curveTo(97.12891, 0.0, 106.66797, 9.558594, 106.66797, 21.332031);
        shape.lineTo(106.66797, 490.66797);
        shape.curveTo(106.66797, 502.4414, 97.12891, 512.0, 85.33203, 512.0);
        shape.closePath();

        g.fill(shape);

        // _0_1
        shape = new GeneralPath();
        shape.moveTo(170.66797, 144.0);
        shape.curveTo(170.66797, 123.41406, 153.92188, 106.66797, 133.33203, 106.66797);
        shape.lineTo(37.33203, 106.66797);
        shape.curveTo(16.746094, 106.66797, 0.0, 123.41406, 0.0, 144.0);
        shape.lineTo(0.0, 176.0);
        shape.curveTo(0.0, 196.58594, 16.746094, 213.33203, 37.33203, 213.33203);
        shape.lineTo(133.33203, 213.33203);
        shape.curveTo(153.92188, 213.33203, 170.66797, 196.58594, 170.66797, 176.0);
        shape.closePath();

        g.setPaint(new Color(0x00BCD4));
        g.fill(shape);

        // _0_2
        shape = new GeneralPath();
        shape.moveTo(474.66797, 106.66797);
        shape.lineTo(378.66797, 106.66797);
        shape.curveTo(358.07812, 106.66797, 341.33203, 123.41406, 341.33203, 144.0);
        shape.lineTo(341.33203, 176.0);
        shape.curveTo(341.33203, 196.58594, 358.07812, 213.33203, 378.66797, 213.33203);
        shape.lineTo(474.66797, 213.33203);
        shape.curveTo(495.2539, 213.33203, 512.0, 196.58594, 512.0, 176.0);
        shape.lineTo(512.0, 144.0);
        shape.curveTo(512.0, 123.41406, 495.2539, 106.66797, 474.66797, 106.66797);
        shape.closePath();

        g.setPaint(new Color(0x4CAF50));
        g.fill(shape);

        // _0_3
        shape = new GeneralPath();
        shape.moveTo(304.0, 320.0);
        shape.lineTo(208.0, 320.0);
        shape.curveTo(187.41406, 320.0, 170.66797, 336.7461, 170.66797, 357.33203);
        shape.lineTo(170.66797, 389.33203);
        shape.curveTo(170.66797, 409.92188, 187.41406, 426.66797, 208.0, 426.66797);
        shape.lineTo(304.0, 426.66797);
        shape.curveTo(324.58594, 426.66797, 341.33203, 409.92188, 341.33203, 389.33203);
        shape.lineTo(341.33203, 357.33203);
        shape.curveTo(341.33203, 336.7461, 324.58594, 320.0, 304.0, 320.0);
        shape.closePath();

        g.setPaint(new Color(0xFFC107));
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
    public SettingsIcon() {
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
