package com.newlin.application;

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
    public ObjectInputStream objectInputStream;
    public ObjectOutputStream objectOutputStream;
    public boolean isConnected = false;
    public ReentrantLock isConnectedLock = new ReentrantLock();

    public ServeIncomingRequest(Socket socket)
    {
        this.socket = socket;
        this.clientName = socket.getInetAddress().getHostName();
    }

    public void run()
    {
        try
        {
            if(socket.isConnected())
            {
                try
                {
                    isConnectedLock.lock();
                    isConnected = true;
                }
                finally
                {
                    isConnectedLock.unlock();
                }

                objectInputStream = new ObjectInputStream(socket.getInputStream());
                Command receivedCommand = (Command) objectInputStream.readObject();

                Server.commandSlot.acquire();

                if(receivedCommand.getAction() == Command.Actions.CONNECT)
                {
                    Response response = new Response(true, Server.log("CONNECTED SUCCESSFULLY"));
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                else if(receivedCommand.getAction() != Command.Actions.CONNECT)
                {
                    Response response = new Response(true, Server.log("RECEIVED INVALID COMMAND"));
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(response);
                    objectOutputStream.flush();
                }
                Server.commandSlot.release();
            }
            while(isConnected)
            {
                if(!socket.isConnected())
                {
                    try
                    {
                        isConnectedLock.lock();
                        isConnected = false;
                    }
                    finally
                    {
                        isConnectedLock.unlock();
                    }
                    return; //SKIPS TO FINALLY BLOCK
                }
                Command receivedCommand = (Command) objectInputStream.readObject();
                Server.commandSlot.acquire();
                System.out.println(Server.log("RECEIVED COMMAND: " + receivedCommand.getAction() + " FROM: " + clientName));
                Server.EXECUTOR.submit(new Processor(receivedCommand, objectOutputStream));
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
}