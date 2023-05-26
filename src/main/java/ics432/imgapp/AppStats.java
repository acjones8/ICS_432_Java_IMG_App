package ics432.imgapp;

import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;

public class AppStats {
    private int executeJobs;
    private int successJobs;

    private Text successText;
    private Text executeText;


    private Map<String, FilterStat> filterStatMap;

    public AppStats() {
        executeJobs = 0;
        successJobs = 0;
        successText = new Text(Integer.toString(successJobs));
        executeText = new Text(Integer.toString(executeJobs));
        filterStatMap = new HashMap<>();
    }

    public void addFilter(String key) {
        filterStatMap.put(key, new FilterStat(key));
    }
    public Map<String,FilterStat> getMap(){
        return filterStatMap;
    }

    private void setSuccessText() {
        successText.setText(Integer.toString(successJobs));
    }

    private void setExecuteText() {
        executeText.setText(Integer.toString(executeJobs));
    }

    public Text getSuccessText() {
        return successText;
    }

    public Text getExecuteText() {
        return executeText;
    }

    public synchronized void updateExecuteJobs() {
        executeJobs++;
        setExecuteText();
    }

    public synchronized void updateSuccessJobs() {
        successJobs++;
        setSuccessText();
    }
}
