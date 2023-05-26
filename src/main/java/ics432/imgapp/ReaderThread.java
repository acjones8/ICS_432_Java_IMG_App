package ics432.imgapp;

import java.io.IOException;

public class ReaderThread extends ThreadTime implements Runnable {
    private ProducerConsumer readBuffer;
    private ProducerConsumer processBuffer;

    public ReaderThread(ProducerConsumer readBuffer, ProducerConsumer processBuffer) {
        this.readBuffer = readBuffer;
        this.processBuffer = processBuffer;
    }

    public void run() {
        while (true) {
            WorkUnit unit = readBuffer.get();
            try {
                long start = System.currentTimeMillis();
                unit.readImage();
                long end = System.currentTimeMillis();
                unit.getJob().readingTime += end - start;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.processBuffer.put(unit);
        }
    }
}
