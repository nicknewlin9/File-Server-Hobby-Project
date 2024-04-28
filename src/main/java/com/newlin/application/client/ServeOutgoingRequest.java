package com.newlin.application.client;

import com.newlin.application.Command;
import com.newlin.application.Response;

import java.io.IOException;

public class ServeOutgoingRequest implements Runnable
{
    public Command command;

    public ServeOutgoingRequest(Command command)
    {
        this.command = command;
    }
    public void run()
    {
        try
        {
            Client.objectOutputStream.writeObject(command);
            Response receivedResponse = (Response) Client.objectInputStream.readObject();

            //PROCESS RESPONSE
            System.out.println(receivedResponse.getResponse()[0]);
        }
        catch(IOException | ClassNotFoundException exception)
        {
            System.err.println(Client.log("EXCEPTION IN SERVE OUTGOING REQUEST THREAD"));
            exception.printStackTrace();
        }
        finally
        {
            Client.queueSlot.release();
        }
    }
}