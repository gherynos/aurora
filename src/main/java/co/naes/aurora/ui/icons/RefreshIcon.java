package co.naes.aurora.ui.icons;

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
public class RefreshIcon implements Icon {

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
        shape.moveTo(19.0, 8.75);
        shape.lineTo(19.0, 9.64);
        shape.lineTo(12.43, 12.790001);
        shape.curveTo(12.16, 12.920001, 11.84, 12.920001, 11.570001, 12.790001);
        shape.lineTo(5.0000005, 9.640001);
        shape.lineTo(5.0000005, 8.750001);
        shape.curveTo(5.0000005, 7.790001, 5.7900004, 7.000001, 6.7500005, 7.000001);
        shape.lineTo(17.25, 7.000001);
        shape.curveTo(18.21, 7.000001, 19.0, 7.790001, 19.0, 8.750001);
        shape.closePath();

        g.setPaint(new Color(0x2196F3));
        g.fill(shape);

        // _0_1
        shape = new GeneralPath();
        shape.moveTo(19.0, 9.64);
        shape.lineTo(19.0, 15.25);
        shape.curveTo(19.0, 16.21, 18.21, 17.0, 17.25, 17.0);
        shape.lineTo(6.75, 17.0);
        shape.curveTo(5.79, 17.0, 5.0, 16.21, 5.0, 15.25);
        shape.lineTo(5.0, 9.639999);
        shape.lineTo(11.57, 12.789999);
        shape.curveTo(11.84, 12.919999, 12.16, 12.919999, 12.429999, 12.789999);
        shape.closePath();

        g.setPaint(new Color(0x64B5F6));
        g.fill(shape);

        // _0_2
        shape = new GeneralPath();
        shape.moveTo(23.0, 11.0);
        shape.curveTo(22.447, 11.0, 22.0, 11.448, 22.0, 12.0);
        shape.curveTo(22.0, 17.514, 17.514, 22.0, 12.0, 22.0);
        shape.curveTo(6.486, 22.0, 2.0, 17.514, 2.0, 12.0);
        shape.curveTo(2.0, 6.486, 6.486, 2.0, 12.0, 2.0);
        shape.curveTo(13.661, 2.0, 15.289, 2.424, 16.737, 3.203);
        shape.lineTo(15.219999, 4.7200003);
        shape.curveTo(15.004999, 4.9340005, 14.941999, 5.2570004, 15.056999, 5.537);
        shape.curveTo(15.172999, 5.8170004, 15.445999, 6.0, 15.749999, 6.0);
        shape.lineTo(20.25, 6.0);
        shape.curveTo(20.664, 6.0, 21.0, 5.664, 21.0, 5.25);
        shape.lineTo(21.0, 0.75);
        shape.curveTo(21.0, 0.447, 20.817, 0.17299998, 20.537, 0.05699998);
        shape.curveTo(20.257002, -0.059000015, 19.934, 0.0049999803, 19.720001, 0.21999998);
        shape.lineTo(18.2, 1.7390001);
        shape.curveTo(16.338001, 0.6140001, 14.193001, 1.1920929E-7, 12.000001, 1.1920929E-7);
        shape.curveTo(5.383001, 1.1920929E-7, 9.536743E-7, 5.383, 9.536743E-7, 12.0);
        shape.curveTo(9.536743E-7, 18.617, 5.383001, 24.0, 12.000001, 24.0);
        shape.curveTo(18.617, 24.0, 24.0, 18.617, 24.0, 12.0);
        shape.curveTo(24.0, 11.448, 23.553, 11.0, 23.0, 11.0);
        shape.closePath();

        g.setPaint(new Color(0x607D8B));
        g.fill(shape);

        g.setTransform(transformations.pop()); // _0

    }

    /**
     * Returns the X of the bounding box of the original SVG image.
     *
     * @return The X of the bounding box of the original SVG image.
     */
    public static int getOrigX() {
        return 1;
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
    public RefreshIcon() {
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
