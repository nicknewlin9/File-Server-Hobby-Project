package com.newlin.application.server;

import com.newlin.application.Command;
import com.newlin.filesystem.CommandProcessor;
import com.newlin.application.Response;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class ServeIncomingRequest implements Runnable
{
    public Socket socket;
    public String clientName;
    //public ObjectInputStream objectInputStream;
    //public ObjectOutputStream objectOutputStream;
    public boolean isConnected = false;
    public ReentrantLock isConnectedLock = new ReentrantLock();

    public ServeIncomingRequest(Socket socket)
    {
        this.socket = socket;
        this.clientName = socket.getInetAddress().getHostName();
    }

    public void run()
    {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream()))
        {
            if(socket.isConnected())
            {
                setConnectedStatus(true);

                //objectInputStream = new ObjectInputStream(socket.getInputStream());
                Command receivedCommand = (Command) objectInputStream.readObject();

                Server.commandSlot.acquire();

                if(receivedCommand.getAction() == Command.Actions.CONNECT)
                {
                    Response response = new Response(true, Server.log("CONNECTED SUCCESSFULLY"));
                    //objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                else if(receivedCommand.getAction() != Command.Actions.CONNECT)
                {
                    Response response = new Response(true, Server.log("RECEIVED INVALID COMMAND"));
                    //objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                Server.commandSlot.release();
            }
            while(isConnected)
            {
                if(!socket.isConnected())
                {
                    setConnectedStatus(false);
                    return; //SKIPS TO FINALLY BLOCK
                }
                Command receivedCommand = (Command) objectInputStream.readObject();
                Server.commandSlot.acquire();

                System.out.println(Server.log("RECEIVED COMMAND: " + receivedCommand.getAction() + " FROM: " + clientName));

                try
                {
                    System.out.println(Server.log("NOW PROCESSING " + receivedCommand.getAction() + " COMMAND"));
                    Response response = switch (receivedCommand.getAction())
                    {
                        case Command.Actions.CONNECT -> new Response(true, Server.log("CONNECTED SUCCESSFULLY"));

                        case Command.Actions.LIST -> CommandProcessor.ProcessListCommand(receivedCommand);

                        case Command.Actions.DELETE -> CommandProcessor.ProcessDeleteCommand(receivedCommand);

                        case Command.Actions.RENAME -> CommandProcessor.ProcessRenameCommand(receivedCommand);

                        case Command.Actions.DOWNLOAD -> CommandProcessor.ProcessDownloadCommand(receivedCommand);

                        case Command.Actions.UPLOAD -> CommandProcessor.ProcessUploadCommand(receivedCommand);

                        default -> new Response(false, Server.log("RECEIVED INVALID COMMAND"));
                    };
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                catch(IOException exception)
                {
                    System.err.println(Server.log("EXCEPTION IN THREAD PROCESSING COMMAND"));
                    exception.printStackTrace();
                }
                finally
                {
                    Server.commandSlot.release();
                }
            }
        }
        catch(EOFException exception)
        {
            System.out.println(Server.log("CLIENT: " + clientName + " DISCONNECTED"));
        }
        catch(IOException | ClassNotFoundException | InterruptedException exception)
        {
            System.err.println(Server.log("EXCEPTION IN SERVE INCOMING REQUEST THREAD"));
            exception.printStackTrace();
        }
        finally
        {
            Server.queueSlot.release();
        }
    }

    public void setConnectedStatus(boolean status)
    {
        isConnectedLock.lock();
        try
        {
            isConnected = status;
        }
        finally
        {
            isConnectedLock.unlock();
        }
    }
}