package fr.tp.inf112.projects.robotsim.persistance;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class RequestProcessor implements Runnable {
    private Socket socket;

    public RequestProcessor(Socket socket) {
        this.socket = socket;
    }

    public RequestProcessor() {
        this(null);
    }

    @Override
    public void run() {
        try {
            InputStream inpStr = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inpStr);
            Object obj = objectInputStream.readObject();
            if (obj instanceof String string) {
                System.out.println("Reading" + obj);
                Canvas ret = this.read(string);
                System.out.println(ret);
                OutputStream outStr = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
                objectOutputStream.writeObject(ret);
            }
            else if (obj instanceof Factory factory) {
                System.out.println("Saving Factory");
                this.persist(factory);
            }
            else if (obj instanceof Integer i) {
                if (i == 1) {
                    OutputStream outStr = socket.getOutputStream();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
                    objectOutputStream.writeObject(getFilesLocal());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Canvas read(final String canvasId)
            throws IOException {
        try (
                final InputStream fileInputStream = new FileInputStream(canvasId);
                final InputStream bufInputStream = new BufferedInputStream(fileInputStream);
                final ObjectInputStream objectInputStrteam = new ObjectInputStream(bufInputStream)
        ) {
            return (Canvas) objectInputStrteam.readObject();
        }
        catch (ClassNotFoundException | IOException ex) {
            throw new IOException(ex);
        }

    }

    public void persist(Canvas canvasModel)
            throws IOException {
        try (
                final OutputStream fileOutStream = new FileOutputStream(canvasModel.getId());
                final OutputStream bufOutStream = new BufferedOutputStream(fileOutStream);
                final ObjectOutputStream objOutStream = new ObjectOutputStream(bufOutStream)
        ) {
            objOutStream.writeObject(canvasModel);
        }
    }


    private File[] getFilesLocal() {
        //chooser.setFileFilter(fileNameFilter);
        File dir = new File("./");  // current directory
        File[] files = dir.listFiles((d, name) -> name.endsWith(".factory"));
        System.out.println(Arrays.toString(dir.listFiles()));
        return files;
    }

}
