package ics432.imgapp;

import javafx.scene.image.Image;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A helper class that implements static helper methods
 */
class Util {

    /**
     * Helper method to load an image from a resource file (with a URL)
     *
     * @param srcSubDir The directory in the src directory
     * @param filename The resource file name
     *
     * @return an image
     */
    static Image loadImageFromResourceFile(String srcSubDir, String filename) {

        Path path = Paths.get("src", srcSubDir, "resources", filename).toAbsolutePath();
        return loadImageFromPath(path);

    }

    /**
     * Helper method to load an image from a jpg file
     *
     * @param dirPath  the directory absolute path
     * @param filename The resource file name
     *
     * @return an image
     */
    static Image loadImageFromDir(String dirPath, String filename) {
        Path path = Paths.get(dirPath, filename).toAbsolutePath();
        return loadImageFromPath(path);
    }

    /**
     * Helper method to load an image from an absolute path
     *
     * @param path The path
     *
     * @return an image or null if there was an error
     */
    private static Image loadImageFromPath(Path path) {

        try {
            Image image = new Image(path.toUri().toURL().toString());
            if (image.isError()) {
                return null;
            }
            return image;
        } catch (MalformedURLException e) {
            return null;
        }
    }

}
