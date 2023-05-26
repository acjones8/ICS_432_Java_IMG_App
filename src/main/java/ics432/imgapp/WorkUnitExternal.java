package ics432.imgapp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WorkUnitExternal extends WorkUnit {
    public WorkUnitExternal(Path inputFile, Path targetDir, String filterName, AppStats appStats, Job job) {
        super(inputFile, targetDir, filterName, appStats, job);
    }

    /**
     * The poison pill.
     */
    public WorkUnitExternal() {
        super();
    }

    /**
     * A helper method read images
     */
    void readImage() throws IOException {
        this.startReadTime = System.currentTimeMillis();
        System.err.println("Applying " + this.filterName + " to " + inputFile.toAbsolutePath() + " ...");
    }

    /**
     * A helper method process images
     */
    void processImage() {
        List<String> args = new ArrayList<>();
        args.add("docker");
        args.add("run");
        args.add("--rm");
        args.add("-v");
        String inputFilePathStringMap = this.targetDir + ":/tmp/input";
        args.add(inputFilePathStringMap);
        args.add("-v");
        String outputFilePathStringMap = this.targetDir + ":/tmp/output";
        args.add(outputFilePathStringMap);
        args.add("ics432imgapp_c_filters");
        String executableName = "";
        switch(this.filterName) {
            case "DPEdge":
                executableName = "jpegedge";
                break;
            case "DPFunk1":
                executableName = "jpegfunk1";
                break;
            case "DPFunk2":
                executableName = "jpegfunk2";
                break;
        }
        args.add(executableName);
        args.add("/tmp/input/" + inputFile.getFileName().toString());
        args.add("/tmp/output/" + this.filterName + "_" + inputFile.getFileName());
        args.add(Integer.toString(this.job.getGui().getNumDPThreads()));

        System.err.print(args);

        ProcessBuilder pb = new ProcessBuilder(args);
        try {
            Process p = pb.inheritIO().start(); // The inheritIO() is important!
            int status = p.waitFor();
            if (status != 0) {
                // Ok to just abort if some error
                System.err.println("Processbuilder-created process failed! [FATAL]");
                System.exit(0);
            } 
        } catch (InterruptedException ignore) {
        } catch (IOException e) {
            // Ok to just abort if some error
            System.err.println("Processbuilder-created process failed! [FATAL]");
            e.printStackTrace();
            System.exit(0);
        }

        this.appStats.updateExecuteJobs();
    }

    /**
     * A helper method write images
     */
    public Path writeImage() throws IOException {
        String outputPath = this.targetDir + System.getProperty("file.separator") + this.filterName + "_"
                + inputFile.getFileName();

        this.endReadTime = System.currentTimeMillis();
        this.appStats.getMap().get(this.filterName).updateAverageTime(this.fileBytes, getReadTime());

        return Paths.get(outputPath);
    }

}
