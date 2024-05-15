package com.newlin.application;

import java.io.Serializable;

@SuppressWarnings("unused")
public class Response implements Serializable
{
    private boolean success;
    private Serializable data;

    public Response(boolean success, Serializable data)
    {
        this.success = success;
        this.data = data;
    }

    public Response(boolean success)
    {
        this.success = success;
    }

    public boolean getSuccess()
    {
        return success;
    }

    public Serializable getData()
    {
        return data;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public void setData(Serializable data)
    {
        this.data = data;
    }

    public boolean equals(boolean success)
    {
        return success == this.success;
    }
}