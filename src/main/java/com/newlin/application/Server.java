package com.newlin.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Server
{
    public static final int MAX_CONNECTED_CLIENTS = 3;

    public static final int MAX_REQUESTS_PER_CLIENT = 3;

    public static final int NUM_THREADS = 3 + (MAX_CONNECTED_CLIENTS * MAX_REQUESTS_PER_CLIENT);

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS);

    public static final int LISTENING_PORT = 3001;

    public static Queue<SocketChannel> clientQueue = new LinkedList<>();
    public static ReentrantLock clientQueueLock = new ReentrantLock();
    public static Semaphore clientQueueSlot = new Semaphore(MAX_CONNECTED_CLIENTS,true);
    public static Semaphore clientConnection = new Semaphore(0,true);

    public static boolean isOnline = false;
    public static ReentrantLock isOnlineLock = new ReentrantLock();

    public static void main(String[] args)
    {
        try
        {
            Thread.sleep(1000);
            System.out.print("STARTING SERVER");
            Thread.sleep(1000);
            System.out.print(".");
            Thread.sleep(1000);
            System.out.print(".");
            Thread.sleep(1000);
            System.out.print(".\n");
            Thread.sleep(1000);

            //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE AN AVAILABLE QUEUE SLOT
            //THEN BLOCKS UNTIL IT RECEIVES A NEW CLIENT CONNECTION
            //ADDS THE NEW CLIENT CONNECTION TO THE QUEUE
            //RELEASES A PERMIT TO THE CONSUMER
            EXECUTOR.submit(new NewConnectionProducer());

            //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE A NEW CLIENT CONNECTION
            //SUBMITS THAT NEW CLIENT CONNECTION TO THE EXECUTOR FOR PROCESSING
            EXECUTOR.submit(new NewConnectionConsumer());

            //CREATES AN ACTIVE THREAD FOR ACCEPTING AND PROCESSING USER INPUT
            EXECUTOR.submit(new UserInputListener());
        }
        catch(Exception exception)
        {
            System.err.println("EXCEPTION DURING STARTUP");
            exception.printStackTrace();
        }
    }

    public static class NewConnectionProducer implements Runnable
    {
        public void run()
        {
            try
            {
                ServerSocketChannel incomingConnectionsListenChannel = ServerSocketChannel.open().bind(new InetSocketAddress(LISTENING_PORT));
                if(incomingConnectionsListenChannel.isOpen())
                {
                    try
                    {
                        isOnlineLock.lock();
                        isOnline = true;
                        System.out.println("SERVER OPEN AND LISTENING ON PORT: " + LISTENING_PORT);
                    }
                    finally
                    {
                        isOnlineLock.unlock();
                    }
                }
                do
                {
                    try
                    {
                        clientQueueSlot.acquire();
                        SocketChannel newConnection = incomingConnectionsListenChannel.accept();
                        clientQueueLock.lock();
                        clientQueue.add(newConnection);
                        clientConnection.release();
                    }
                    finally
                    {
                        clientQueueLock.unlock();
                    }
                }
                while(incomingConnectionsListenChannel.isOpen());

                try
                {
                    isOnlineLock.lock();
                    isOnline = false;
                    System.out.println("SERVER CLOSED");
                }
                finally
                {
                    isOnlineLock.unlock();
                }
            }
            catch(InterruptedException | IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static class NewConnectionConsumer implements Runnable
    {
        public void run()
        {
            try
            {
                do
                {
                    clientConnection.acquire();
                    SocketChannel socketChannel = clientQueue.element();
                    System.out.println("SERVING NEW CONNECTION FROM: " + socketChannel.socket().getInetAddress().getHostName());

                    //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE AN AVAILABLE COMMAND SLOT
                    //THEN BLOCKS UNTIL IT RECEIVES A NEW COMMAND FROM CLIENT
                    //ADDS THE NEW COMMAND TO THE QUEUE
                    //RELEASES A PERMIT TO THE CONSUMER
                    EXECUTOR.submit(new NewCommandProducer(socketChannel));

                    //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE A NEW COMMAND REQUEST
                    //SUBMITS THAT NEW COMMAND TO THE EXECUTOR TO PROCESSING
                    EXECUTOR.submit(new NewCommandConsumer(socketChannel));
                }
                while(isOnline);
            }
            catch(InterruptedException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    //ACTIVE THREAD FOR ACCEPTING AND PROCESSING USER INPUT
    public static class UserInputListener implements Runnable
    {
        public void run()
        {
            Scanner scanner = new Scanner(System.in);
            do
            {
                System.out.println("\nENTER A COMMAND: ");
                switch(scanner.nextLine().toUpperCase())
                {
                    case "QUIT":
                        System.exit(0);
                        break;

                    case "FORCE QUIT":
                        System.exit(0);
                        break;

                    default:
                        System.out.println("\nVALID COMMANDS: \"QUIT\" OR \"FORCE QUIT\"");
                        break;
                }
            }
            while(isOnline);
            scanner.close();
        }
    }

    public static class NewCommandProducer implements Runnable
    {
        public SocketChannel socketChannel;

        public static Queue<Command> commandQueue = new LinkedList<>();
        public static ReentrantLock commandQueueLock = new ReentrantLock();
        public static Semaphore commandQueueSlot = new Semaphore(MAX_REQUESTS_PER_CLIENT,true);
        public static Semaphore commandRequest = new Semaphore(0,true);

        public static boolean isConnected = false;
        public static ReentrantLock isConnectedLock = new ReentrantLock();

        public NewCommandProducer(SocketChannel socketChannel)
        {
            this.socketChannel = socketChannel;
        }

        public void run()
        {
            try
            {
                if(socketChannel.isOpen())
                {
                    try
                    {
                        isConnectedLock.lock();
                        isConnected = true;
                    }
                    finally
                    {
                        isConnectedLock.unlock();
                    }
                }
                do
                {
                    try
                    {
                        commandQueueSlot.acquire();
                        ObjectInputStream objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
                        Command receivedCommand = (Command) objectInputStream.readObject();
                        commandQueueLock.lock();
                        commandQueue.add(receivedCommand);
                        commandRequest.release();
                    }
                    finally
                    {
                        commandQueueLock.unlock();
                    }
                }
                while(socketChannel.isOpen());

                //IF THE SOCKET CHANNEL CLOSED
                try
                {
                    clientQueueLock.lock();
                    clientQueue.remove(this.socketChannel);
                    clientQueueSlot.release();
                }
                finally
                {
                    clientQueueLock.unlock();
                }
            }
            catch(InterruptedException | IOException | ClassNotFoundException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static class NewCommandConsumer implements Runnable //CONSUMER OF PACKETS
    {
        public SocketChannel socketChannel;

        public NewCommandConsumer(SocketChannel socketChannel)
        {
            this.socketChannel = socketChannel;
        }

        public void run()
        {
            try
            {
                do
                {
                    NewCommandProducer.commandRequest.acquire();
                    Command command = NewCommandProducer.commandQueue.element();
                    System.out.println("RECEIVED COMMAND FROM: " + this.socketChannel.socket().getInetAddress().getHostName());

                    ProcessCommand(command, socketChannel);
                }
                while(NewCommandProducer.isConnected);
            }
            catch(InterruptedException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static void ProcessCommand(Command command, SocketChannel socketChannel)
    {
        Response response;
        Actions action = command.getAction();
        switch(action)
        {
            case Actions.LIST:
                EXECUTOR.submit(new ProcessListCommand(command,socketChannel));

            case Actions.DELETE:
                EXECUTOR.submit(new ProcessDeleteCommand(command,socketChannel));

            case Actions.RENAME:
                EXECUTOR.submit(new ProcessRenameCommand(command,socketChannel));

            case Actions.DOWNLOAD:
                EXECUTOR.submit(new ProcessDownloadCommand(command,socketChannel));

            case Actions.UPLOAD:
                EXECUTOR.submit(new ProcessUploadCommand(command,socketChannel));

            default:
                response = new Response(false, "RECEIVED INVALID COMMAND");
                sendResponse(response,socketChannel);
                try
                {
                    NewCommandProducer.commandQueueLock.lock();
                    NewCommandProducer.commandQueue.remove(command);
                    NewCommandProducer.commandQueueSlot.release();
                }
                finally
                {
                    NewCommandProducer.commandQueueLock.unlock();
                }
        }
    }

    public static void sendResponse(Response response, SocketChannel socketChannel)
    {
        try
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketChannel.socket().getOutputStream());
            objectOutputStream.writeObject(response);
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static class ProcessListCommand implements Runnable
    {
        private Command command;
        private SocketChannel socketChannel;

        public ProcessListCommand(Command command, SocketChannel socketChannel)
        {
            this.command = command;
            this.socketChannel = socketChannel;
        }
        public void run()
        {
            try
            {
                Response response = new Response(true, "RECEIVED LIST COMMAND");
                sendResponse(response,socketChannel);
                try
                {
                    NewCommandProducer.commandQueueLock.lock();
                    NewCommandProducer.commandQueue.remove(command);
                    NewCommandProducer.commandQueueSlot.release();
                }
                finally
                {
                    NewCommandProducer.commandQueueLock.unlock();
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD PROCESSING LIST COMMAND");
                exception.printStackTrace();
            }
        }
    }

    public static class ProcessDeleteCommand implements Runnable
    {
        private Command command;
        private SocketChannel socketChannel;

        public ProcessDeleteCommand(Command command, SocketChannel socketChannel)
        {
            this.command = command;
            this.socketChannel = socketChannel;
        }
        public void run()
        {
            try
            {
                Response response = new Response(true, "RECEIVED DELETE COMMAND");
                sendResponse(response,socketChannel);
                try
                {
                    NewCommandProducer.commandQueueLock.lock();
                    NewCommandProducer.commandQueue.remove(command);
                    NewCommandProducer.commandQueueSlot.release();
                }
                finally
                {
                    NewCommandProducer.commandQueueLock.unlock();
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD PROCESSING DELETE COMMAND");
                exception.printStackTrace();
            }
        }
    }

    public static class ProcessRenameCommand implements Runnable
    {
        private Command command;
        private SocketChannel socketChannel;

        public ProcessRenameCommand(Command command, SocketChannel socketChannel)
        {
            this.command = command;
            this.socketChannel = socketChannel;
        }
        public void run()
        {
            try
            {
                Response response = new Response(true, "RECEIVED RENAME COMMAND");
                sendResponse(response,socketChannel);
                try
                {
                    NewCommandProducer.commandQueueLock.lock();
                    NewCommandProducer.commandQueue.remove(command);
                    NewCommandProducer.commandQueueSlot.release();
                }
                finally
                {
                    NewCommandProducer.commandQueueLock.unlock();
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD PROCESSING RENAME COMMAND");
                exception.printStackTrace();
            }
        }
    }

    public static class ProcessDownloadCommand implements Runnable
    {
        private Command command;
        private SocketChannel socketChannel;

        public ProcessDownloadCommand(Command command, SocketChannel socketChannel)
        {
            this.command = command;
            this.socketChannel = socketChannel;
        }
        public void run()
        {
            try
            {
                Response response = new Response(true, "RECEIVED DOWNLOAD COMMAND");
                sendResponse(response,socketChannel);
                try
                {
                    NewCommandProducer.commandQueueLock.lock();
                    NewCommandProducer.commandQueue.remove(command);
                    NewCommandProducer.commandQueueSlot.release();
                }
                finally
                {
                    NewCommandProducer.commandQueueLock.unlock();
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD PROCESSING DOWNLOAD COMMAND");
                exception.printStackTrace();
            }
        }
    }

    public static class ProcessUploadCommand implements Runnable
    {
        private Command command;
        private SocketChannel socketChannel;

        public ProcessUploadCommand(Command command, SocketChannel socketChannel)
        {
            this.command = command;
            this.socketChannel = socketChannel;
        }
        public void run()
        {
            try
            {
                Response response = new Response(true, "RECEIVED UPLOAD COMMAND");
                sendResponse(response,socketChannel);
                try
                {
                    NewCommandProducer.commandQueueLock.lock();
                    NewCommandProducer.commandQueue.remove(command);
                    NewCommandProducer.commandQueueSlot.release();
                }
                finally
                {
                    NewCommandProducer.commandQueueLock.unlock();
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD PROCESSING UPLOAD COMMAND");
                exception.printStackTrace();
            }
        }
    }
}