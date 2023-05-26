package ics432.imgapp;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

class StatisticsWindow extends Stage {

    private final Button closeButton;
    private boolean shouldStop = false;

    /**
     * Constructor
     *
     * @param windowWidth  The window's width
     * @param windowHeight The window's height
     * @param X            The horizontal position of the job window
     * @param Y            The vertical position of the job window
     */
    StatisticsWindow(int windowWidth, int windowHeight, double X, double Y, AppStats appStats) {

        // The  preferred height of buttons
        double buttonPreferredHeight = 27.0;

        // Set up the window
        this.setTitle("Jobs Statistics");
        this.setResizable(false);
        this.centerOnScreen();
        this.setWidth(400);
        this.setHeight(225);

        // Create a "Close" button
        this.closeButton = new Button("Close");
        this.closeButton.setId("closeButton");
        this.closeButton.setPrefHeight(buttonPreferredHeight);

        this.closeButton.setOnAction(f -> this.close());
        Text jobsExecutedKey = new Text("Total Jobs Started");
        Text jobsSuccessKey = new Text("Total Jobs Completed");
        Text averageJobs = new Text("Average Time");

        // Build the scene

        VBox layout = new VBox(5);

        HBox row1 = new HBox(5);
        row1.setAlignment(Pos.CENTER_LEFT);
        row1.getChildren().add(jobsExecutedKey);
        row1.getChildren().add(appStats.getExecuteText());
        layout.getChildren().add(row1);

        HBox row2 = new HBox(5);
        row2.setAlignment(Pos.CENTER_LEFT);
        row2.getChildren().add(jobsSuccessKey);
        row2.getChildren().add(appStats.getSuccessText());
        layout.getChildren().add(row2);

        HBox row3 = new HBox(5);
        row3.setAlignment(Pos.CENTER_LEFT);
        row3.getChildren().add(averageJobs);
        layout.getChildren().add(row3);

        VBox vBox = new VBox(5);
        for(FilterStat filterStat: appStats.getMap().values()) {
            vBox.getChildren().add(filterStat.getAverageTimeText());
        }
        layout.getChildren().add(vBox);




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

    //method to enable close button
    public void enableClose() {
        this.closeButton.setDisable(false);
    }

    public boolean isStop() {
        return shouldStop;
    }

}
