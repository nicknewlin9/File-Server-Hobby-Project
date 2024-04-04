import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server
{
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 30000;
    private static final int NUM_THREADS = 4;
    private static Command currentCommand = new Command("STARTUP");

    public static void main(String[] args) throws ExecutionException, InterruptedException
    {
        System.out.println("Starting up server...");
        Thread.sleep(2000);

        TaskManager taskManager = new TaskManager(Executors.newFixedThreadPool(NUM_THREADS),currentCommand);

        ExecutorService executor = taskManager.getExecutor();

        //ACTIVE THREAD
        executor.submit(new TaskManager.OpenStatusListener());

        ServerSocket listenSocket = executor.submit(new TaskManager.OpenServerSocket(SERVER_PORT)).get();

        //ACTIVE THREAD
        executor.submit(new TaskManager.ListenForConnections(listenSocket));

        Scanner scanner = new Scanner(System.in);
        while(!listenSocket.isClosed())
        {
            setCurrentCommand(scanner);
        }
        scanner.close();
        executor.close();
        System.out.println("Listen channel closed. Exiting the program.");
        System.exit(0);
        //INITIATE SAFE SHUTDOWN
        //WAIT FOR PROCESSING THREADS TO STOP, THEN CLOSE ACTIVE THREADS
    }
    public static String getServerIp()
    {
        return SERVER_IP;
    }
    public static int getServerPort()
    {
        return SERVER_PORT;
    }
    public static int getNumThreads()
    {
        return NUM_THREADS;
    }
    public static Command getCurrentCommand()
    {
        return currentCommand;
    }

    public static void setCurrentCommand(Scanner scanner)
    {
        do
        {
            System.out.println("\nEnter a command: ");
            switch(scanner.nextLine().toUpperCase())
            {
                case "STARTUP":
                    break;

                case "QUIT CLIENT":
                    System.out.println("\nShutting down client...");
                    currentCommand = new Command("QUIT CLIENT");
                    break;

                case "QUIT SERVER":
                    System.out.println("\nShutting down server...");
                    currentCommand = new Command("QUIT SERVER");
                    break;

                case "FORCE QUIT":
                    currentCommand = new Command("FORCE QUIT");
                    break;

                default:
                    System.out.println("\nINVALID COMMAND\nVALID COMMANDS: \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT CLIENT\" OR \"QUIT SERVER\"");
                    currentCommand = new Command("INVALID");
                    break;
            }
        }
        while(currentCommand.equals("INVALID"));
    }
}