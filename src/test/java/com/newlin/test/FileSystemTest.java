package com.newlin.test;

import com.newlin.application.client.ResponseProcessor;
import com.newlin.application.server.CommandProcessor;
import com.newlin.application.server.Server;
import com.newlin.util.command.Action;
import com.newlin.util.command.Command;
import com.newlin.util.filesystem.FileNode;
import com.newlin.util.response.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class FileSystemTest
{
    @Test
    public void createFileStructure()
    {
        Properties properties = getProperties();

        Path rootPath = Paths.get(properties.getProperty("server.filesystem.directory"));
        FileNode rootFileNode;
        rootFileNode = FileNode.loadFileStructure(rootPath);

        //rootFileNode.printChildren();
        //System.out.println("\n\n\n\n\n");
        //rootFileNode.printNode();
        //System.out.println("\n\n\n\n\n");

        CommandProcessor commandProcessor = new CommandProcessor(rootFileNode);
        Response response = commandProcessor.submit(new Command(Action.LIST, ""));

        FileNode node = (FileNode) response.data();

        System.out.println(node.getNodeString());

    }



    private static Properties getProperties()
    {
        Properties properties = new Properties();
        try (InputStream input = FileSystemTest.class.getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                return null;
                //logger.severe("Can't find config.properties");
            }
            properties.load(input);
        }
        catch (IOException exception)
        {
            //logger.severe("Can't read config.properties");
        }
        return properties;
    }
}
