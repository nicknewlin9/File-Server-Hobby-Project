package com.newlin.test;

public class VolatileExample
{
    private volatile boolean flag = false;

    public static void main(String[] args) throws InterruptedException
    {
        VolatileExample example = new VolatileExample();

        Thread writerThread = new Thread(() ->
        {
            try
            {
                Thread.sleep(1000); // Simulate some work
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            example.setFlagTrue();
        });

        Thread readerThread = new Thread(example::doWork);

        readerThread.start();
        writerThread.start();

        readerThread.join();
        writerThread.join();
    }

    public void setFlagTrue()
    {
        flag = true;
    }

    public void doWork()
    {
        while (!flag)
        {
            Thread.onSpinWait();
            // Wait until flag becomes true
        }
        System.out.println("Flag is now true!");
    }
}