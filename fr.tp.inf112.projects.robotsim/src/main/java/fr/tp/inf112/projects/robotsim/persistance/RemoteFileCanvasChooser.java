package fr.tp.inf112.projects.robotsim.persistance;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class RemoteFileCanvasChooser extends FileCanvasChooser {

    private final InetAddress netAddress;
    private final int port = 80;
    private final Integer askForFiles = 1;

    public RemoteFileCanvasChooser(String fileExtension, String documentTypeLabel) {
        super(fileExtension, documentTypeLabel);
        try {
            netAddress= InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public RemoteFileCanvasChooser() {
        this(null, null);
    }

    @Override
    protected String browseCanvases(final boolean open)
            throws IOException {

        File[] files = getFiles();
        if (open) {
            if (files == null || files.length == 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "There are no Factory saved yet."
                );
            } else {
                File ret = (File) JOptionPane.showInputDialog(
                        null,
                        "Which file would you like to open?",
                        "Open Factory",
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        files,
                        null
                );
                return ret.getPath();


            }
        } else {
            //Saving the file instead of opening
            return JOptionPane.showInputDialog(null, "What do you want to name your file?", "Saving factory", JOptionPane.INFORMATION_MESSAGE);
        }
        return null;
    }

    private File[] getFiles() throws IOException {
        try (Socket socket = new Socket(netAddress, port)) {
            OutputStream outStr = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
            objectOutputStream.writeObject(askForFiles);

            InputStream inpStr = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inpStr);
            return ((File[]) objectInputStream.readObject());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
