package com.newlin.util.response;

import java.io.Serializable;

public record Response(boolean success, Serializable data) implements Serializable {}