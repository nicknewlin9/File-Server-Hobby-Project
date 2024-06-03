package com.newlin.test;

import com.newlin.util.filesystem.FileNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemTest
{
    @Test
    public void createFileStructure()
    {
        Path rootPath = Paths.get("src");

        FileNode rootFileNode;

        rootFileNode = FileNode.loadFileStructure(rootPath);

        //rootFileNode.printChildren();
        rootFileNode.printNode();
    }
}
