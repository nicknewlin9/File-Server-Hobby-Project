package com.newlin.application.client;

import com.newlin.util.filesystem.FileNode;
import com.newlin.util.logger.ConsoleFormatter;
import com.newlin.util.logger.LogFileFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.logging.*;

public class Client
{
    public static Logger logger;
    public static Properties properties = new Properties();
    public static FileNode rootFileNode;
    public static ExecutorService executorService;

    public static volatile boolean isOnline = false;

    public static Socket socket;
    public static ObjectInputStream objectInputStream;
    public static ObjectOutputStream objectOutputStream;
    public static Semaphore commandSlot;

    public static void main(String[] args) throws Exception
    {
        initializeLogger();

        logger.info("Starting client...");
        startup();
        logger.info("Startup successful");
        logger.info("Connected to: " + properties.getProperty("client.default.connect.ip"));

        startUserInputListener();

        //DO CLIENT STUFF

        synchronized (Client.class)
        {
            while (isOnline)
            {
                Client.class.wait();
            }
        }

        logger.info("Shutting down...");
        shutdown();
    }

    private static void startup()
    {
        logger.fine("Loading properties...");
        loadProperties();

        logger.fine("Loading executor service...");
        executorService = Executors.newFixedThreadPool(Integer.parseInt(properties.getProperty("client.max.threads")));

        logger.fine("Loading file system...");
        loadFileSystem();

        commandSlot = new Semaphore(Integer.parseInt(properties.getProperty("server.max.requests")));

        connect();
    }

    private static void initializeLogger()
    {
        logger = Logger.getLogger("CLIENT");
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
            FileHandler fileHandler = new FileHandler("logs/client_latest.log");
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new LogFileFormatter());
            logger.addHandler(fileHandler);
        }
        catch (IOException exception)
        {
            logger.warning("Can't create log file");
        }
    }

    private static void connect()
    {
        try
        {
            socket = new Socket();
            logger.info("Connecting to default ip...");
            String ip = properties.getProperty("client.default.connect.ip");
            logger.info("IP: " + ip);
            int port = Integer.parseInt(properties.getProperty("application.port"));
            logger.info("Port: " + port);

            socket.connect(new InetSocketAddress(ip,port));
            if(socket.isConnected())
            {
                setOnlineStatus(true);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                logger.info("Output stream set");
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                logger.info("Input stream set");
            }
        }
        catch(IOException exception)
        {
            logger.severe("EXCEPTION: Can't connect to default ip");
            logger.severe(exception.getMessage());
            for(StackTraceElement element : exception.getStackTrace())
            {
                logger.severe("    " + element.toString());
            }
        }
    }

    private static void loadProperties()
    {
        try (InputStream input = Client.class.getClassLoader().getResourceAsStream("config.properties"))
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

    private static void loadFileSystem()
    {
        try
        {
            Path rootPath = Paths.get(properties.getProperty("client.filesystem.directory"));

            if(Files.exists(rootPath) && Files.isDirectory(rootPath))
            {
                rootFileNode = FileNode.loadFileStructure(rootPath);
            }
            else if(!Files.exists(rootPath))
            {
                Files.createDirectory(rootPath);
            }
            else if(Files.exists(rootPath) && !Files.isDirectory(rootPath))
            {
                throw new FileSystemException("");
            }
        }
        catch(FileSystemException exception)
        {
            logger.severe("Specified path to program's root directory is not a directory");
        }
        catch(IOException exception)
        {
            logger.severe("Exception while creating root directory");
        }
    }

    private static void startUserInputListener()
    {
        new Thread(new UserInputListener()).start();
    }

    private static void shutdown()
    {
        System.exit(0);
    }

    public static void setOnlineStatus(boolean status)
    {
        isOnline = status;
        synchronized (Client.class)
        {
            Client.class.notifyAll();
        }
    }
}