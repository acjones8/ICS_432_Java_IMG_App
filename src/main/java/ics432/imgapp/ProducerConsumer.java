package ics432.imgapp;

import java.util.concurrent.ArrayBlockingQueue;

public class ProducerConsumer {
    private final ArrayBlockingQueue<WorkUnit> buffer;

    public ProducerConsumer() {
        this.buffer = new ArrayBlockingQueue<>(16);
    }

    public void put(WorkUnit workUnit) {
        try{
            this.buffer.put(workUnit);
        }  catch (InterruptedException e) {
            // e.printStackTrace();
        }        
    }

    public WorkUnit get() {
        WorkUnit workUnit = null;
        try {
           workUnit = this.buffer.take();
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
        return workUnit;
    }

    public int getSize() {
        return this.buffer.size();
    }

}
