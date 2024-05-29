package com.newlin.application.server;

import java.io.IOException;
import java.net.Socket;

public class AcceptIncomingRequests implements Runnable
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

                Socket socket = Server.listenSocket.accept();
                Server.executorService.submit(new ServeIncomingRequest(socket));
            }
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
