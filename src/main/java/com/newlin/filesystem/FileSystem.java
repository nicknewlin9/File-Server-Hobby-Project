package com.newlin.filesystem;

import com.newlin.application.ConsoleColors;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSystem implements Serializable
{
    private final FileNode rootNode;

    public FileSystem(String rootNodePathString)
    {
        Path rootPath = Paths.get(rootNodePathString);
        rootNode = buildFileTree(rootPath);
    }

    public FileNode buildFileTree(Path path)
    {
        FileNode node = new FileNode(path);

        if (Files.isDirectory(path))
        {
            try (Stream<Path> pathStream = Files.list(path))
            {
                List<Path> sortedPaths = pathStream
                        .sorted(Comparator.comparing((Path p) -> !Files.isDirectory(p))) // Sort with directories first
                        .collect(Collectors.toList());

                for (Path p : sortedPaths)
                {
                    node.addChild(buildFileTree(p));
                }
            }
            catch (IOException e)
            {
                System.err.println("Error reading directory: " + path);
            }
        }
        return node;
    }

    public FileNode getRootNode()
    {
        return rootNode;
    }

    public static void createDirectory(String pathString) throws IOException
    {
        Path path = Paths.get(pathString);

        if(!Files.exists(path))
        {
            Files.createDirectory(path);
        }
    }

    public static void createFile(String pathString) throws IOException
    {
        Path path = Paths.get(pathString);

        if(!Files.exists(path))
        {
            Files.createFile(path);
        }
    }

    public void printAll()
    {
        printAll(rootNode,0);
    }

    private void printAll(FileNode node, int level)
    {
        for(int i = 0; i < level; i++)
        {
            System.out.print("    ");
        }
        if(node.getPath().toFile().isDirectory())
        {
            System.out.println(ConsoleColors.BLUE.getCode() + node.getPath().getFileName() + ConsoleColors.RESET.getCode());
        }
        else
        {
            System.out.println(node.getPath().getFileName());
        }
        node.getChildren().forEach(child -> printAll(child, level + 1));
    }

    public void printChildren()
    {
        for (FileNode child : rootNode.getChildren())
        {
            if(child.getPath().toFile().isDirectory())
            {
                System.out.println(ConsoleColors.BLUE.getCode() + child.getPath().getFileName() + ConsoleColors.RESET.getCode());
            }
            else
            {
                System.out.print("    ");
                System.out.println(child.getPath().getFileName());
            }
        }
    }
}
