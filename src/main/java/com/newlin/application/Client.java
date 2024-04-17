package com.newlin.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Client
{
    public static final int MAX_REQUESTS = 3;

    public static final int NUM_THREADS = 3 + MAX_REQUESTS;

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS);

    public static final int LISTENING_PORT = 3001;

    public static Queue<Command> commandQueue = new LinkedList<>();
    public static ReentrantLock commandQueueLock = new ReentrantLock();
    public static Semaphore commandQueueSlot = new Semaphore(MAX_REQUESTS,true);
    public static Semaphore commandRequest = new Semaphore(0,true);
    public static Semaphore commandTyped = new Semaphore(0,true);

    public static boolean isConnected = false;
    public static ReentrantLock isConnectedLock = new ReentrantLock();

    public static SocketChannel socketChannel;

    public static void main(String[] args)
    {
        try
        {
            Thread.sleep(1000);
            System.out.print("STARTING CLIENT");
            Thread.sleep(1000);
            System.out.print(".");
            Thread.sleep(1000);
            System.out.print(".");
            Thread.sleep(1000);
            System.out.print(".\n");
            Thread.sleep(1000);

            //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE AN AVAILABLE COMMAND QUEUE SLOT
            //THEN BLOCKS UNTIL IT RECEIVES A NEW COMMAND
            //ADDS THE NEW COMMAND TO THE QUEUE
            //RELEASES A PERMIT TO THE CONSUMER
            EXECUTOR.submit(new CommandProducer());

            //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE A NEW COMMAND
            //SUBMITS THAT NEW COMMAND TO THE EXECUTOR FOR PROCESSING
            EXECUTOR.submit(new CommandConsumer());

            //CREATES AN ACTIVE THREAD FOR ACCEPTING AND PROCESSING USER INPUT
            EXECUTOR.submit(new UserInputListener());
        }
        catch(Exception exception)
        {
            System.err.println("EXCEPTION DURING STARTUP");
            exception.printStackTrace();
        }
    }

    public static class CommandProducer implements Runnable
    {
        public void run()
        {
            try
            {
                do
                {
                    try
                    {
                        commandQueueSlot.acquire();
                        commandTyped.acquire();
                        Command command = UserInputListener.CURRENT_COMMAND;
                        commandQueueLock.lock();
                        commandQueue.add(command);
                        commandRequest.release();
                    }
                    finally
                    {
                        commandQueueLock.unlock();
                    }
                }
                while(isConnected);
            }
            catch(InterruptedException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static class CommandConsumer implements Runnable
    {
        public void run()
        {
            try
            {
                commandRequest.acquire();
                Command command = commandQueue.element();
                Actions action = command.getAction();
                switch(action)
                {
                    case Actions.LIST:
                        EXECUTOR.submit(new ProcessListCommand(command));

                    case Actions.DELETE:
                        EXECUTOR.submit(new ProcessDeleteCommand(command));

                    case Actions.RENAME:
                        EXECUTOR.submit(new ProcessRenameCommand(command));

                    case Actions.DOWNLOAD:
                        EXECUTOR.submit(new ProcessDownloadCommand(command));

                    case Actions.UPLOAD:
                        EXECUTOR.submit(new ProcessUploadCommand(command));

                    default:
                        Response response = new Response(false, "COMMAND NOT RECOGNIZED");
                        System.out.println(response);
                        try
                        {
                            commandQueueLock.lock();
                            commandQueue.remove(command);
                            commandQueueSlot.release();
                        }
                        finally
                        {
                            commandQueueLock.unlock();
                        }
                }
            }
            catch(InterruptedException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static class UserInputListener implements Runnable
    {
        public static Command CURRENT_COMMAND = new Command(Actions.STARTUP);

        public void run()
        {
            try(Scanner scanner = new Scanner(System.in))
            {
                do
                {
                    System.out.println("\nENTER A COMMAND: ");
                    switch (scanner.nextLine().toUpperCase())
                    {
                        case "CONNECT":
                            System.out.println("TYPE IP ADDRESS TO CONNECT TO: ");
                            String DESTINATION_IP = scanner.nextLine();
                            socketChannel.connect(new InetSocketAddress(DESTINATION_IP, LISTENING_PORT));
                            if (socketChannel.isConnected())
                            {
                                try
                                {
                                    isConnectedLock.lock();
                                    isConnected = true;
                                    System.out.println("CONNECTED TO: " + socketChannel.socket().getInetAddress().getHostName() + " ON PORT: " + LISTENING_PORT);
                                }
                                finally
                                {
                                    isConnectedLock.unlock();
                                }
                            }
                            break;

                        case "QUIT":
                            System.exit(0);
                            break;

                        case "FORCE QUIT":
                            System.exit(0);
                            break;

                        default:
                            System.out.println("\nVALID COMMANDS: \"CONNECT\" \"QUIT\" OR \"FORCE QUIT\"");
                            break;
                    }
                }
                while(!isConnected);
                try(SocketChannel socketChannel = SocketChannel.open())
                {
                    do
                    {
                        System.out.println("\nENTER A COMMAND: ");
                        switch(scanner.nextLine().toUpperCase())
                        {
                            case "CONNECT":
                                System.out.println("TYPE IP ADDRESS TO CONNECT TO: ");
                                String DESTINATION_IP = scanner.nextLine();
                                socketChannel.connect(new InetSocketAddress(DESTINATION_IP, LISTENING_PORT));
                                if (socketChannel.isConnected())
                                {
                                    try
                                    {
                                        isConnectedLock.lock();
                                        isConnected = true;
                                        System.out.println("CONNECTED TO: " + socketChannel.socket().getInetAddress().getHostName() + " ON PORT: " + LISTENING_PORT);
                                    }
                                    finally
                                    {
                                        isConnectedLock.unlock();
                                    }
                                }
                                break;

                            case "QUIT":
                                System.exit(0);
                                break;

                            case "FORCE QUIT":
                                System.exit(0);
                                break;

                            case "LIST":
                                CURRENT_COMMAND = new Command(Actions.LIST);
                                commandTyped.release();
                                break;

                            case "DELETE":
                                System.out.println("FILENAME TO DELETE: ");
                                String filenameToDelete = scanner.nextLine();
                                CURRENT_COMMAND = new Command(Actions.LIST,filenameToDelete);
                                commandTyped.release();
                                break;

                            case "RENAME":
                                System.out.println("FILENAME TO RENAME: ");
                                String filenameToRename = scanner.nextLine();
                                System.out.println("NEW FILENAME: ");
                                String newFilename = scanner.nextLine();
                                CURRENT_COMMAND = new Command(Actions.LIST,filenameToRename,newFilename);
                                commandTyped.release();
                                break;

                            case "DOWNLOAD":
                                System.out.println("FILENAME TO DOWNLOAD: ");
                                String filenameToDownload = scanner.nextLine();
                                CURRENT_COMMAND = new Command(Actions.LIST,filenameToDownload);
                                commandTyped.release();
                                break;

                            case "UPLOAD":
                                System.out.println("FILENAME TO UPLOAD: ");
                                String filenameToUpload = scanner.nextLine();
                                CURRENT_COMMAND = new Command(Actions.LIST,filenameToUpload);
                                commandTyped.release();
                                break;

                            default:
                                System.out.println("\nVALID COMMANDS: \"CONNECT\" \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT\" OR \"FORCE QUIT\"");
                                break;
                        }
                    }
                    while(isConnected);
                }
                catch(IOException exception)
                {
                    exception.printStackTrace();
                }
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static void SendCommand(Command command)
    {
        try
        {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socketChannel.socket().getOutputStream());
            objectOutputStream.writeObject(command);
            objectOutputStream.close();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static class ProcessListCommand implements Runnable
    {
        private Command command;

        public ProcessListCommand(Command command)
        {
            this.command = command;
        }
        public void run()
        {
            try
            {
                SendCommand(command);
                ObjectInputStream objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
                Response response = (Response) objectInputStream.readObject();
                System.out.println(response);
                try
                {
                    commandQueueLock.lock();
                    commandQueue.remove(command);
                    commandQueueSlot.release();
                }
                finally
                {
                    commandQueueLock.unlock();
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

        public ProcessDeleteCommand(Command command)
        {
            this.command = command;
        }
        public void run()
        {
            try
            {
                SendCommand(command);
                ObjectInputStream objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
                Response response = (Response) objectInputStream.readObject();
                System.out.println(response);
                try
                {
                    commandQueueLock.lock();
                    commandQueue.remove(command);
                    commandQueueSlot.release();
                }
                finally
                {
                    commandQueueLock.unlock();
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

        public ProcessRenameCommand(Command command)
        {
            this.command = command;
        }
        public void run()
        {
            try
            {
                SendCommand(command);
                ObjectInputStream objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
                Response response = (Response) objectInputStream.readObject();
                System.out.println(response);
                try
                {
                    commandQueueLock.lock();
                    commandQueue.remove(command);
                    commandQueueSlot.release();
                }
                finally
                {
                    commandQueueLock.unlock();
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

        public ProcessDownloadCommand(Command command)
        {
            this.command = command;
        }
        public void run()
        {
            try
            {
                SendCommand(command);
                ObjectInputStream objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
                Response response = (Response) objectInputStream.readObject();
                System.out.println(response);
                try
                {
                    commandQueueLock.lock();
                    commandQueue.remove(command);
                    commandQueueSlot.release();
                }
                finally
                {
                    commandQueueLock.unlock();
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

        public ProcessUploadCommand(Command command)
        {
            this.command = command;
        }
        public void run()
        {
            try
            {
                SendCommand(command);
                ObjectInputStream objectInputStream = new ObjectInputStream(socketChannel.socket().getInputStream());
                Response response = (Response) objectInputStream.readObject();
                System.out.println(response);
                try
                {
                    commandQueueLock.lock();
                    commandQueue.remove(command);
                    commandQueueSlot.release();
                }
                finally
                {
                    commandQueueLock.unlock();
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
