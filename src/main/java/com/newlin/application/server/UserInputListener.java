package com.newlin.application.server;

import java.util.Scanner;

import static com.newlin.application.server.Server.logger;

public class UserInputListener implements Runnable
{
    public void run()
    {
        try(Scanner scanner = new Scanner(System.in))
        {
            while(Server.isOnline)
            {
                logger.info("Enter a command: ");
                String input = scanner.nextLine();
                handleInput(input);
            }
        }
    }

    private static void handleInput(String input)
    {
        logger.fine("User typed: \"" + input + "\"");
    }
}
