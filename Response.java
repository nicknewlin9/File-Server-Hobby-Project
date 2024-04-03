import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Response implements Serializable
{
    private final List<Object> contents = new LinkedList<>();
    public Response(Object...contents)
    {
        this.contents.addAll(Arrays.asList(contents));
    }
    public List<Object> getContents()
    {
        return this.contents;
    }

    public boolean isSuccessful()
    {
        return (boolean) this.contents.getFirst();
    }

    public void setSuccess(boolean success)
    {
        this.contents.set(0,success);
    }

    public void setContents(List<?> list)
    {
        //SET CONTENTS AFTER 1ST ELEMENT
    }

    public void displayResponse()
    {
        Response response = new Response(this.contents);
        if(response.isSuccessful())
        {
            System.out.println("Operation successful");
        }
        else
        {
            System.out.println("Operation unsuccessful");
        }
        //DEAL WITH RESPONSE CONTENT
        for(Object object : response.getContents())
        {
            if(object.equals(response.getContents().getFirst()))
            {
                return;
            }
            else
            {
                System.out.println("Server responded: " + object);
            }
        }
    }

    @Override
    public String toString()
    {
        List<Object> responseList = new LinkedList<>();
        for(Object object : contents)
        {
            responseList.add(object.toString());
        }
        return responseList.toString();
    }
}
