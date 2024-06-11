package com.newlin.test;

import com.newlin.application.server.CommandProcessor;
import com.newlin.util.command.Action;
import com.newlin.util.command.Command;
import com.newlin.util.filesystem.FileNode;
import com.newlin.util.logger.ConsoleColors;
import com.newlin.util.response.Response;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class FileSystemTest
{
    @Test
    public void createFileStructure()
    {
        Properties properties = getProperties();
        assert properties != null;
        Path rootPath = Paths.get(properties.getProperty("server.filesystem.directory"));
        FileNode rootFileNode;
        rootFileNode = FileNode.loadFileStructure(rootPath);

        CommandProcessor commandProcessor = new CommandProcessor(rootFileNode);
        Response response = commandProcessor.submit(new Command(Action.LIST, ""));

        FileNode node = (FileNode) response.data();

        //System.out.println(node);
        System.out.println(node.getChildrenStringForConsole());
        ConsoleColors.printColors();


        //CommandProcessor commandProcessor1 = new CommandProcessor(rootFileNode);
        //Response response1 = commandProcessor1.submit(new Command(Action.DOWNLOAD, "poopy.txt"));
        //System.out.println(response1.data());
    }

    private static Properties getProperties()
    {
        Properties properties = new Properties();
        try (InputStream input = FileSystemTest.class.getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                return null;
            }
            properties.load(input);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();

        }
        return properties;
    }

    @Test
    public void fileTest() throws IOException {
        Path root = Paths.get("remote");
        FileNode node = FileNode.loadFileStructure(root);

        System.out.println(node);

        try(FileInputStream fileInputStream = new FileInputStream("remote/poopy.txt"))
        {

        }
    }
}
