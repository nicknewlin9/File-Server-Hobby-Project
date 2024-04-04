import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Client
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
            System.out.println("STARTING UP CLIENT...");
            Thread.sleep(2000);

            //NEED TO MAKE INSTANCE OF TASKMANAGER BEFORE ACCESSING ITS STATIC RUNNABLE/CALLABLE CLASSES
            new TaskManager(Executors.newFixedThreadPool(NUM_THREADS),new Command("STARTUP"));

            //ACTIVE THREAD
            TaskManager.executor.submit(new TaskManager.OpenStatusListener());

            ServerSocket listenSocket = TaskManager.executor.submit(new TaskManager.OpenServerSocket(CLIENT_PORT)).get();

            //ACTIVE THREAD
            TaskManager.executor.submit(new TaskManager.ListenForConnections(listenSocket));

            Socket serverConnection = TaskManager.executor.submit(new TaskManager.ConnectToHost(SERVER_IP,SERVER_PORT)).get();

            Scanner scanner = new Scanner(System.in);
            while(serverConnection.isConnected() && !listenSocket.isClosed())
            {
                System.out.println("CURRENT COMMAND: " + TaskManager.currentCommand.getArgument(0));
                setCurrentCommand(scanner);
                TaskManager.executor.submit(new TaskManager.SubmitRequest(serverConnection,TaskManager.currentCommand));
            }
            System.out.println("CONNECTION IS CLOSED. EXITING THE PROGRAM.");
            scanner.close();
            TaskManager.executor.close();
            TaskManager.currentCommand = new Command("QUIT CLIENT");
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

                case "LIST":
                    TaskManager.currentCommand = new Command("LIST");
                    break;

                case "DELETE":
                    System.out.println("TYPE FILENAME TO DELETE: ");
                    String filenameToDelete = scanner.nextLine();
                    TaskManager.currentCommand = new Command("DELETE",filenameToDelete);
                    break;

                case "RENAME":
                    System.out.println("TYPE FILENAME TO RENAME: ");
                    String filenameToRename = scanner.nextLine();
                    System.out.println("TYPE NEW FILENAME: ");
                    String newFilename = scanner.nextLine();
                    TaskManager.currentCommand = new Command("RENAME",filenameToRename,newFilename);
                    break;

                case "DOWNLOAD":
                    System.out.println("TYPE FILENAME TO DOWNLOAD: ");
                    String filenameToDownload = scanner.nextLine();
                    TaskManager.currentCommand = new Command("DOWNLOAD",filenameToDownload);
                    break;

                case "UPLOAD":
                    System.out.println("TYPE FILENAME TO UPLOAD: ");
                    String filenameToUpload = scanner.nextLine();
                    TaskManager.currentCommand = new Command("UPLOAD",filenameToUpload);
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
                    System.out.println("\nVALID COMMANDS: \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT CLIENT\" \"QUIT SERVER\" OR \"FORCE QUIT\"");
                    TaskManager.currentCommand = new Command("INVALID");
                    break;
            }
        }
        while(TaskManager.currentCommand.equals("INVALID"));
    }
}
