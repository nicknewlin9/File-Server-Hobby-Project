package com.newlin.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Client
{
    public static final int LISTENING_PORT = 3001;

    public static final int NUM_THREADS = 4 + Server.MAX_REQUESTS;
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS);

    public static Scanner scanner = new Scanner(System.in);
    public static Socket socket;
    public static ObjectOutputStream objectOutputStream;
    public static ObjectInputStream objectInputStream;
    public static Semaphore queueSlot = new Semaphore(Server.MAX_REQUESTS,true);
    public static boolean isConnected = false;
    public static ReentrantLock isConnectedLock = new ReentrantLock();
    public static Condition disconnected = isConnectedLock.newCondition();
    public static Condition commandTyped = isConnectedLock.newCondition();
    public static String DESTINATION_IP;
    public static Command CURRENT_COMMAND = new Command(Command.Actions.STARTUP);

    public static void main(String[] args)
    {
        try
        {
            startup();

            System.out.println("[CLIENT] CONNECTED TO: " + DESTINATION_IP);

            EXECUTOR.submit(new UserInputListener());

            try
            {
                isConnectedLock.lock();
                disconnected.await();
                System.out.println("[CLIENT] DISCONNECTED");
            }
            finally
            {
                isConnectedLock.unlock();
            }

            shutdown();
        }
        catch(InterruptedException exception)
        {
            System.err.println("[CLIENT] EXCEPTION IN MAIN THREAD");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void startup()
    {
        try
        {
            System.out.print("[CLIENT] STARTING CLIENT");
            Thread.sleep(1000);
            System.out.print(".");
            Thread.sleep(1000);
            System.out.print(".");
            Thread.sleep(1000);
            System.out.print(".\n");

            do
            {
                System.out.println("\n[CLIENT] ENTER A COMMAND: ");
                switch (scanner.nextLine().toUpperCase())
                {
                    case "CONNECT":
                        System.out.println("[CLIENT] TYPE IP ADDRESS TO CONNECT TO: ");
                        DESTINATION_IP = scanner.nextLine();
                        socket = new Socket();
                        socket.connect(new InetSocketAddress(DESTINATION_IP,LISTENING_PORT));
                        if(socket.isConnected())
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

                            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                            objectOutputStream.writeObject(new Command(Command.Actions.CONNECT));
                            objectOutputStream.flush();

                            objectInputStream = new ObjectInputStream(socket.getInputStream());
                            Response receivedResponse = (Response) objectInputStream.readObject();

                            //PROCESS RESPONSE
                            System.out.println(receivedResponse);
                        }
                        break;

                    case "QUIT":
                        throw new InterruptedException();

                    case "FORCE QUIT":
                        System.exit(0);
                        break;

                    default:
                        System.out.println("\n[CLIENT] VALID COMMANDS: \"CONNECT\" \"QUIT\" OR \"FORCE QUIT\"");
                        break;
                }
            }
            while(!isConnected);
        }
        catch(InterruptedException exception)
        {
            System.err.println("[CLIENT] EXCEPTION DURING STARTUP");
            exception.printStackTrace();
            System.exit(0);
        }
        catch(IOException exception)
        {
            System.err.println("[CLIENT] COULDN'T CONNECT TO: " + DESTINATION_IP);
            exception.printStackTrace();
            System.exit(0);
        }
        catch(ClassNotFoundException exception)
        {
            System.err.println("[CLIENT] EXCEPTION WHILE SENDING PING TO SERVER (ESTABLISHING I/O STREAMS FOR THE SOCKET)");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void shutdown()
    {
        try
        {
            System.err.print("[CLIENT] SHUTTING DOWN");
            Thread.sleep(1000);
            System.err.print(".");
            Thread.sleep(1000);
            System.err.print(".");
            Thread.sleep(1000);
            System.err.print(".\n");
            Thread.sleep(1000);

            System.exit(0);
        }
        catch(InterruptedException exception)
        {
            System.err.println("[CLIENT] EXCEPTION DURING SHUTDOWN");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static class UserInputListener implements Runnable
    {
        public void run()
        {
            try
            {
                while(isConnected)
                {
                    if(!socket.isConnected())
                    {
                        isConnected = false;
                        return; //SKIPS TO FINALLY BLOCK
                    }
                    queueSlot.acquire();
                    System.out.println("\n[CLIENT] ENTER A COMMAND: ");
                    switch(scanner.nextLine().toUpperCase())
                    {
                        case "QUIT":
                            return;

                        case "FORCE QUIT":
                            System.exit(0);
                            break;

                        case "LIST":
                            CURRENT_COMMAND = new Command(Command.Actions.LIST);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "DELETE":
                            System.out.println("[CLIENT] FILENAME TO DELETE: ");
                            String filenameToDelete = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.DELETE,filenameToDelete);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "RENAME":
                            System.out.println("[CLIENT] FILENAME TO RENAME: ");
                            String filenameToRename = scanner.nextLine();
                            System.out.println("[CLIENT] NEW FILENAME: ");
                            String newFilename = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.RENAME,filenameToRename,newFilename);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "DOWNLOAD":
                            System.out.println("[CLIENT] FILENAME TO DOWNLOAD: ");
                            String filenameToDownload = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.DOWNLOAD,filenameToDownload);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "UPLOAD":
                            System.out.println("[CLIENT] FILENAME TO UPLOAD: ");
                            String filenameToUpload = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.UPLOAD,filenameToUpload);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        default:
                            System.out.println("\n[CLIENT] VALID COMMANDS: \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT\" OR \"FORCE QUIT\"");
                            break;
                    }
                }
            }
            catch(InterruptedException exception)
            {
                System.err.println("[CLIENT] EXCEPTION IN USER INPUT THREAD");
                exception.printStackTrace();
            }
            finally
            {
                try
                {
                    isConnectedLock.lock();
                    disconnected.signal();
                }
                finally
                {
                    isConnectedLock.unlock();
                }
            }
        }
    }
}
