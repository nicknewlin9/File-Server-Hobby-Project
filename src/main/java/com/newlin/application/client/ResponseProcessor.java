package com.newlin.application.client;

import com.newlin.util.filesystem.FileNode;
import com.newlin.util.response.Response;

import static com.newlin.application.client.Client.logger;

public class ResponseProcessor
{
    public void submit(Response response)
    {
        if(response.success())
        {
            switch(response.data())
            {
                case FileNode node -> System.out.println(node.getChildrenString());
                case String message -> logger.info("Server returned: " + message);
                default -> logger.severe("Received invalid response from server");
            }
        }
        else
        {
            logger.severe("Received a false response from server");
        }
    }
}
