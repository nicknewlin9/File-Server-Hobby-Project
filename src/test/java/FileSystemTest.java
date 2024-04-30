import com.newlin.filesystem.FileNode;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import com.newlin.filesystem.FileSystem;

public class FileSystemTest
{
    @Test
    public void createFileStructure() throws IOException
    {
        FileSystem fileSystem = new FileSystem("");

        FileNode rootNode = fileSystem.getRootNode();

        rootNode.printAll();

    }
}
