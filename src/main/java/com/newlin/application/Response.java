package com.newlin.application;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Response implements Serializable
{
    private boolean success;
    private Serializable response;

    public Response(boolean success, Serializable response)
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

    public Serializable getResponse()
    {
        return response;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public void setResponse(Serializable response)
    {
        this.response = response;
    }

    public boolean equals(boolean success)
    {
        return success == this.success;
    }
}