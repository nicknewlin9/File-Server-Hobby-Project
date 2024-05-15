import com.newlin.application.client.Client;
import com.newlin.application.server.Server;
import com.newlin.gui.GUI;
import org.junit.jupiter.api.Test;

public class GUITest
{
    @Test
    public void testClientWithGUI()
    {
        GUI.openGUI();
        Client.main(null);
    }

    @Test
    public void testServerWithGUI()
    {
        GUI.openGUI();
        Server.main(null);
    }
}
