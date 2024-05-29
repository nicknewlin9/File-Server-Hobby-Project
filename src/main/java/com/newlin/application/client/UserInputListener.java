package com.newlin.application.client;

import java.util.Scanner;

import static com.newlin.application.client.Client.isOnline;
import static com.newlin.application.client.Client.logger;

public class UserInputListener implements Runnable
{
    public void run()
    {
        try(Scanner scanner = new Scanner(System.in))
        {
            while(isOnline)
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