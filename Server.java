import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server
{
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 25565;
    private static final String CLIENT_IP = "localhost";
    private static final int CLIENT_PORT = 25566;
    private static final int NUM_THREADS = 4;

    public static void main(String[] args)
    {
        try
        {
            System.out.println("STARTING UP SERVER...");
            Thread.sleep(2000);

            //NEED TO MAKE INSTANCE OF TASKMANAGER BEFORE ACCESSING ITS STATIC RUNNABLE/CALLABLE CLASSES
            new TaskManager(Executors.newFixedThreadPool(NUM_THREADS),new Command("STARTUP"));

            //ACTIVE THREAD
            TaskManager.executor.submit(new TaskManager.OpenStatusListener());

            ServerSocket listenSocket = TaskManager.executor.submit(new TaskManager.OpenServerSocket(SERVER_PORT)).get();

            //ACTIVE THREAD
            TaskManager.executor.submit(new TaskManager.ListenForConnections(listenSocket));


            Scanner scanner = new Scanner(System.in);
            while(!listenSocket.isClosed())
            {
                System.out.println("CURRENT COMMAND: " + TaskManager.currentCommand.getArgument(0));
                setCurrentCommand(scanner);
            }
            System.out.println("CONNECTION CLOSED. EXITING THE PROGRAM.");
            scanner.close();
            TaskManager.executor.close();
            TaskManager.currentCommand = new Command("QUIT SERVER");
        }
        catch(Exception exception)
        {
            System.err.println("EXCEPTION IN THREAD: " + Thread.currentThread().getName());
            exception.printStackTrace();
        }
    }
    public static String getServerIp()
    {
        return SERVER_IP;
    }
    public static int getServerPort()
    {
        return SERVER_PORT;
    }
    public static String getClientIp()
    {
        return CLIENT_IP;
    }
    public static int getClientPort()
    {
        return CLIENT_PORT;
    }
    public static int getNumThreads()
    {
        return NUM_THREADS;
    }
    public static Command getCurrentCommand()
    {
        return TaskManager.currentCommand;
    }

    public static void setCurrentCommand(Scanner scanner)
    {
        do
        {
            System.out.println("\nENTER A COMMAND: ");
            switch(scanner.nextLine().toUpperCase())
            {
                case "STARTUP":
                    break;

                case "QUIT CLIENT":
                    TaskManager.currentCommand = new Command("QUIT CLIENT");
                    break;

                case "QUIT SERVER":
                    TaskManager.currentCommand = new Command("QUIT SERVER");
                    break;

                case "FORCE QUIT":
                    TaskManager.currentCommand = new Command("FORCE QUIT");
                    break;

                default:
                    System.out.println("\nVALID COMMANDS: \"QUIT CLIENT\" \"QUIT SERVER\" OR \"FORCE QUIT\"");
                    TaskManager.currentCommand = new Command("INVALID");
                    break;
            }
        }
        while(TaskManager.currentCommand.equals("INVALID"));
    }
}