package ics432.imgapp;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Collections;

public class MedianFilter implements BufferedImageOp {

    // private BufferedImage inputImg;
    // private BufferedImage outputImg;
    private int width;
    private int height;

    @Override
    public BufferedImage filter(BufferedImage inputImg, BufferedImage outputImg) throws IllegalArgumentException {
        width = inputImg.getWidth();
        height = inputImg.getHeight();

        // If second argument is null, then filter() method allocates the output image object, otherwise is uses the one passed in.
        if (outputImg == null) {
            // allocate the output image object
            outputImg = new BufferedImage(width, height, inputImg.getType());
        }
        // iterate 2d array of pixels and process picture
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                outputImg.setRGB(x, y, processPixel(inputImg, x, y));
            }
        }
        return outputImg;
    }

    private int processPixel(BufferedImage inputImg, int x, int y) {
        byte[] bytes = {0, 0, 0};
        int rgb;
        ArrayList<Byte> r = new ArrayList<Byte>();
        ArrayList<Byte> g = new ArrayList<Byte>();
        ArrayList<Byte> b = new ArrayList<Byte>();
        // get rgb values of surrounding pixel
        for (int i = Math.max(0, x - 1); i <= Math.min(width - 1, x + 1); i++) {
            for (int j = Math.max(0, y - 1); j <= Math.min(height - 1, y + 1); j++) {
                rgb = inputImg.getRGB(i, j);
                // access the RGB value of a pixel
                bytes = RGB.intToBytes(rgb);
                r.add(bytes[0]);
                g.add(bytes[1]);
                b.add(bytes[2]);
            }
        }
        Collections.sort(r);
        Collections.sort(g);
        Collections.sort(b);
        bytes[0] = r.get(r.size() / 2);
        bytes[1] = g.get(g.size() / 2);
        bytes[2] = b.get(b.size() / 2);

        // return median rgb value
        return RGB.bytesToInt(bytes);
    }


    @Override
    public Rectangle2D getBounds2D(BufferedImage src) {
        return null;
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel destCM) {
        return null;
    }

    @Override
    public RenderingHints getRenderingHints() {
        return null;
    }

    @Override
    public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) {
        return null;
    }

}
