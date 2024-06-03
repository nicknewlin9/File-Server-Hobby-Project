package com.newlin.util.filesystem;

import com.newlin.util.logger.ConsoleColors;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class FileNode implements Serializable
{
    private String fileName;
    private FileNode parent;
    private final List<FileNode> children;

    public FileNode(String fileName)
    {
        this.fileName = fileName;
        this.children = new ArrayList<>();
    }

    protected String getFileName()
    {
        return this.fileName;
    }

    protected void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    protected FileNode getParent()
    {
        return this.parent;
    }

    protected void setParent(FileNode parent)
    {
        this.parent = parent;
    }

    protected List<FileNode> getChildren()
    {
        return this.children;
    }

    protected void addChild(FileNode child)
    {
        child.setParent(this);
        this.children.add(child);
    }

    protected Path getPath()
    {
        List<String> pathElements = new ArrayList<>();
        FileNode current = this;
        while (current != null)
        {
            pathElements.addFirst(current.getFileName());
            current = current.getParent();
        }
        return Paths.get(String.join("/", pathElements));
    }

    public static FileNode loadFileStructure(Path path)
    {
        FileNode node = new FileNode(path.getFileName().toString());

        if (Files.isDirectory(path))
        {
            try (Stream<Path> pathStream = Files.list(path))
            {
                List<Path> sortedPaths = pathStream
                        .filter(p -> !p.getFileName().toString().startsWith("."))
                        .sorted(Comparator.comparing(p -> !Files.isDirectory(p)))
                        .toList();

                for (Path p : sortedPaths)
                {
                    FileNode childNode = loadFileStructure(p);
                    node.addChild(childNode);
                }
            }
            catch (IOException exception)
            {
                System.err.println("Error reading directory: " + path);
            }
        }
        return node;
    }

    public void printChildren()
    {
        for (FileNode child : getChildren())
        {
            if (child.getPath().toFile().isDirectory())
            {
                System.out.println(ConsoleColors.BLUE.getCode() + child.getFileName() + ConsoleColors.RESET.getCode());
            }
            else
            {
                System.out.print("    ");
                System.out.println(child.getFileName());
            }
        }
    }

    public void printNode()
    {
        printNode(this, 0);
    }

    private void printNode(FileNode node, int level)
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
        node.getChildren().forEach(child -> printNode(child, level + 1));
    }
}