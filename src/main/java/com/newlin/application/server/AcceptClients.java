package com.newlin.application.server;

import java.io.IOException;
import java.net.Socket;

public class AcceptClients implements Runnable
{
    public void run()
    {
        try
        {
            while(Server.isOnline)
            {
                if(Server.listenSocket.isClosed())
                {
                    Server.setOnlineStatus(false);
                    return; //SKIPS TO FINALLY BLOCK
                }
                Server.queueSlot.acquire();
                Socket socket = Server.listenSocket.accept();
                Server.executorService.submit(new ServeClient(socket));
            }
        }
        catch(InterruptedException exception)
        {
            Server.logger.severe("Exception while waiting for available queue slot");
        }
        catch (IOException exception)
        {
            Server.logger.warning("Exception while waiting for incoming connections");
        }
        finally
        {
            try
            {
                Server.isOnlineLock.lock();
                Server.offline.signal();
            }
            finally
            {
                Server.isOnlineLock.unlock();
            }
        }
    }
}
