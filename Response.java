import java.io.Serializable;
import java.util.List;

public class Response implements Serializable
{
    private boolean success;
    private List<String> response;
    public Response(boolean success, List<String> response)
    {
        this.success = success;
        this.response = response;
    }
    public Response(boolean success)
    {
        this.success = success;
    }
    public boolean getSuccess()
    {
        return success;
    }
    public List<String> getResponse()
    {
        return response;
    }
    public void setSuccess(boolean success)
    {
        this.success = success;
    }
    public void setResponse(List<String> response)
    {
        this.response = response;
    }
    @Override
    public String toString()
    {
        String responseString = "\n";
        for(int i = 1; i <= response.size(); i++)
        {
            responseString = responseString.concat("RESPONSE" + i + "= ").concat(response.get(i));
        }
        return"SUCCESS: " + success + responseString;
    }
}
