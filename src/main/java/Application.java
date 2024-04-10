import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Application
{
    private static Status CURRENT_STATUS;
    public static final int LISTENING_PORT = 30000; //INCOMING CONNECTIONS
    public static final int INTERNAL_PORT = 3001; //INCOMING PACKETS
    public static String LOCAL_IP_ADDRESS = "10.0.0.49"; //CAN MAKE THIS A VARIABLE ONLY ON THE SERVER
    public static String DESTINATION_IP_ADDRESS;
    private static ExecutorService EXECUTOR;

    public static void main(String[] args) //STARTUP SEQUENCE
    {
        try
        {
            if(args[0].equals("SERVER"))
            {
                System.out.print("STARTING SERVER");
                Thread.sleep(1000);
                System.out.print(".");
                Thread.sleep(1000);
                System.out.print(".");
                Thread.sleep(1000);
                System.out.print(".\n");
                Thread.sleep(1000);

                CURRENT_STATUS = Status.STARTUP;

                EXECUTOR = Executors.newFixedThreadPool(8);

                EXECUTOR.submit(new OpenStatusListener());

                EXECUTOR.submit(new AcceptIncomingConnections());

                if(!AcceptIncomingConnections.incomingConnectionsListenSocket.isClosed())
                {
                    //LOCAL_IP_ADDRESS = AcceptIncomingConnections.incomingConnectionsListenSocket.getInetAddress().getHostName();

                    System.out.println("STARTUP SUCCESSFUL");
                    System.out.println("LISTENING FOR CONNECTIONS AT IP: " + LOCAL_IP_ADDRESS);
                    System.out.println("ON PORT: " + LISTENING_PORT);

                    CURRENT_STATUS = Status.ONLINE;

                    EXECUTOR.submit(new ServerInputListener());
                }
                else
                {
                    System.err.println("STARTUP UNSUCCESSFUL");
                    System.err.println("SHUTTING DOWN...");
                    System.exit(0);
                }
            }
            else if(args[0].equals("CLIENT"))
            {
                System.out.print("STARTING CLIENT");
                Thread.sleep(1000);
                System.out.print(".");
                Thread.sleep(1000);
                System.out.print(".");
                Thread.sleep(1000);
                System.out.print(".\n");
                Thread.sleep(1000);

                CURRENT_STATUS = Status.STARTUP;

                EXECUTOR = Executors.newFixedThreadPool(4);

                EXECUTOR.submit(new OpenStatusListener());

                EXECUTOR.submit(new AcceptIncomingConnections());

                if(!AcceptIncomingConnections.incomingConnectionsListenSocket.isClosed())
                {
                    LOCAL_IP_ADDRESS = AcceptIncomingConnections.incomingConnectionsListenSocket.getInetAddress().getHostName();

                    System.out.println("STARTUP SUCCESSFUL");
                    System.out.println("LISTENING FOR CONNECTIONS AT IP: " + LOCAL_IP_ADDRESS);
                    System.out.println("ON PORT: " + LISTENING_PORT);

                    CURRENT_STATUS = Status.ONLINE;

                    EXECUTOR.submit(new ClientInputListener());
                }
                else
                {
                    System.err.println("STARTUP UNSUCCESSFUL");
                    System.err.println("SHUTTING DOWN...");
                    System.exit(0);
                }
            }
            else
            {
                System.err.println("INVALID STARTUP SYNTAX");
                System.err.println("APPLICATION <CLIENT>");
                System.err.println("APPLICATION <SERVER>");
                System.exit(0);
            }
        }
        catch(Exception exception)
        {
            CURRENT_STATUS = Status.CRITICAL_ERROR;

            System.err.println("EXCEPTION DURING STARTUP");
            System.err.println("FORCE QUITTING...");
            exception.printStackTrace();
            System.exit(0);
        }
    }

    public static void Connect(String IP_ADDRESS)
    {
        try(Socket socket = new Socket())
        {
            InetSocketAddress destinationInetSocketAddress = new InetSocketAddress(IP_ADDRESS,LISTENING_PORT);

            socket.connect(destinationInetSocketAddress);

            System.out.println("CONNECTED TO: " + destinationInetSocketAddress.getHostName());
            System.out.println("VIA LISTENING PORT: " + LISTENING_PORT);

            DESTINATION_IP_ADDRESS = IP_ADDRESS;

            CURRENT_STATUS = Status.CONNECTED;
        }
        catch(IOException exception)
        {
            System.err.println("COULDN'T CONNECT TO: " + IP_ADDRESS);
            System.err.println("VIA LISTENING PORT: " + LISTENING_PORT);
            exception.printStackTrace();
        }
    }

    public static void SendPacket(Packet<?> outgoingPacket, String IP_ADDRESS)
    {
        try(Socket socket = new Socket())
        {
            InetSocketAddress destinationAddress = new InetSocketAddress(IP_ADDRESS, INTERNAL_PORT);

            socket.connect(destinationAddress);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            objectOutputStream.writeObject(outgoingPacket);

            System.out.println("SENT PACKET: " + outgoingPacket);
            System.out.println("TO IP ADDRESS: " + IP_ADDRESS);
        }
        catch(IOException exception)
        {
            System.err.println("COULDN'T SEND PACKET: " + outgoingPacket);
            System.err.println("TO IP ADDRESS: " + IP_ADDRESS);
            exception.printStackTrace();
        }
    }

    public static Packet<?> ReadPacketFromSocket(Socket socket)
    {
        try
        {
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            //WAIT
            Packet<?> packet = (Packet<?>) objectInputStream.readObject();

            System.out.println("READ PACKET: " + packet.toString());
            System.out.println("FROM SOCKET: " + socket);
            return packet;
        }
        catch(IOException | ClassNotFoundException exception)
        {
            System.err.println("COULDN'T READ PACKET FROM SOCKET: " + socket);
            exception.printStackTrace();
        }
        return null;
    }

    public static void ServeNewConnection()
    {
        try
        {
            EXECUTOR.submit(new AcceptIncomingPackets());

            if (!AcceptIncomingPackets.incomingPacketsListenSocket.isClosed())
            {
                System.out.println("CONNECTION SUCCESSFUL");
                System.out.println("READY TO RECEIVE PACKETS");
            }
            else
            {
                System.err.println("CONNECTION UNSUCCESSFUL");
                System.exit(0);
            }
        }
        catch(IOException exception)
        {
            System.err.println("COULDN'T SERVE NEW CONNECTION");
            exception.printStackTrace();
        }
    }

    public static void ProcessIncomingPacket(Packet<?> incomingPacket)
    {
        try
        {
            System.out.println("NOW PROCESSING PACKET...");

            //GET THE DATA'S OBJECT TYPE FROM THE PACKET FIRST, THEN DO THE IF STATEMENTS
            if(incomingPacket.getDATA() instanceof Command)
            {
                Packet<Command> commandPacket = (Packet<Command>) incomingPacket;
                Command command = commandPacket.getDATA();
                switch(command.getCommand())
                {
                    case "LIST":
                        EXECUTOR.submit(new ProcessListCommand(commandPacket));

                    case "DELETE":
                        EXECUTOR.submit(new ProcessDeleteCommand(commandPacket));

                    case "RENAME":
                        EXECUTOR.submit(new ProcessRenameCommand(commandPacket));

                    case "DOWNLOAD":
                        EXECUTOR.submit(new ProcessDownloadCommand(commandPacket));

                    case "UPLOAD":
                        EXECUTOR.submit(new ProcessUploadCommand(commandPacket));

                    default:
                        Packet<Response> responsePacket = new Packet<>(new Response(false, "RECEIVED INVALID COMMAND"));

                        Application.SendPacket(responsePacket, commandPacket.getSourceIP());
                }
            }
            else if(incomingPacket.getDATA() instanceof Response)
            {
                Packet<Response> responsePacket = (Packet<Response>) incomingPacket;
                Response response = responsePacket.getDATA();
                System.out.println(response.toString());
            }
            else
            {
                System.err.println("RECEIVED INVALID PACKET");
            }
        }
        catch(Exception exception)
        {
            System.err.println("COULDN'T PROCESS INCOMING PACKET");
            exception.printStackTrace();
        }
    }

    public static class AcceptIncomingConnections implements Runnable
    {
        private static ServerSocket incomingConnectionsListenSocket;
        public AcceptIncomingConnections() throws IOException
        {
            incomingConnectionsListenSocket = new ServerSocket(LISTENING_PORT);
        }
        public void run()
        {
            try
            {
                while(true)
                {
                    //WAIT
                    Socket incomingConnection = incomingConnectionsListenSocket.accept();

                    String SOURCE_IP = incomingConnection.getInetAddress().getHostName();

                    System.out.println("RECEIVED A NEW CONNECTION REQUEST FROM IP: " + SOURCE_IP);
                    System.out.println("ESTABLISHING A CONNECTION...");

                    Application.ServeNewConnection();
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD ACCEPTING INCOMING CONNECTIONS");
                exception.printStackTrace();
            }
        }
    }

    public static class AcceptIncomingPackets implements Runnable
    {
        private static ServerSocket incomingPacketsListenSocket;
        public AcceptIncomingPackets() throws IOException
        {
            incomingPacketsListenSocket = new ServerSocket(INTERNAL_PORT);
        }
        public void run()
        {
            try
            {
                while(true)
                {
                    //WAIT
                    Socket socket = incomingPacketsListenSocket.accept();

                    Packet<?> receivedPacket = Application.ReadPacketFromSocket(socket);

                    Application.ProcessIncomingPacket(receivedPacket);
                }
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD ACCEPTING INCOMING PACKETS");
                exception.printStackTrace();
            }
        }
    }

    public static class ProcessListCommand implements Runnable
    {
        private final Packet<Command> commandPacket;
        public ProcessListCommand(Packet<Command> commandPacket)
        {
            this.commandPacket = commandPacket;
        }
        public void run()
        {
            try
            {
                //WAIT
                //PROCESS COMMAND HERE OR IN STATIC METHOD AND GENERATE RESPONSE

                Packet<Response> responsePacket = new Packet<>(new Response(true,"RECEIVED LIST COMMAND"));

                Application.SendPacket(responsePacket, commandPacket.getSourceIP());
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
        private final Packet<Command> commandPacket;
        public ProcessDeleteCommand(Packet<Command> commandPacket)
        {
            this.commandPacket = commandPacket;
        }
        public void run()
        {
            try
            {
                //WAIT
                //PROCESS COMMAND HERE OR IN STATIC METHOD AND GENERATE RESPONSE

                Packet<Response> responsePacket = new Packet<>(new Response(true,"RECEIVED DELETE COMMAND"));

                Application.SendPacket(responsePacket, commandPacket.getSourceIP());
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
        private final Packet<Command> commandPacket;
        public ProcessRenameCommand(Packet<Command> commandPacket)
        {
            this.commandPacket = commandPacket;
        }
        public void run()
        {
            try
            {
                //WAIT
                //PROCESS COMMAND HERE OR IN STATIC METHOD AND GENERATE RESPONSE

                Packet<Response> responsePacket = new Packet<>(new Response(true,"RECEIVED RENAME COMMAND"));

                Application.SendPacket(responsePacket, commandPacket.getSourceIP());
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
        private final Packet<Command> commandPacket;
        public ProcessDownloadCommand(Packet<Command> commandPacket)
        {
            this.commandPacket = commandPacket;
        }
        public void run()
        {
            try
            {
                //WAIT
                //PROCESS COMMAND HERE OR IN STATIC METHOD AND GENERATE RESPONSE

                Packet<Response> responsePacket = new Packet<>(new Response(true,"RECEIVED DOWNLOAD COMMAND"));

                Application.SendPacket(responsePacket, commandPacket.getSourceIP());
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
        private final Packet<Command> commandPacket;
        public ProcessUploadCommand(Packet<Command> commandPacket)
        {
            this.commandPacket = commandPacket;
        }
        public void run()
        {
            try
            {
                //WAIT
                //PROCESS COMMAND HERE OR IN STATIC METHOD AND GENERATE RESPONSE

                Packet<Response> responsePacket = new Packet<>(new Response(true,"RECEIVED UPLOAD COMMAND"));

                Application.SendPacket(responsePacket, commandPacket.getSourceIP());
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD PROCESSING UPLOAD COMMAND");
                exception.printStackTrace();
            }
        }
    }

    public static class OpenStatusListener implements Runnable
    {
        public void run()
        {
            try
            {
                while(!(CURRENT_STATUS.equals(Status.SHUTDOWN) || CURRENT_STATUS.equals(Status.CRITICAL_ERROR)))
                {
                    Thread.sleep(1000);
                }

                System.err.println("SHUTTING DOWN...");
                System.err.println("HAVEN'T IMPLEMENTED SAFE SHUTDOWN YET");
                System.err.println("FORCE QUITTING...");
                System.exit(0);
            }
            catch(Exception exception)
            {
                System.err.println("EXCEPTION IN THREAD SERVER STATUS LISTENER");
                exception.printStackTrace();
            }
        }
    }

    public static class ServerInputListener implements Runnable
    {
        private final Scanner scanner = new Scanner(System.in);
        public void run()
        {
            do
            {
                System.out.println("\nENTER A COMMAND: ");
                switch(scanner.nextLine().toUpperCase())
                {
                    case "QUIT":
                        CURRENT_STATUS = Status.SHUTDOWN;
                        break;

                    case "FORCE QUIT":
                        CURRENT_STATUS = Status.CRITICAL_ERROR;
                        break;

                    default:
                        System.out.println("\nVALID COMMANDS: \"QUIT\" OR \"FORCE QUIT\"");
                        break;
                }
            }
            while(CURRENT_STATUS.equals(Status.ONLINE));
        }
    }

    public static class ClientInputListener implements Runnable
    {
        private final Scanner scanner = new Scanner(System.in);
        public void run()
        {
            do
            {
                System.out.println("\nENTER A COMMAND: ");
                switch(scanner.nextLine().toUpperCase())
                {
                    case "CONNECT":
                        System.out.println("TYPE IP ADDRESS TO CONNECT TO: ");
                        String DESTINATION_IP = scanner.nextLine();

                        Application.Connect(DESTINATION_IP); //SETS CURRENT_STATUS TO CONNECTED IF SUCCESSFUL
                        break;

                    case "QUIT":
                        CURRENT_STATUS = Status.SHUTDOWN;
                        break;

                    case "FORCE QUIT":
                        CURRENT_STATUS = Status.CRITICAL_ERROR;
                        break;

                    default:
                        System.out.println("\nVALID COMMANDS: \"CONNECT\" \"QUIT\" OR \"FORCE QUIT\"");
                        break;
                }
            }
            while(!CURRENT_STATUS.equals(Status.CONNECTED));

            do
            {
                System.out.println("\nENTER A COMMAND: ");
                Command command;
                switch(scanner.nextLine().toUpperCase())
                {
                    case "CONNECT":
                        System.out.println("TYPE IP ADDRESS TO CONNECT TO: ");
                        String DESTINATION_IP = scanner.nextLine();

                        Application.Connect(DESTINATION_IP); //SETS CURRENT_STATUS TO CONNECTED IF SUCCESSFUL
                        break;

                    case "QUIT":
                        CURRENT_STATUS = Status.SHUTDOWN;
                        break;

                    case "FORCE QUIT":
                        CURRENT_STATUS = Status.CRITICAL_ERROR;
                        break;

                    case "LIST":
                        command = new Command("LIST");

                        Application.SendPacket(new Packet<>(command), DESTINATION_IP_ADDRESS);

                        break;

                    case "DELETE":
                        System.out.println("FILENAME TO DELETE: ");
                        String filenameToDelete = scanner.nextLine();
                        command = new Command("DELETE", filenameToDelete);

                        Application.SendPacket(new Packet<>(command), DESTINATION_IP_ADDRESS);

                        break;

                    case "RENAME":
                        System.out.println("FILENAME TO RENAME: ");
                        String filenameToRename = scanner.nextLine();
                        System.out.println("NEW FILENAME: ");
                        String newFilename = scanner.nextLine();
                        command = new Command("RENAME", filenameToRename, newFilename);

                        Application.SendPacket(new Packet<>(command), DESTINATION_IP_ADDRESS);

                        break;

                    case "DOWNLOAD":
                        System.out.println("FILENAME TO DOWNLOAD: ");
                        String filenameToDownload = scanner.nextLine();
                        command = new Command("DOWNLOAD",filenameToDownload);

                        Application.SendPacket(new Packet<>(command), DESTINATION_IP_ADDRESS);

                        break;

                    case "UPLOAD":
                        System.out.println("FILENAME TO UPLOAD: ");
                        String filenameToUpload = scanner.nextLine();
                        command = new Command("UPLOAD",filenameToUpload);

                        Application.SendPacket(new Packet<>(command), DESTINATION_IP_ADDRESS);

                        break;

                    default:
                        System.out.println("\nVALID COMMANDS: \"CONNECT\" \"LIST\" \"DELETE\" \"RENAME\" \"DOWNLOAD\" \"UPLOAD\" \"QUIT\" OR \"FORCE QUIT\"");
                        break;
                }
            }
            while(CURRENT_STATUS.equals(Status.CONNECTED));
        }
    }
}