package ics432.imgapp;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that implements a "Job Window" on which a user
 * can launch a Job
 */

class JobWindow extends Stage {

    private Path targetDir;
    private final List<Path> inputFiles;
    private final FileListWithViewPort flwvp;
    private final Button changeDirButton;
    private final TextField targetDirTextField;
    private final Button runButton;
    private final Button closeButton;
    private final Button cancelButton;
    private final ComboBox<String> imgTransformList;
    private final ProgressBar jobProgressBar;
    private boolean shouldStop = false;
    private final AppStats appStats;

    private int numJobs;
    private ProducerConsumer readBuffer;
    private int numDPThreads;

    /**
     * Constructor
     *
     * @param windowWidth  The window's width
     * @param windowHeight The window's height
     * @param X            The horizontal position of the job window
     * @param Y            The vertical position of the job window
     * @param id           The id of the job
     * @param inputFiles   The batch of input image files
     */
    JobWindow(int windowWidth, int windowHeight, double X, double Y, int id, List<Path> inputFiles, AppStats appStats, 
                    ProducerConsumer readBuffer, int numDPThreads) {
        
        this.numDPThreads = numDPThreads;
        this.readBuffer = readBuffer;
        this.appStats = appStats;

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up instance variables
        targetDir = Paths.get(inputFiles.get(0).getParent().toString()); // Same dir as input images
        this.inputFiles = inputFiles;
        this.numJobs = this.inputFiles.size();

        // Set up the window
        this.setX(X);
        this.setY(Y);
        this.setTitle("Image Transformation Job #" + id);
        this.setResizable(false);

        // Make this window non closable
        this.setOnCloseRequest(Event::consume);

        // Create all sub-widgets in the window
        Label targetDirLabel = new Label("Target Directory:");
        targetDirLabel.setPrefWidth(115);

        // Create a "change target directory"  button
        this.changeDirButton = new Button("");
        this.changeDirButton.setId("changeDirButton");
        this.changeDirButton.setPrefHeight(buttonPreferredHeight);
        Image image = Util.loadImageFromResourceFile("main", "folder-icon.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(10);
        imageView.setFitHeight(10);
        this.changeDirButton.setGraphic(imageView);

        // Create a "target directory"  textfield
        this.targetDirTextField = new TextField(this.targetDir.toString());
        this.targetDirTextField.setDisable(true);
        HBox.setHgrow(targetDirTextField, Priority.ALWAYS);

        // Create an informative label
        Label transformLabel = new Label("Transformation: ");
        transformLabel.setPrefWidth(115);

        //  Create the pulldown list of image transforms
        this.imgTransformList = new ComboBox<>();
        this.imgTransformList.setId("imgTransformList");  // For TestFX
        this.imgTransformList.setItems(FXCollections.observableArrayList(
                "Invert",
                "Solarize",
                "Oil4",
                "Median",
                "DPMedian",
                "DPEdge",
                "DPFunk1",
                "DPFunk2"
        ));

        this.imgTransformList.getSelectionModel().selectFirst();  //Chooses first imgTransform as default

        // Create a "Run" button
        this.runButton =
                new Button("Run job (on " + inputFiles.size() + " image" + (inputFiles.size() == 1 ? "" : "s") + ")");
        this.runButton.setId("runJobButton");
        this.runButton.setPrefHeight(buttonPreferredHeight);

        // Create the FileListWithViewPort display
        this.flwvp =
                new FileListWithViewPort(windowWidth * 0.98, windowHeight - 4 * buttonPreferredHeight - 3 * 5, false);
        this.flwvp.addFiles(inputFiles);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        // Create a "Cancel" button
        this.cancelButton = new Button("Cancel");
        this.cancelButton.setId("cancelButton");
        this.cancelButton.setDisable(true);
        this.cancelButton.setPrefHeight(buttonPreferredHeight);

        this.jobProgressBar = new ProgressBar(0.0);
        this.jobProgressBar.setId("jobProgressBar");
        this.jobProgressBar.setVisible(false);
        this.jobProgressBar.setPrefSize(100.0, buttonPreferredHeight);

        // Set actions for all widgets
        this.changeDirButton.setOnAction(e -> {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Choose target directory");
            File dir = dirChooser.showDialog(this);
            this.setTargetDir(Paths.get(dir.getAbsolutePath()));
        });

        this.runButton.setOnAction(e -> {
            this.closeButton.setDisable(true);
            this.changeDirButton.setDisable(true);
            this.runButton.setDisable(true);
            this.imgTransformList.setDisable(true);

            executeJob(imgTransformList.getSelectionModel().getSelectedItem());
        });

        this.closeButton.setOnAction(f -> {
            // executor.shutdownNow();
            this.close();
        });

        this.cancelButton.setOnAction(f -> {
            this.shouldStop = true;
            this.cancelButton.setDisable(true);
        });

        // Build the scene
        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(targetDirLabel);
        row1.getChildren().add(changeDirButton);
        row1.getChildren().add(targetDirTextField);
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(transformLabel);
        row2.getChildren().add(imgTransformList);
        layout.getChildren().add(row2);

        layout.getChildren().add(flwvp);

        HBox row3 = new HBox(5);
        row3.getChildren().add(runButton);
        row3.getChildren().add(cancelButton);
        row3.getChildren().add(closeButton);
        row3.getChildren().add(jobProgressBar);
        layout.getChildren().add(row3);

        Scene scene = new Scene(layout, windowWidth, windowHeight);

        // Pop up the new window
        this.setScene(scene);
        this.toFront();
        this.show();
    }

    /**
     * Method to add a listener for the "window was closed" event
     *
     * @param listener The listener method
     */
    public void addCloseListener(Runnable listener) {
        this.addEventHandler(WindowEvent.WINDOW_HIDDEN, (event) -> listener.run());
    }

    /**
     * Method to set the target directory
     *
     * @param dir A directory
     */
    private void setTargetDir(Path dir) {
        if (dir != null) {
            this.targetDir = dir;
            this.targetDirTextField.setText(targetDir.toAbsolutePath().toString());
        }
    }

    /**
     * A method to execute the job
     *
     * @param filterName The name of the filter to apply to input images
     */
    private void executeJob(String filterName) {

        // Clear the display
        this.flwvp.clear();

        // Create a job
        Job job = new Job(filterName, this.targetDir, this.inputFiles, this,
            appStats, this.readBuffer);

        // Execute it, Changed to job.start to start execute() in new thread
        job.start();

    }

    public void addToDisplay(Job job) {
        // Process the outcome
        List<Path> toAddToDisplay = new ArrayList<>();

        StringBuilder errorMessage = new StringBuilder();
        for (Job.ImgTransformOutcome o : job.getOutcome()) {
            if (o.success) {
                toAddToDisplay.add(o.outputFile);
            } else {
                errorMessage.append(o.inputFile.toAbsolutePath()).append(": ").append(o.error.getMessage())
                        .append("\n");
            }
        }

        // Pop up error dialog if needed
        if (!errorMessage.toString().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ImgTransform Job Error");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage.toString());
            alert.showAndWait();
        }

        // Update the viewport
        this.flwvp.addFiles(toAddToDisplay);
    }

    //method to update progress Bar
    public void updateJobProgressBar(double progress) {
        jobProgressBar.setProgress(progress);
    }

    public void setJobProgressBarVisible(boolean visible) {
        jobProgressBar.setVisible(visible);
    }

    //method to enable close button
    public void enableClose() {
        this.closeButton.setDisable(false);
    }

    public void enableCancel() {
        this.cancelButton.setDisable(false);
    }

    public void disableCancel() {
        this.cancelButton.setDisable(true);
    }

    public boolean isStop() {
        return shouldStop;
    }

    public int getNumJobs() {
        return numJobs;
    }

    public int getNumDPThreads() {
        return this.numDPThreads;
    }
}
