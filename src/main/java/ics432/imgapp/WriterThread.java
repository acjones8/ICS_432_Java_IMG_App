package ics432.imgapp;

import java.io.IOException;
import java.nio.file.Path;

public class WriterThread extends ThreadTime implements Runnable {
    private ProducerConsumer writeBuffer;

    public WriterThread(ProducerConsumer writeBuffer) {
        this.writeBuffer = writeBuffer;
    }

    public void run() {
        while (true) {
            WorkUnit unit = writeBuffer.get();
            Path inputFile = unit.getInputFile();
            Job job = unit.getJob();
            JobWindow jw = job.getGui();

            try {
                long start = System.currentTimeMillis();
                Path outputFile = unit.writeImage();
                long end = System.currentTimeMillis();
                job.writingTime += end - start;
                double progress = (double) job.getJobsWritten() / jw.getNumJobs();

                job.addToOutcome(job.getOutcome(), inputFile, outputFile, null);
                
                // addFiletoDisplay();
                // updateProgressBar();

                jw.addToDisplay(job);
                jw.updateJobProgressBar(progress);
                if (progress == 1.0) {
                    synchronized(job) {
                        job.notify();
                    }
                }

                unit.cleanUpWorkUnit();
            } catch (IOException e) {
                job.addToOutcome(job.getOutcome(), inputFile, null, e);
                throw new RuntimeException(e);
            }
        }
    }

    // private void addFiletoDisplay () {
    //     jobWindow.addToDisplay(job);
    // }

    // private void updateProgressBar() {
    //     jobWindow.updateJobProgressBar(++numJobs / jobWindow.getNumJobs());
    // }
}
