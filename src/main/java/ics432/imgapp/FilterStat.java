package ics432.imgapp;

import javafx.scene.text.Text;

public class FilterStat {

    private long byteTotal;

    // Milliseconds.
    private long timeTotal;
    private double averageTime;
    private final String key;
    private final Text averageTimeText;

    public FilterStat(String key) {
        byteTotal = 0;
        timeTotal = 0;
        this.key = key;
        averageTimeText = new Text(Double.toString(averageTime));
        updateAverageTime(byteTotal, timeTotal);
    }

    public synchronized void updateAverageTime(long bytes, long time) {
        byteTotal += bytes;
        timeTotal += time;
        updateAverageTimeMb();
        setAverageTimeText();
    }

    private void setAverageTimeText() {
        averageTimeText.setText(textToString(key, averageTime));
    }

    public Text getAverageTimeText() {
        return averageTimeText;
    }

    private String textToString(String filter, double averageTime) {
        return filter + ": " + Double.toString(averageTime) + " [mb/s]";
    }

    private double getTimeInSeconds() {
        return (double) timeTotal / 1000;
    }

    private double getMb() {
        return (double) byteTotal / 1048576;
    }

    private synchronized void updateAverageTimeMb() {
        if (timeTotal == 0) {
            averageTime = 0;
            return;
        }
        averageTime = getMb() / getTimeInSeconds();
    }
}
