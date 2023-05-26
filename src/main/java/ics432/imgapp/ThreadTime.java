package ics432.imgapp;

public abstract class ThreadTime {
    private long startTime;
    private long endTime;

    protected void updateStartTime() {
        startTime = System.currentTimeMillis();
    }

    protected void updateEndTime() {
        endTime = System.currentTimeMillis();
    }

    /* If startTime or endTime has not been updated then throw a runtime exception, otherwise return the total time. */
    public long getTime() {
        if (startTime == 0 || endTime == 0) {
            throw new RuntimeException("startTime: " + startTime + "; endTime: " + endTime);
        }
        return endTime - startTime;
    }

}
