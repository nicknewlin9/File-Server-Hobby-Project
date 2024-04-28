package com.newlin.application.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Server
{
    public static final int MAX_CLIENTS = 3;
    public static final int MAX_REQUESTS = 6;
    public static final int LISTENING_PORT = 3001;

    public static final int NUM_THREADS = 2 + MAX_CLIENTS + MAX_REQUESTS;
    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(NUM_THREADS);

    public static Semaphore queueSlot = new Semaphore(MAX_CLIENTS,true);
    public static Semaphore commandSlot = new Semaphore(MAX_REQUESTS,true);
    public static boolean isOnline = false;
    public static ReentrantLock isOnlineLock = new ReentrantLock();
    public static Condition offline = isOnlineLock.newCondition();

    public static void main(String[] args)
    {
        try
        {
            startup();

            System.out.println(log("SERVER OPEN AND LISTENING ON PORT: " + LISTENING_PORT));

            EXECUTOR.submit(new UserInputListener());

            try
            {
                isOnlineLock.lock();
                offline.await();
            }
            finally
            {
                isOnlineLock.unlock();
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
            System.out.print("STARTING SERVER");
            Thread.sleep(500);
            System.out.print(".");
            Thread.sleep(500);
            System.out.print(".");
            Thread.sleep(500);
            System.out.print(".\n");
            Thread.sleep(500);

            EXECUTOR.submit(new AcceptIncomingRequests());
        }
        catch(InterruptedException exception)
        {
            System.err.println(log("EXCEPTION DURING STARTUP"));
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
        return "[" + DateTime + "] [SERVER] " + string;
    }

    public static class UserInputListener implements Runnable
    {
        public void run()
        {
            try(Scanner scanner = new Scanner(System.in))
            {
                while(isOnline)
                {
                    System.out.println(log("ENTER A COMMAND: "));
                    switch(scanner.nextLine().toUpperCase())
                    {
                        case "QUIT":
                            return;

                        case "FORCE QUIT":
                            System.exit(0);
                            break;

                        default:
                            System.out.println(log("VALID COMMANDS: \"QUIT\" OR \"FORCE QUIT\""));
                            break;
                    }
                }
            }
            finally
            {
                try
                {
                    isOnlineLock.lock();
                    offline.signal();
                }
                finally
                {
                    isOnlineLock.unlock();
                }
            }
        }
    }

    public static class AcceptIncomingRequests implements Runnable
    {
        public void run()
        {
            try
            {
                ServerSocket incomingConnectionsListenSocket = new ServerSocket(LISTENING_PORT);
                if(!incomingConnectionsListenSocket.isClosed())
                {
                    try
                    {
                        isOnlineLock.lock();
                        isOnline = true;
                    }
                    finally
                    {
                        isOnlineLock.unlock();
                    }
                }
                while(isOnline)
                {
                    if(incomingConnectionsListenSocket.isClosed())
                    {
                        try
                        {
                            isOnlineLock.lock();
                            isOnline = false;
                        }
                        finally
                        {
                            isOnlineLock.unlock();
                        }
                        return; //SKIPS TO FINALLY BLOCK
                    }

                    queueSlot.acquire();
                    Socket socket = incomingConnectionsListenSocket.accept();
                    System.out.println(log("NEW CONNECTION FROM: " + socket.getInetAddress().getHostName()));
                    EXECUTOR.submit(new ServeIncomingRequest(socket));
                }
            }
            catch(InterruptedException | IOException exception)
            {
                System.err.println(log("EXCEPTION IN ACCEPT INCOMING REQUESTS THREAD"));
                exception.printStackTrace();
            }
            finally
            {
                try
                {
                    isOnlineLock.lock();
                    offline.signal();
                }
                finally
                {
                    isOnlineLock.unlock();
                }
            }
        }
    }
}