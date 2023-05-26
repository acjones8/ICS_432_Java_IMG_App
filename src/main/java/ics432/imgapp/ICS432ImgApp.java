package ics432.imgapp;

import javafx.application.Application;
import javafx.stage.Stage;


import java.awt.*;

/**
 * Top-level class that merely defines the JavaFX start() method that pops up
 * the MainWindow window.
 *
 * It is in this class that one may want to add static variables and objects that
 * should be visible to all (most) classes in this application. Remaining aware
 * that "globals" are a bad idea in general.
 */
public class ICS432ImgApp extends Application {

    /**
     * start() JavaFx Method to start the application
     *
     * @param primaryStage  The primary stage, off which hang all windows.
     */
    @Override
    public void start(Stage primaryStage) {
        // Determine screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Determine appropriate window dimensions
        int width = (int)(0.8 * screenSize.getWidth());
        int height = (int)(0.8 * screenSize.getHeight());
        // Pop up the main window
        new MainWindow(primaryStage, width, height);
    }
}
