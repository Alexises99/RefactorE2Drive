package com.example.refactore2drive.obd;

import com.github.pires.obd.commands.ObdCommand;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;


public class OBDProducer implements Runnable{
    private BlockingQueue<OBDCommandJob> queue;
    private ArrayList<ObdCommand> list;

    private static final String TAG = OBDProducer.class.getName();

    public OBDProducer(BlockingQueue<OBDCommandJob> queue, ArrayList<ObdCommand> list) {
        this.queue = queue;
        this.list = list;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (ObdCommand command : list) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                queue.add(new OBDCommandJob(command));
            }
        }
    }
}

