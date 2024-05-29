package com.newlin.application.server;

import com.newlin.util.command.Action;
import com.newlin.util.command.Command;
import com.newlin.util.filesystem.CommandProcessor;
import com.newlin.util.response.Response;
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

                OldServer.commandSlot.acquire();

                if(receivedCommand.action() == Action.CONNECT)
                {
                    Response response = new Response(true, OldServer.log("CONNECTED SUCCESSFULLY"));
                    //objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                else if(receivedCommand.action() != Action.CONNECT)
                {
                    Response response = new Response(true, OldServer.log("RECEIVED INVALID COMMAND"));
                    //objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                OldServer.commandSlot.release();
            }
            while(isConnected)
            {
                if(!socket.isConnected())
                {
                    setConnectedStatus(false);
                    return; //SKIPS TO FINALLY BLOCK
                }
                Command receivedCommand = (Command) objectInputStream.readObject();
                OldServer.commandSlot.acquire();

                System.out.println(OldServer.log("RECEIVED COMMAND: " + receivedCommand.action() + " FROM: " + clientName));

                try
                {
                    System.out.println(OldServer.log("NOW PROCESSING " + receivedCommand.action() + " COMMAND"));
                    Response response = switch (receivedCommand.action())
                    {
                        case Action.CONNECT -> new Response(true, OldServer.log("CONNECTED SUCCESSFULLY"));

                        case Action.LIST -> CommandProcessor.ProcessListCommand(receivedCommand);

                        case Action.DELETE -> CommandProcessor.ProcessDeleteCommand(receivedCommand);

                        case Action.RENAME -> CommandProcessor.ProcessRenameCommand(receivedCommand);

                        case Action.DOWNLOAD -> CommandProcessor.ProcessDownloadCommand(receivedCommand);

                        case Action.UPLOAD -> CommandProcessor.ProcessUploadCommand(receivedCommand);

                        default -> new Response(false, OldServer.log("RECEIVED INVALID COMMAND"));
                    };
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                catch(IOException exception)
                {
                    System.err.println(OldServer.log("EXCEPTION IN THREAD PROCESSING COMMAND"));
                    exception.printStackTrace();
                }
                finally
                {
                    OldServer.commandSlot.release();
                }
            }
        }
        catch(EOFException exception)
        {
            System.out.println(OldServer.log("CLIENT: " + clientName + " DISCONNECTED"));
        }
        catch(IOException | ClassNotFoundException | InterruptedException exception)
        {
            System.err.println(OldServer.log("EXCEPTION IN SERVE INCOMING REQUEST THREAD"));
            exception.printStackTrace();
        }
        finally
        {
            OldServer.queueSlot.release();
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