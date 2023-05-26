package ics432.imgapp;

public class ProcessorThread extends ThreadTime implements Runnable {
    private ProducerConsumer processBuffer;
    private ProducerConsumer writeBuffer;

    public ProcessorThread(ProducerConsumer processBuffer, ProducerConsumer writeBuffer) {
        this.processBuffer = processBuffer;
        this.writeBuffer = writeBuffer;
    }

    public void run() {
        while (true) {
            WorkUnit unit = processBuffer.get();
            long start = System.currentTimeMillis();
            try {
                unit.processImage();
            } catch (NullPointerException e) {
                // ignore shutdownNow() error messages
                break;
            }
            long end = System.currentTimeMillis();
            unit.getJob().processingTime += end - start;
            this.writeBuffer.put(unit);
        }
    }
}
