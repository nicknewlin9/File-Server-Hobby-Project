package com.newlin.application.server;

import com.newlin.util.logger.ConsoleFormatter;
import com.newlin.util.logger.LogFileFormatter;
import com.newlin.util.filesystem.FileSystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.*;

public class Server
{
    public static Logger logger;
    public static Properties properties = new Properties();
    public static FileSystem fileSystem;
    public static ExecutorService executorService;

    public static boolean isOnline = false;
    public static ReentrantLock isOnlineLock = new ReentrantLock();
    public static Condition offline = isOnlineLock.newCondition();

    public static ServerSocket listenSocket;

    public static void main(String[] args) throws Exception
    {
        initializeLogger();

        logger.info("Starting server...");
        startup();
        logger.info("Startup successful");
        logger.info("Server open on port: " + Integer.parseInt(properties.getProperty("application.port")));

        startUserInputListener();

        //DO SERVER STUFF

        try
        {
            isOnlineLock.lock();
            offline.await();
        }
        finally
        {
            isOnlineLock.unlock();
        }

        logger.info("Shutting down...");
        shutdown();
    }

    private static void startup()
    {
        logger.fine("Loading properties...");
        loadProperties();

        logger.fine("Loading executor service...");
        executorService = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("server.max.threads")));

        logger.fine("Loading file system...");
        fileSystem = new FileSystem(properties.getProperty("server.filesystem.directory"));

        logger.fine("Opening listen socket...");
        openListenSocket();

        startAcceptingRequests();
        logger.fine("Now accepting client connections");
    }

    private static void initializeLogger()
    {
        logger = Logger.getLogger("SERVER");
        LogManager.getLogManager().reset();
        logger.setLevel(Level.ALL);

        //SET LOGGER OUTPUT TO CONSOLE
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new ConsoleFormatter());

        logger.addHandler(consoleHandler);

        //SET LOGGER OUTPUT TO FILE
        try
        {
            FileHandler fileHandler = new FileHandler("logs/server_latest.log");
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFileFormatter());
            logger.addHandler(fileHandler);
        }
        catch (IOException exception)
        {
            logger.warning("Can't create log file");
        }
    }

    private static void openListenSocket()
    {
        try
        {
            listenSocket = new ServerSocket(Integer.parseInt(properties.getProperty("application.port")));
            if(!listenSocket.isClosed())
            {
                setOnlineStatus(true);
            }
        }
        catch(IOException exception)
        {
            logger.severe("Can't open listen socket");
        }
    }

    private static void loadProperties()
    {
        try (InputStream input = Server.class.getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                logger.severe("Can't find config.properties");
            }
            properties.load(input);
        }
        catch (IOException exception)
        {
            logger.severe("Can't read config.properties");
        }
    }

    private static void startUserInputListener()
    {
        new Thread(new UserInputListener()).start();
    }

    private static void startAcceptingRequests()
    {
        new Thread(new AcceptIncomingRequests()).start();
    }

    private static void shutdown()
    {

    }

    public static void setOnlineStatus(boolean status)
    {
        isOnlineLock.lock();
        try
        {
            isOnline = status;
        }
        finally
        {
            isOnlineLock.unlock();
        }
    }
}
