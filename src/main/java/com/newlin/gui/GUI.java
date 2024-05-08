package com.newlin.gui;

import javax.swing.*;
import java.awt.*;

public class GUI extends JFrame
{

    public GUI()
    {
        setTitle("File Server Program");
        setSize(1200, 800);
        setMinimumSize(new Dimension(600, 400));
        setIconImage(new ImageIcon("/Users/nicholasnewlin/Documents/Java Projects/File-Server-Program/src/main/resources/Mac Studio.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            GUI gui = new GUI();

            gui.createSplitPane();

            gui.setVisible(true);
        });
    }

    public void createSplitPane()
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);  // Distribute space evenly when resizing

        // Left panel (Commands)
        JPanel leftPanel = new JPanel(new BorderLayout());

        JTextArea commandOutput = new JTextArea();
        commandOutput.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(commandOutput);

        JTextField commandInput = new JTextField();

        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(commandInput, BorderLayout.SOUTH);

        // Right panel (Control and display)
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton[] buttons = new JButton[4];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton("Button " + (i + 1));
            buttonPanel.add(buttons[i]);
        }

        JTextArea dataDisplay = new JTextArea();

        JScrollPane dataScrollPane = new JScrollPane(dataDisplay);

        rightPanel.add(buttonPanel, BorderLayout.NORTH);
        rightPanel.add(dataScrollPane, BorderLayout.CENTER);

        // Add panels to split pane
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        // Add splitPane to the frame
        getContentPane().add(splitPane);
    }
}