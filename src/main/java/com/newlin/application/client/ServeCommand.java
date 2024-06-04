package com.newlin.application.client;

import com.newlin.util.command.Command;
import com.newlin.util.response.Response;

import java.io.IOException;

public class ServeCommand implements Runnable
{
    public Command command;

    public ServeCommand(Command command)
    {
        this.command = command;
    }

    public void run()
    {
        try
        {
            Client.objectOutputStream.writeObject(command);
            Client.objectOutputStream.flush();
            Response receivedResponse = (Response) Client.objectInputStream.readObject();

            Client.logger.info(String.valueOf(receivedResponse.success()));

            ResponseProcessor responseProcessor = new ResponseProcessor();
            responseProcessor.submit(receivedResponse);

            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException exception)
            {
                throw new RuntimeException(exception);
            }
        }
        catch(ClassNotFoundException exception)
        {
            Client.logger.warning("Can't parse response from server");
        }
        catch(IOException exception)
        {
            Client.logger.warning("Exception while communicating with server");
        }
        finally
        {
            Client.commandSlot.release();
        }
    }
}