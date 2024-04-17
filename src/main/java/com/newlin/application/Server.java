package com.newlin.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
    public static final int LISTENING_PORT = 3001;

    public static final int NUM_THREADS = 3 + (MAX_CONNECTED_CLIENTS * MAX_REQUESTS_PER_CLIENT);
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS);

    public static Queue<ServeClient> clientQueue = new LinkedList<>();
    public static ReentrantLock clientQueueLock = new ReentrantLock();
    public static Semaphore clientQueueSlot = new Semaphore(MAX_CONNECTED_CLIENTS,true);

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

            //CREATES A THREAD THAT BLOCKS UNTIL IT CAN ACQUIRE AN AVAILABLE CLIENT QUEUE SLOT
            //THEN BLOCKS UNTIL IT RECEIVES A NEW CLIENT CONNECTION
            //ADDS THE NEW CLIENT CONNECTION TO THE QUEUE
            //SUBMITS CLIENT CONNECTION TO THE EXECUTOR
            EXECUTOR.submit(new AcceptClients());

            //CREATES AN ACTIVE THREAD FOR ACCEPTING AND PROCESSING USER INPUT
            EXECUTOR.submit(new UserInputListener());
        }
        catch(Exception exception)
        {
            System.err.println("EXCEPTION DURING STARTUP");
            exception.printStackTrace();
        }
    }

    public static class AcceptClients implements Runnable
    {
        public void run()
        {
            try(ServerSocket incomingConnectionsListenSocket = new ServerSocket(LISTENING_PORT))
            {
                if(!incomingConnectionsListenSocket.isClosed())
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
                    if(incomingConnectionsListenSocket.isClosed())
                    {
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
                    try
                    {
                        clientQueueSlot.acquire();
                        Socket newConnection = incomingConnectionsListenSocket.accept();
                        ServeClient newClient = new ServeClient(newConnection);
                        clientQueueLock.lock();
                        clientQueue.add(newClient);
                        EXECUTOR.submit(newClient);
                    }
                    finally
                    {
                        clientQueueLock.unlock();
                    }
                }
                while(isOnline);
            }
            catch(InterruptedException | IOException exception)
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

    public static class ServeClient implements Runnable
    {
        public Socket socket;
        public ObjectInputStream objectInputStream;
        public ObjectOutputStream objectOutputStream;

        public Queue<ServeCommand> commandQueue = new LinkedList<>();
        public ReentrantLock commandQueueLock = new ReentrantLock();
        public Semaphore commandQueueSlot = new Semaphore(MAX_REQUESTS_PER_CLIENT,true);

        public boolean isConnected = false;
        public ReentrantLock isConnectedLock = new ReentrantLock();

        public ServeClient(Socket socket)
        {
            try
            {
                this.socket = socket;
                if(this.socket.isConnected())
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
                this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
                this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }
        }

        public void run()
        {
            try
            {
                System.out.println("RECEIVED NEW CONNECTION FROM: " + socket.getInetAddress().getHostName());
                do
                {
                    if(!socket.isConnected())
                    {
                        try
                        {
                            isConnectedLock.lock();
                            isConnected = false;
                            clientQueueLock.lock();
                            clientQueue.remove(this);
                            clientQueueSlot.release();
                        }
                        finally
                        {
                            clientQueueLock.unlock();
                            isConnectedLock.unlock();
                            Thread.currentThread().interrupt();
                        }
                    }
                    try
                    {
                        commandQueueSlot.acquire();
                        Command receivedCommand = (Command) objectInputStream.readObject();
                        ServeClient serveClient = this;
                        ServeCommand newCommand = new ServeCommand(serveClient, receivedCommand);
                        commandQueueLock.lock();
                        commandQueue.add(newCommand);
                        EXECUTOR.submit(newCommand);
                    }
                    finally
                    {
                        commandQueueLock.unlock();
                    }
                }
                while(isConnected);
            }
            catch(InterruptedException | IOException | ClassNotFoundException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static class ServeCommand implements Runnable
    {
        public ServeClient client;
        public Command command;

        public ServeCommand(ServeClient client, Command command)
        {
            this.client = client;
            this.command = command;
        }

        public void run()
        {
            try
            {
                System.out.println("RECEIVED COMMAND: " + command + "\nFROM: " + client.socket.getInetAddress().getHostName());
                ServeCommand serveCommand = this;
                switch(command.getAction())
                {
                    case Actions.LIST:
                        EXECUTOR.submit(new ProcessListCommand(serveCommand, command));

                    case Actions.DELETE:
                        EXECUTOR.submit(new ProcessDeleteCommand(serveCommand, command));

                    case Actions.RENAME:
                        EXECUTOR.submit(new ProcessRenameCommand(serveCommand, command));

                    case Actions.DOWNLOAD:
                        EXECUTOR.submit(new ProcessDownloadCommand(serveCommand, command));

                    case Actions.UPLOAD:
                        EXECUTOR.submit(new ProcessUploadCommand(serveCommand, command));

                    default:
                        Response response = new Response(false, "RECEIVED INVALID COMMAND");
                        client.objectOutputStream.writeObject(response);
                        client.objectOutputStream.flush();
                        try
                        {
                            client.commandQueueLock.lock();
                            client.commandQueue.remove(serveCommand);
                            client.commandQueueSlot.release();
                        }
                        finally
                        {
                            client.commandQueueLock.unlock();
                        }
                }
            }
            catch(IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    public static class ProcessListCommand implements Runnable
    {
        public ServeCommand serveCommand;
        public Command command;

        public ProcessListCommand(ServeCommand serveCommand, Command command)
        {
            this.serveCommand = serveCommand;
            this.command = command;
        }
        public void run()
        {
            try
            {
                //GENERATE RESPONSE
                Response response = new Response(true, "RECEIVED LIST COMMAND");
                serveCommand.client.objectOutputStream.writeObject(response);
                serveCommand.client.objectOutputStream.flush();
                try
                {
                    serveCommand.client.commandQueueLock.lock();
                    serveCommand.client.commandQueue.remove(serveCommand);
                    serveCommand.client.commandQueueSlot.release();
                }
                finally
                {
                    serveCommand.client.commandQueueLock.unlock();
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
        public ServeCommand serveCommand;
        public Command command;

        public ProcessDeleteCommand(ServeCommand serveCommand, Command command)
        {
            this.serveCommand = serveCommand;
            this.command = command;
        }
        public void run()
        {
            try
            {
                //GENERATE RESPONSE
                Response response = new Response(true, "RECEIVED DELETE COMMAND");
                serveCommand.client.objectOutputStream.writeObject(response);
                serveCommand.client.objectOutputStream.flush();
                try
                {
                    serveCommand.client.commandQueueLock.lock();
                    serveCommand.client.commandQueue.remove(serveCommand);
                    serveCommand.client.commandQueueSlot.release();
                }
                finally
                {
                    serveCommand.client.commandQueueLock.unlock();
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
        public ServeCommand serveCommand;
        public Command command;

        public ProcessRenameCommand(ServeCommand serveCommand, Command command)
        {
            this.serveCommand = serveCommand;
            this.command = command;
        }
        public void run()
        {
            try
            {
                //GENERATE RESPONSE
                Response response = new Response(true, "RECEIVED RENAME COMMAND");
                serveCommand.client.objectOutputStream.writeObject(response);
                serveCommand.client.objectOutputStream.flush();
                try
                {
                    serveCommand.client.commandQueueLock.lock();
                    serveCommand.client.commandQueue.remove(serveCommand);
                    serveCommand.client.commandQueueSlot.release();
                }
                finally
                {
                    serveCommand.client.commandQueueLock.unlock();
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
        public ServeCommand serveCommand;
        public Command command;

        public ProcessDownloadCommand(ServeCommand serveCommand, Command command)
        {
            this.serveCommand = serveCommand;
            this.command = command;
        }
        public void run()
        {
            try
            {
                //GENERATE RESPONSE
                Response response = new Response(true, "RECEIVED DOWNLOAD COMMAND");
                serveCommand.client.objectOutputStream.writeObject(response);
                serveCommand.client.objectOutputStream.flush();
                try
                {
                    serveCommand.client.commandQueueLock.lock();
                    serveCommand.client.commandQueue.remove(serveCommand);
                    serveCommand.client.commandQueueSlot.release();
                }
                finally
                {
                    serveCommand.client.commandQueueLock.unlock();
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
        public ServeCommand serveCommand;
        public Command command;

        public ProcessUploadCommand(ServeCommand serveCommand, Command command)
        {
            this.serveCommand = serveCommand;
            this.command = command;
        }
        public void run()
        {
            try
            {
                //GENERATE RESPONSE
                Response response = new Response(true, "RECEIVED UPLOAD COMMAND");
                serveCommand.client.objectOutputStream.writeObject(response);
                serveCommand.client.objectOutputStream.flush();
                try
                {
                    serveCommand.client.commandQueueLock.lock();
                    serveCommand.client.commandQueue.remove(serveCommand);
                    serveCommand.client.commandQueueSlot.release();
                }
                finally
                {
                    serveCommand.client.commandQueueLock.unlock();
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