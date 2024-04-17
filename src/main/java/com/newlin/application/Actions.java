package com.newlin.application;

public enum Actions
{
    STARTUP(0),
    CONNECT(1),
    QUIT(0),
    FORCE_QUIT(0),
    LIST(0),
    DELETE(1),
    RENAME(2),
    DOWNLOAD(1),
    UPLOAD(1);

    final int NUM_ARGS;

    Actions(int NUM_ARGS)
    {
        this.NUM_ARGS = NUM_ARGS;
    }
}