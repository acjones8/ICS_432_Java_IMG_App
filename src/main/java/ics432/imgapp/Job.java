package ics432.imgapp;

import javafx.scene.control.*;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that defines the "job" abstraction, that is, a  set of input image files
 * to which a filter must be applied, thus generating a set of output
 * image files. Each output file name is the input file name prepended with
 * the ImgTransform name and an underscore.
 */
class Job extends Thread {

    private final String filterName;
    private final Path targetDir;
    private final List<Path> inputFiles;
    private final JobWindow gui;
    protected long processingTime;
    protected long writingTime;
    protected long readingTime;
    private long executingTime;
    private boolean shouldRun = true;
    private boolean canceled = false;

    private final AppStats appStats;
    private ProducerConsumer readBuffer;
    private int jobsWritten = 0;

    // The list of outcomes for each input file
    private final List<ImgTransformOutcome> outcome;

    /**
     * Constructor
     *
     * @param filterName The imgTransform to apply to input images
     * @param targetDir  The target directory in which to generate output images
     * @param inputFiles The list of input file paths
     */
    Job(String filterName, Path targetDir, List<Path> inputFiles, JobWindow gui, AppStats appStats,
            ProducerConsumer readBuffer) {

        this.filterName = filterName;
        this.targetDir = targetDir;
        this.inputFiles = inputFiles;
        this.gui = gui;
        this.appStats = appStats;
        this.readBuffer = readBuffer;

        this.outcome = new ArrayList<>();
    }

    //Method must override run, runs in new thread when start is called

    @Override
    public void run() {
        gui.enableCancel();

        // Always run multi threading option
        executeMultiThread();
        
        gui.enableClose();
        gui.disableCancel();

        // if job is canceled, display canceled pop up
        if (canceled) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("CANCELED");
                alert.setHeaderText(null);
                alert.setContentText("Job has been canceled.");
                alert.showAndWait();
            });
        } else {

            // platform.runlater to display window for process time
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Process Time");
                alert.setHeaderText(null);
                alert.setContentText(
                        "Reading Time: " + readingTime / 1000.0 + "s\nProcessing Time: " + processingTime / 1000.0
                                + "s\nWriting Time: " + writingTime / 1000.0 + "s\nTotal Job Execution Time: "
                                + executingTime / 1000.0 + "s");
                alert.showAndWait();
            });
        }

    }

    void executeMultiThread() {
        System.err.println("Running MultiThreading execution");

        gui.setJobProgressBarVisible(true);
        
        long startTime = System.currentTimeMillis();
        // Add Paths to readBuffer before multi-threading begins.
        if(this.filterName == "DPEdge" || this.filterName == "DPFunk1" ||  this.filterName == "DPFunk2"){
            inputFiles.forEach(inputFile -> this.readBuffer.put(new WorkUnitExternal(inputFile, targetDir, filterName, appStats, this)));
        }
        else {
            inputFiles.forEach(inputFile -> this.readBuffer.put(new WorkUnit(inputFile, targetDir, filterName, appStats, this)));
        }

        synchronized(this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        this.executingTime = System.currentTimeMillis() - startTime;

        // Make Job progressBar invisible
        gui.setJobProgressBarVisible(false);
        shouldRun = false;
    }

    /**
     * Get the JobWindow
     */
    public JobWindow getGui() {
        return this.gui;
    }

    /**
     * Increment jobsWritten and return the new value
     */
    public int getJobsWritten() {
        return ++this.jobsWritten;
    }

    /**
     * Method to execute the imgTransform job
     */
    void DEPRICATED_execute() {
        System.err.println("Running non-MultiThreading execution");

        while (shouldRun) {
            double numCompleted = 0;
            int numFiles = inputFiles.size();
            long startExecuteTime = System.currentTimeMillis();

            // Make jobProgress Bar visible
            gui.setJobProgressBarVisible(true);

            // Go through each input file and process it
            for (Path inputFile : inputFiles) {

                // if canceled, stop the thread
                if (gui.isStop()) {
                    canceled = true;
                    break;
                }

                System.err.println("Applying " + this.filterName + " to " + inputFile.toAbsolutePath() + " ...");

                Path outputFile;
                try {
                    if (!canceled) {
                        this.appStats.updateExecuteJobs();
                    }
                    outputFile = processInputFile(inputFile);
                    // Generate a "success" outcome
                    this.outcome.add(new ImgTransformOutcome(true, inputFile, outputFile, null));
                    this.appStats.updateSuccessJobs();
                } catch (IOException e) {
                    // Generate a "failure" outcome
                    this.outcome.add(new ImgTransformOutcome(false, inputFile, null, e));
                }
                numCompleted++;
                // add completed files to display
                gui.addToDisplay(this);
                // set amount of progress in job progressBar
                gui.updateJobProgressBar(numCompleted / numFiles);
            }
            this.executingTime = System.currentTimeMillis() - startExecuteTime;
            // Make Job progressBar invisible
            gui.setJobProgressBarVisible(false);
            shouldRun = false;
        }

    }

    /**
     * Getter for job outcomes
     *
     * @return The job outcomes, i.e., a list of ImgTransformOutcome objects
     * (in flux if the job isn't done executing)
     */
    List<ImgTransformOutcome> getOutcome() {
        return this.outcome;
    }

    public void addToOutcome(List<ImgTransformOutcome> outcomePath, Path inputPath, Path outputPath, Exception error) {
        outcomePath.add(new ImgTransformOutcome(true, inputPath, outputPath, error));
    }

    /**
     * Helper method to apply a imgTransform to an input image file
     *
     * @param inputFile The input file path
     * @return the output file path
     */
    private Path processInputFile(Path inputFile) throws IOException {

        // Load the image from file
        WorkUnit test = new WorkUnit(inputFile, targetDir, filterName, appStats, this);

        long startReadTime = System.currentTimeMillis();
        test.readImage();
        this.readingTime += System.currentTimeMillis() - startReadTime;

        // Create the filter
        // BufferedImageOp filter = createFilter(filterName);

        // Process the image
        long startProcessingTime = System.currentTimeMillis();
        test.processImage();
        this.processingTime += System.currentTimeMillis() - startProcessingTime;

        // Write the image back to a file
        long startWritingTime = System.currentTimeMillis();

        String outputPath =
                this.targetDir + System.getProperty("file.separator") + this.filterName + "_" + inputFile.getFileName();

        test.writeImage();
        this.writingTime += System.currentTimeMillis() - startWritingTime;

        // Success!
        return Paths.get(outputPath);
    }

    /**
     * A helper nested class to define a imgTransform' outcome for a given input file and ImgTransform
     */
    static class ImgTransformOutcome {

        // Whether the image transform is successful or not
        final boolean success;
        // The Input File path
        final Path inputFile;
        // The output file path (or null if failure)
        final Path outputFile;
        // The exception that was raised (or null if success)
        final Exception error;

        /**
         * Constructor
         *
         * @param success     Whether the imgTransform operation worked
         * @param input_file  The input file path
         * @param output_file The output file path  (null if success is false)
         * @param error       The exception raised (null if success is true)
         */
        ImgTransformOutcome(boolean success, Path input_file, Path output_file, Exception error) {
            this.success = success;
            this.inputFile = input_file;
            this.outputFile = output_file;
            this.error = error;
        }

    }
}
