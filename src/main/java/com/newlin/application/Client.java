package com.newlin.application;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
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
    public static String DESTINATION_IP;
    public static Command CURRENT_COMMAND = new Command(Command.Actions.STARTUP);

    public static void main(String[] args)
    {
        try
        {
            startup();

            System.out.println(log("CONNECTED TO: " + DESTINATION_IP));

            EXECUTOR.submit(new UserInputListener());

            try
            {
                isConnectedLock.lock();
                disconnected.await();
                System.out.println(log("DISCONNECTED"));
            }
            finally
            {
                isConnectedLock.unlock();
            }

            shutdown();
        }
        catch(InterruptedException exception)
        {
            System.err.println(log("EXCEPTION IN MAIN THREAD"));
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void startup()
    {
        try
        {
            Thread.sleep(2000);
            System.out.print("STARTING CLIENT");
            Thread.sleep(500);
            System.out.print(".");
            Thread.sleep(500);
            System.out.print(".");
            Thread.sleep(500);
            System.out.print(".\n");
            Thread.sleep(500);

            connect();
        }
        catch(InterruptedException exception)
        {
            System.err.println(log("EXCEPTION DURING STARTUP"));
            exception.printStackTrace();
            System.exit(0);
        }
    }
    public static void connect() throws InterruptedException
    {
        try
        {
            do
            {
                System.out.println(log("ENTER A COMMAND: "));
                switch (scanner.nextLine().toUpperCase())
                {
                    case "CONNECT":
                        System.out.println(log("TYPE IP ADDRESS TO CONNECT TO: "));
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
                            System.out.println(receivedResponse.getResponse()[0]);
                        }
                        break;

                    case "QUIT":
                        throw new InterruptedException();

                    case "FORCE QUIT":
                        System.exit(0);
                        break;

                    default:
                        System.out.println(log("VALID COMMANDS: \"CONNECT\" \"QUIT\" OR \"FORCE QUIT\""));
                        break;
                }
            }
            while(!isConnected);
        }
        catch(IOException exception)
        {
            System.err.println(log("COULDN'T CONNECT TO: " + DESTINATION_IP));
            connect();
        }
        catch(ClassNotFoundException exception)
        {
            System.err.println(log("EXCEPTION WHILE SENDING PING TO SERVER (ESTABLISHING I/O STREAMS FOR THE SOCKET)"));
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void shutdown()
    {
        try
        {
            System.err.print(log("SHUTTING DOWN"));
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
            System.err.println(log("EXCEPTION DURING SHUTDOWN"));
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static String log(String string)
    {
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String day = String.format("%02d", LocalDateTime.now().getDayOfMonth());
        String year = String.format("%02d", LocalDateTime.now().getYear());
        String date = month + "/" + day + "/" + year;

        String hour = String.format("%02d", LocalDateTime.now().getHour());
        String minute = String.format("%02d", LocalDateTime.now().getMinute());
        String second = String.format("%02d", LocalDateTime.now().getSecond());
        String time = hour + ":" + minute + ":" + second;

        String DateTime = date + " " + time;
        return "[" + DateTime + "] [CLIENT] " + string;
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
                    System.out.println(log("ENTER A COMMAND: "));
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
                            System.out.println(log("FILENAME TO DELETE: "));
                            String filenameToDelete = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.DELETE,filenameToDelete);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "RENAME":
                            System.out.println(log("FILENAME TO RENAME: "));
                            String filenameToRename = scanner.nextLine();
                            System.out.println(log("NEW FILENAME: "));
                            String newFilename = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.RENAME,filenameToRename,newFilename);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "DOWNLOAD":
                            System.out.println(log("FILENAME TO DOWNLOAD: "));
                            String filenameToDownload = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.DOWNLOAD,filenameToDownload);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        case "UPLOAD":
                            System.out.println(log("FILENAME TO UPLOAD: "));
                            String filenameToUpload = scanner.nextLine();
                            CURRENT_COMMAND = new Command(Command.Actions.UPLOAD,filenameToUpload);
                            EXECUTOR.submit(new ServeOutgoingRequest(CURRENT_COMMAND));
                            break;

                        default:
                            System.out.println(log("VALID COMMANDS: \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT\" OR \"FORCE QUIT\""));
                            break;
                    }
                }
            }
            catch(InterruptedException exception)
            {
                System.err.println(log("EXCEPTION IN USER INPUT THREAD"));
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
