package ics432.imgapp;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * A class that implements the "Main Window" for the app, which
 * allows the user to add input files for potential filtering and
 * to create an image filtering job
 */
class MainWindow {

    private final Stage primaryStage;
    private final Button quitButton;
    private int pendingJobCount = 0;
    private final FileListWithViewPort fileListWithViewPort;
    private int jobID = 0;
    private final Slider slider, dpThreadsSlider;
    private int additionalProcessorThreads = 0;
    private int numDPThreads = 1;
    private ExecutorService executor;

    // Create the array blocking queues
    // to read
    ProducerConsumer readBuffer = new ProducerConsumer();
    // to process
    ProducerConsumer processBuffer = new ProducerConsumer();
    // to write
    ProducerConsumer writeBuffer = new ProducerConsumer();

    /**
     * Constructor
     *
     * @param primaryStage The primary stage
     */
    MainWindow(Stage primaryStage, int windowWidth, int windowHeight) {

        // Initialize the reader, processor, and writer daemon threads
        ReaderThread reader = new ReaderThread(readBuffer, processBuffer);
        ProcessorThread processor = new ProcessorThread(processBuffer, writeBuffer);
        WriterThread writer = new WriterThread(writeBuffer);

        Thread readerThread = new Thread(reader);
        Thread processorThread = new Thread(processor);
        Thread writerThread = new Thread(writer);

        readerThread.setDaemon(true);
        processorThread.setDaemon(true);
        writerThread.setDaemon(true);

        readerThread.start();
        processorThread.start();
        writerThread.start();

        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        double buttonPreferredHeight = 27.0;

        AppStats appStats = new AppStats();

        appStats.addFilter("Invert");
        appStats.addFilter("Solarize");
        appStats.addFilter("Oil4");
        appStats.addFilter("Median");
        appStats.addFilter("DPMedian");
        appStats.addFilter("DPEdge");
        appStats.addFilter("DPFunk1");
        appStats.addFilter("DPFunk2");

        // Set up the primaryStage
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ICS 432 Image Editing App");

        // Make this primaryStage non closable
        this.primaryStage.setOnCloseRequest(Event::consume);

        // Create all widgets
        Button addFilesButton = new Button("Add Image Files");
        addFilesButton.setPrefHeight(buttonPreferredHeight);
        addFilesButton.setId("addFilesButton"); // for TestFX

        Button createJobButton = new Button("Create Job");
        createJobButton.setPrefHeight(buttonPreferredHeight);
        createJobButton.setDisable(true);
        createJobButton.setId("createJobButton"); // for TestFX

        quitButton = new Button("Quit");
        quitButton.setId("quitButton"); // for TestFX
        quitButton.setPrefHeight(buttonPreferredHeight);

        // Show Stats button
        Button showStatsButton = new Button("Show Statistics");
        showStatsButton.setId("showStatsButton");
        showStatsButton.setPrefHeight(buttonPreferredHeight);

        // Slider for # images in RAM
        this.slider = new Slider(1, Runtime.getRuntime().availableProcessors(), 1);
        final Label imagesInRam = new Label("#threads");
        final Label imagesValueLabel = new Label("1");

        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setSnapToTicks(true);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setPrefWidth(250);

        slider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            imagesValueLabel.setText(String.valueOf((newValue.intValue())));
            additionalProcessorThreads = newValue.intValue() - 1;

            // kill existing threads in thread pool
            executor.shutdownNow();

            // System.out.println("Total Number of threads running BEFORE CREATION: " + Thread.activeCount());

            // (Re)Create thread pool with max being the num of available processors
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            for (int i = 0; i < this.additionalProcessorThreads; i++) {
                executor.execute(new ProcessorThread(processBuffer, writeBuffer));
            }

            // System.out.println("Total Number of threads running AFTER CREATION: " + Thread.activeCount());

        });

        // Slider for # dp threads
        this.dpThreadsSlider = new Slider(1, Runtime.getRuntime().availableProcessors(), 1);
        final Label dpThreadsLabel = new Label("#dp_threads");
        dpThreadsSlider.setShowTickLabels(true);
        dpThreadsSlider.setShowTickMarks(true);
        dpThreadsSlider.setSnapToTicks(true);
        dpThreadsSlider.setBlockIncrement(1);
        dpThreadsSlider.setMajorTickUnit(1);
        dpThreadsSlider.setMinorTickCount(0);
        dpThreadsSlider.setPrefWidth(250);

        dpThreadsSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            this.numDPThreads = newValue.intValue();
        });

        this.fileListWithViewPort = new FileListWithViewPort(
                windowWidth * 0.98,
                windowHeight - 3 * buttonPreferredHeight - 3 * 5,
                true);

        // Listen for the "nothing is selected" property of the widget
        // to disable the createJobButton dynamically
        this.fileListWithViewPort.addNoSelectionListener(createJobButton::setDisable);

        // Set actions for all widgets
        addFilesButton.setOnAction(e -> addFiles(selectFilesWithChooser()));

        quitButton.setOnAction(e -> {
            // kill existing threads in thread pool
            executor.shutdownNow();

            // If the button is enabled, it's fine to quit
            this.primaryStage.close();
        });

        createJobButton.setOnAction(e -> {
            this.quitButton.setDisable(true);
            this.pendingJobCount += 1;
            this.jobID += 1;
            this.slider.setDisable(true);
            this.dpThreadsSlider.setDisable(true);

            JobWindow jw = new JobWindow(
                    (int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                    this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                    this.primaryStage.getY() + 50 + this.pendingJobCount * 10,
                    this.jobID, new ArrayList<>(this.fileListWithViewPort.getSelection()),
                    appStats, this.readBuffer, this.numDPThreads);

            jw.addCloseListener(() -> {
                this.pendingJobCount -= 1;
                if (this.pendingJobCount == 0) {
                    this.quitButton.setDisable(false);
                    this.slider.setDisable(false);
                    this.dpThreadsSlider.setDisable(false);
                }
            });
        });

        showStatsButton.setOnAction(event -> {
            StatisticsWindow statisticsWindow =
                    new StatisticsWindow((int) (windowWidth * 0.8), (int) (windowHeight * 0.8),
                            this.primaryStage.getX() + 100 + this.pendingJobCount * 10,
                            this.primaryStage.getY() + 50 + this.pendingJobCount * 10,
                            appStats);
        });

        //Construct the layout
        VBox layout = new VBox(5);

        layout.getChildren().add(addFilesButton);
        layout.getChildren().add(this.fileListWithViewPort);

        HBox row=new HBox(8);
        row.getChildren().add(createJobButton);
        row.getChildren().add(quitButton);
        row.getChildren().add(showStatsButton);
        row.getChildren().add(imagesInRam);
        row.getChildren().add(slider);
        row.getChildren().add(imagesValueLabel);
        row.getChildren().add(dpThreadsLabel);
        row.getChildren().add(dpThreadsSlider);
        layout.getChildren().add(row);

        Scene scene = new Scene(layout, windowWidth, windowHeight);
        this.primaryStage.setScene(scene);
        this.primaryStage.setResizable(false);

        // Make this primaryStage non closable
        this.primaryStage.setOnCloseRequest(Event::consume);

        //  Show it on  screen.
        this.primaryStage.show();
    }

    /**
     * Method that pops up a file chooser and returns chosen image files
     *
     * @return The list of files
     */
    private List<Path> selectFilesWithChooser() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image Files");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Jpeg Image Files", "*.jpg", "*.jpeg", "*.JPG", "*.JPEG"));
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(this.primaryStage);

        if (selectedFiles == null) {
            return new ArrayList<>();
        } else {
            return selectedFiles.stream().collect(ArrayList::new,
                    (c, e) -> c.add(Paths.get(e.getAbsolutePath())),
                    ArrayList::addAll);
        }
    }

    /*
    private static void statsButtonOnAction() {
    }
     */

    /**
     * Method that adds files to the list of known files
     *
     * @param files The list of files
     */
    private void addFiles(List<Path> files) {

        if (files != null) {
            this.fileListWithViewPort.addFiles(files);
        }
    }

}
