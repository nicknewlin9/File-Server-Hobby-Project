package com.newlin.application;

import java.io.Serializable;

public class Response implements Serializable
{
    private boolean success;
    private String[] response;

    public Response(boolean success, String... response)
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

    public String[] getResponse()
    {
        return response;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public void setResponse(String[] response)
    {
        this.response = response;
    }

    public boolean equals(boolean success)
    {
        return success == this.success;
    }

    @Override
    public String toString()
    {
        String responseString = "";
        for(int i = 1; i <= response.length; i++)
        {
            responseString = responseString.concat("RESPONSE" + i + "= ").concat(response[i].concat("\n"));
        }
        return"SUCCESS: " + success + "\n" + responseString;
    }
}