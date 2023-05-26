package ics432.imgapp;

import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.OilFilter;
import com.jhlabs.image.SolarizeFilter;
import javafx.scene.image.Image;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.imageio.ImageIO.createImageOutputStream;

public class WorkUnit {
    protected final Path targetDir;
    protected String filterName;
    protected Path inputFile;
    protected long startReadTime;
    protected long endReadTime;
    protected AppStats appStats;
    protected long fileBytes;
    protected Job job;

    private Image image;
    private BufferedImage img;
    private boolean isPoison;

    public WorkUnit(Path inputFile, Path targetDir, String filterName, AppStats appStats, Job job) {
        this.inputFile = inputFile;
        this.targetDir = targetDir;
        this.filterName = filterName;
        this.appStats = appStats;

        this.isPoison = false;
        this.fileBytes = new File(inputFile.toAbsolutePath().toString()).length();
        this.startReadTime = 0;
        this.endReadTime = 0;
        this.job = job;
    }

    /**
     * The poison pill.
     */
    public WorkUnit() {
        this.targetDir = null;
        this.isPoison = true;
    }

    /**
     * A helper method read images
    */
    void readImage() throws IOException {
        this.startReadTime = System.currentTimeMillis();

        System.err.println("Applying " + this.filterName + " to " + inputFile.toAbsolutePath() + " ...");

        try {
            this.image = new Image(inputFile.toUri().toURL().toString());

            if (image.isError()) {
                throw new IOException("Error while reading from " + inputFile.toAbsolutePath() +
                        " (" + image.getException().toString() + ")");
            }
        } catch (IOException e) {
            throw new IOException("Error while reading from " + inputFile.toAbsolutePath());
        }
    }

    /**
     * A helper method process images
    */
    void processImage() {
        BufferedImageOp filter = createFilter(filterName);
        this.img = filter.filter(SwingFXUtils.fromFXImage(image, null), null);
        this.appStats.updateExecuteJobs();
    }

    /**
     * A helper method write images
    */
    public Path writeImage() throws IOException {
        String outputPath =
                this.targetDir + System.getProperty("file.separator") + this.filterName + "_" + inputFile.getFileName();
        
        OutputStream os;
        ImageOutputStream outputStream;
        try {
            os = new FileOutputStream(outputPath);
            outputStream = createImageOutputStream(os);
            ImageIO.write(this.img, "jpg", outputStream);
            this.appStats.updateSuccessJobs();
            this.endReadTime = System.currentTimeMillis();
        } catch (IOException | NullPointerException e) {
            throw new IOException("Error while writing to " + outputPath);
        }
        outputStream.close();
        os.close();
        this.appStats.getMap().get(this.filterName).updateAverageTime(this.fileBytes, getReadTime());
        return Paths.get(outputPath);
    }

    /*
     * Clean up memory for garbage collector
     */
    public void cleanUpWorkUnit() {
        this.image = null;
        this.img = null;
        this.job = null;
    }

    /**
     * A getter method that returns input file
    */
    public Path getInputFile() {
        return this.inputFile;
    }

    /**
     * Get the job
     */
    public Job getJob() {
        return this.job;
    }

    /**
     * A helper method to create a Filter object
     *
     * @param filterName the filter's name
     */
    private BufferedImageOp createFilter(String filterName) {
        switch (filterName) {
            case "Invert":
                return new InvertFilter();
            case "Solarize":
                return new SolarizeFilter();
            case "Oil4":
                OilFilter oil4Filter = new OilFilter();
                oil4Filter.setRange(4);
                return oil4Filter;
            case "Median":
                return new MedianFilter();
            case "DPMedian":
                return new DPMedianFilter(this.job.getGui().getNumDPThreads());
            default:
                throw new RuntimeException("Unknown filter " + filterName);
        }
    }

    public boolean isPoison() {
        return isPoison;
    }

    public long getReadTime() {
        if (startReadTime == 0 || endReadTime == 0) {
            throw new RuntimeException("startReadTime: " + startReadTime + "; endReadTime: " + endReadTime);
        }
        return endReadTime - startReadTime;
    }

}
