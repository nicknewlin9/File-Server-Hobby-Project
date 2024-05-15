package com.newlin.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.io.*;

public class GUI extends JFrame
{
    public static void openGUI()
    {
        System.setProperty("apple.awt.application.appearance", "system");
        SwingUtilities.invokeLater(() ->
        {
            GUI gui = new GUI();

            gui.createSplitPane();

            gui.getRootPane().putClientProperty("apple.awt.application.appearance", "system");

            gui.setVisible(true);
        });
    }

    public GUI()
    {
        setTitle("File Server Program");
        setSize(1200, 800);
        setMinimumSize(new Dimension(600, 400));
        setIconImage(new ImageIcon("/Users/nicholasnewlin/Documents/Java Projects/File-Server-Program/src/main/resources/Mac Studio.png").getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void createSplitPane()
    {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);  // Distribute space evenly when resizing

        splitPane.setUI(new BasicSplitPaneUI()
        {
            @Override
            public BasicSplitPaneDivider createDefaultDivider()
            {
                return new BasicSplitPaneDivider(this)
                {
                    @Override
                    public void setBorder(Border b)
                    {

                    }

                    @Override
                    public void paint(Graphics g)
                    {
                        g.setColor(MyColor.GRAY2.color);
                        g.fillRect(0, 0, getSize().width, getSize().height);
                        super.paint(g);
                    }
                };
            }
        });

        // Left panel (Commands)
        JPanel leftPanel = new JPanel(new BorderLayout());

        JTextArea commandOutput = new JTextArea();
        commandOutput.setEditable(false);
        commandOutput.setForeground(Color.WHITE);
        commandOutput.setBackground(MyColor.GRAY2.color);

        PrintStream out = new PrintStream(new OutputStream()
        {
            @Override
            public void write(int b)
            {
                commandOutput.append(""+(char)(b & 0xFF));
            }
        });
        System.setOut(out);

        JScrollPane scrollPane = new JScrollPane(commandOutput);

        JTextField commandInput = new JTextField();
        commandInput.setBackground(MyColor.GRAY2.color);
        commandInput.setForeground(Color.WHITE);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        leftPanel.add(commandInput, BorderLayout.SOUTH);

        PipedInputStream inPipe = new PipedInputStream();
        System.setIn(inPipe);
        try
        {
            PipedOutputStream outPipe = new PipedOutputStream(inPipe);
            PrintStream stream = new PrintStream(outPipe, true);

            commandInput.addActionListener(e -> {
                // Write to outPipe which is connected to inPipe
                stream.println(commandInput.getText());
                commandInput.setText("");
            });
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        //System.setIn(inPipe);

        // Right panel (Control and display)
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setForeground(Color.WHITE);
        buttonPanel.setBackground(MyColor.GRAY2.color);
        JButton[] buttons = new JButton[4];

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JButton("Button " + (i + 1));
            buttonPanel.add(buttons[i]);
        }

        JTextArea dataDisplay = new JTextArea();
        dataDisplay.setForeground(Color.WHITE);
        dataDisplay.setBackground(MyColor.GRAY2.color);

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