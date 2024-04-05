import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.Executors;

public class Server
{
    private static final String DESTINATION_IP = "localhost";
    private static final int DESTINATION_PORT = 25566; //SHOULD BE THE SAME PORT WHEN ON DIFFERENT HOSTS
    private static final String SOURCE_IP = "localhost"; //DELETE WHEN FINISHED
    private static final int SOURCE_PORT = 25565; //DELETE WHEN FINISHED //NEED DIFFERENT PORT NUMBER BECAUSE CLIENT AND SERVER IS ON LOCALHOST
    private static final int NUM_THREADS = 4;

    public static void main(String[] args)
    {
        try
        {
            //STARTUP
            System.out.println("STARTING UP...");

            //SET TASK MANAGER WITH NEW EXECUTOR AND STARTUP COMMAND
            new TaskManager(Executors.newFixedThreadPool(NUM_THREADS),new Command("STARTUP"));

            //ACTIVATE THREAD FOR CHECKING PROGRAM STATUS
            TaskManager.executor.submit(new TaskManager.OpenStatusListener());

            //OPENS A LISTEN SOCKET FOR INCOMING CONNECTIONS
            ServerSocket incomingConnectionsListener = TaskManager.constructIncomingConnectionsListener(SOURCE_IP, SOURCE_PORT);

            //ACTIVATE THREAD FOR LISTENING FOR NEW CONNECTIONS
            //THIS THREAD SHOULD ACCEPT CONNECTIONS AND ACTIVATE A NEW THREAD FOR SERVING THAT CONNECTION WHICH WILL ACTIVATE ANOTHER THREAD FOR A PACKET LISTENER
            TaskManager.executor.submit(new TaskManager.AcceptConnections(incomingConnectionsListener));

            if (incomingConnectionsListener != null && !incomingConnectionsListener.isClosed()) //IS OPEN FOR NEW CONNECTIONS
            {
                //STARTUP SUCCESSFUL
                System.out.println("STARTUP SUCCESSFUL");

                //OPENS A SCANNER FOR USER INPUT
                Scanner scanner = new Scanner(System.in);

                //ACTIVATE THREAD FOR LISTENING FOR USER INPUT
                TaskManager.executor.submit(new TaskManager.ServerInputListener(scanner));
            }
            else
            {
                //STARTUP UNSUCCESSFUL
                System.err.println("STARTUP UNSUCCESSFUL");
                System.exit(1);
            }
        }
        catch(Exception exception)
        {
            System.err.println("EXCEPTION IN MAIN THREAD");
            exception.printStackTrace();
        }
    }
    public static String getSourceIp()
    {
        return SOURCE_IP;
    }
    public static int getSourcePort()
    {
        return SOURCE_PORT;
    }
    public static String getDestinationIp()
    {
        return DESTINATION_IP;
    }
    public static int getDestinationPort()
    {
        return DESTINATION_PORT;
    }
    public static int getNumThreads()
    {
        return NUM_THREADS;
    }
    public static Command getCurrentCommand()
    {
        return TaskManager.currentCommand;
    }
}