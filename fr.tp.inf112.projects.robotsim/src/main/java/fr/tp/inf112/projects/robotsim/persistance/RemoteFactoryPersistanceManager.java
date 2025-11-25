package fr.tp.inf112.projects.robotsim.persistance;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class RemoteFactoryPersistanceManager extends FactoryPersistenceManager {
    private final InetAddress netAddress;
    private final int port = 8080;
    public RemoteFactoryPersistanceManager(CanvasChooser canvasChooser) {
        super(canvasChooser);
        try {
            netAddress= InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public RemoteFactoryPersistanceManager() {
        this(null);
    }



    @Override
    public Canvas read(final String canvasId) throws IOException {
        try (Socket socket = new Socket(netAddress, port)
        ) {
            OutputStream outStr = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
            objectOutputStream.writeObject(canvasId);

            InputStream inpStr = socket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inpStr);
            return (Canvas) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void persist(Canvas canvasModel) throws IOException {
        try (Socket socket = new Socket(netAddress, port)) {
            OutputStream outStr = socket.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
            objectOutputStream.writeObject(canvasModel);
        }
    }


}
