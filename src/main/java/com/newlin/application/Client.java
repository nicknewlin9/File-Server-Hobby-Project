package com.newlin.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
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
    public static final int LISTENING_PORT = 3001;

    public static final int NUM_THREADS = 4 + MAX_REQUESTS;
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS);

    public static Queue<ProcessCommand> commandQueue = new LinkedList<>();
    public static ReentrantLock commandQueueLock = new ReentrantLock();
    public static Semaphore commandQueueSlot = new Semaphore(MAX_REQUESTS,true);
    public static Semaphore commandTyped = new Semaphore(0,true);

    public static boolean isConnected = false;
    public static ReentrantLock isConnectedLock = new ReentrantLock();

    public static Socket socket = new Socket();
    public static ObjectInputStream objectInputStream;
    public static ObjectOutputStream objectOutputStream;

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

            //CREATES AN ACTIVE THREAD FOR ACCEPTING AND PROCESSING USER INPUT
            EXECUTOR.submit(new UserInputListener());

        }
        catch(InterruptedException exception)
        {
            System.err.println("EXCEPTION DURING STARTUP");
            exception.printStackTrace();
        }
    }

    public static class UserInputListener implements Runnable
    {
        public static Command CURRENT_COMMAND = new Command(Actions.STARTUP);

        public void run()
        {
            Scanner scanner = new Scanner(System.in);
            do
            {
                System.out.println("\nENTER A COMMAND: ");
                switch (scanner.nextLine().toUpperCase())
                {
                    case "CONNECT":
                        System.out.println("TYPE IP ADDRESS TO CONNECT TO: ");
                        String DESTINATION_IP = scanner.nextLine();
                        Connect(DESTINATION_IP);
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
            do
            {
                System.out.println("\nENTER A COMMAND: ");
                switch(scanner.nextLine().toUpperCase())
                {
                    case "CONNECT":
                        System.out.println("TYPE IP ADDRESS TO CONNECT TO: ");
                        String DESTINATION_IP = scanner.nextLine();
                        Connect(DESTINATION_IP);
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
            System.out.println("DISCONNECTED FROM SERVER");
            scanner.close();
            Thread.currentThread().interrupt();
        }
    }

    public static void Connect(String DESTINATION_IP)
    {
        try
        {
            socket.connect(new InetSocketAddress(DESTINATION_IP,LISTENING_PORT));
            if(socket.isConnected())
            {
                try
                {
                    isConnectedLock.lock();
                    isConnected = true;
                    System.out.println("CONNECTED TO: " + socket.getInetAddress().getHostName() + "\nON PORT: " + LISTENING_PORT);
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                    //THREAD ONLY GETS CREATED WHEN CLIENT CONNECTS TO SERVER SUCCESSFULLY
                    //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE AN AVAILABLE COMMAND QUEUE SLOT
                    //THEN BLOCKS UNTIL IT CAN ACQUIRE A NEW COMMAND TYPED
                    //GETS THE COMMAND AND ADDS THE SERVECOMMAND TO THE COMMAND QUEUE
                    //SUBMITS SERVE COMMAND TO THE EXECUTOR
                    EXECUTOR.submit(new AcceptCommands());
                }
                finally
                {
                    isConnectedLock.unlock();
                }
            }
            else if(!socket.isConnected())
            {
                System.out.println("COULDN'T CONNECT TO: " + socket.getInetAddress().getHostName() + " ON PORT: " + LISTENING_PORT);
            }
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static class AcceptCommands implements Runnable
    {
        public void run()
        {
            do
            {
                try
                {
                    commandQueueSlot.acquire();
                    commandTyped.acquire();
                    Command command = UserInputListener.CURRENT_COMMAND;
                    ProcessCommand processCommand = new ProcessCommand(command);
                    commandQueueLock.lock();
                    commandQueue.add(processCommand);
                    EXECUTOR.submit(new ProcessCommand(command));
                }
                catch(InterruptedException exception)
                {
                    exception.printStackTrace();
                }
                finally
                {
                    commandQueueLock.unlock();
                }
            }
            while(isConnected);
        }
    }

    public static class ProcessCommand implements Runnable
    {
        public Command command;

        public ProcessCommand(Command command)
        {
            this.command = command;
        }

        public void run()
        {
            try
            {
                do
                {
                    if(!socket.isConnected())
                    {
                        try
                        {
                            isConnectedLock.lock();
                            isConnected = false;
                            commandQueueLock.lock();
                            commandQueue.remove(this);
                            commandQueueSlot.release();
                        }
                        finally
                        {
                            commandQueueLock.unlock();
                            isConnectedLock.unlock();
                            Thread.currentThread().interrupt();
                        }
                    }
                    try
                    {
                        objectOutputStream.writeObject(command);
                        objectOutputStream.flush();

                        Response response = (Response) objectInputStream.readObject();
                        System.out.println(response);
                        commandQueueLock.lock();
                        commandQueue.remove(this);
                    }
                    finally
                    {
                        commandQueueLock.unlock();
                    }
                }
                while(isConnected);
            }
            catch(IOException | ClassNotFoundException exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
