package ics432.imgapp;

import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DPMedianFilter implements BufferedImageOp {

    private int numDPThreads;

    /**
     * Constructor for Median Filter
     */
    public DPMedianFilter(int numDPThreads) {
        this.numDPThreads = numDPThreads;
    }

    public BufferedImage filter(BufferedImage inputImg, BufferedImage outputImg) {
        int width = inputImg.getWidth();
        int height = inputImg.getHeight();
        int chunkSize = (height - 1) / this.numDPThreads + 1;
        ExecutorService pool = Executors.newFixedThreadPool(this.numDPThreads);

        // If second argument is null, then filter() method allocates the output image object, otherwise is uses the one passed in.
        if (outputImg == null) {
            // allocate the output image object
            outputImg = new BufferedImage(width, height, inputImg.getType());
        }

        for (int n = 0; n < this.numDPThreads; n++) {
            pool.execute(new Task(inputImg, outputImg, width, height, n, chunkSize));
        }

        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return outputImg;
    }

    class Task implements Runnable {
        private int width, height, start, stop;
        private BufferedImage input, output;

        public Task(BufferedImage input, BufferedImage output, int width, int height, int iteration, int chunkSize) {
            this.input = input;
            this.output = output;
            this.width = width;
            this.height = height;
            this.start = iteration * chunkSize;
            this.stop = Math.min(this.start + chunkSize, height);
        }

        public void run() {
            byte[] bytes = { 0, 0, 0 };
    
            // iterate 2d array of pixels for picture
            for (int x = 0; x < this.width; x++) {
                for (int y = this.start; y < this.stop; y++) {
                    // store rgb value of each pixel
                    ArrayList<Byte> r = new ArrayList<Byte>();
                    ArrayList<Byte> g = new ArrayList<Byte>();
                    ArrayList<Byte> b = new ArrayList<Byte>();
                    for (int i = Math.max(0, x-1); i <= Math.min(this.width-1, x+1); i++) {
                        for (int j = Math.max(0, y-1); j <= Math.min(this.height-1, y+1); j++) {
                            // access the RGB value of a pixel
                            bytes = RGB.intToBytes(this.input.getRGB(i, j));
                            r.add(bytes[0]);
                            g.add(bytes[1]);
                            b.add(bytes[2]);
                        }
                    }
                    // sort and compute median of rgb value of each pixel
                    Collections.sort(r);
                    Collections.sort(g);
                    Collections.sort(b);
                    bytes[0] = r.get(r.size() / 2);
                    bytes[1] = g.get(g.size() / 2);
                    bytes[2] = b.get(b.size() / 2);
                    // set a pixel's RGB value
                    this.output.setRGB(x, y, RGB.bytesToInt(bytes));
                }
            }
        }
    }

    @Override
    public BufferedImage createCompatibleDestImage(BufferedImage arg0, ColorModel arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Rectangle2D getBounds2D(BufferedImage arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Point2D getPoint2D(Point2D arg0, Point2D arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public RenderingHints getRenderingHints() {
        // TODO Auto-generated method stub
        return null;
    }
}
