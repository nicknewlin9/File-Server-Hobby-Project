package com.newlin.filesystem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Node
{
    private final Path path;
    private final List<Node> children;

    public Node(Path path)
    {
        this.path = path;
        this.children = new ArrayList<>();
    }

    public void addChild(Node child)
    {
        this.children.add(child);
    }

    public Path getPath()
    {
        return path;
    }

    public List<Node> getChildren()
    {
        return children;
    }
}