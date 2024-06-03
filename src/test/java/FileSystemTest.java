import com.newlin.util.filesystem.FileNode;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import com.newlin.util.filesystem.FileSystem;

public class FileSystemTest
{
    @Test
    public void createFileStructure()
    {
        FileSystem fileSystem = new FileSystem("/Users/nicholasnewlin/Documents/Minecraft/MINECRAFT GAMES/SLUTCRAFT V5 (1.19.2)/mods");

        FileNode rootNode = fileSystem.getRootNode();

        rootNode.printChildren();

    }
}
