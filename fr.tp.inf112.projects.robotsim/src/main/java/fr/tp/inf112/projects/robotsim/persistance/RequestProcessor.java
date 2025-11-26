package fr.tp.inf112.projects.robotsim.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.app.SimulatorApplication;
import fr.tp.inf112.projects.robotsim.model.Component;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicVertexMixin;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

public class RequestProcessor implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(RequestProcessor.class.getName());

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
                Canvas ret = this.read(string);
                //System.out.println(ret);
                OutputStream outStr = socket.getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outStr);
                objectOutputStream.writeObject(ret);
            }
            else if (obj instanceof Factory factory) {
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
            Canvas canvasModel = (Canvas) objectInputStrteam.readObject();
            LOGGER.info("Reading factory");
            return canvasModel;
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
            final PolymorphicTypeValidator typeValidator =
                    BasicPolymorphicTypeValidator.builder()
                            .allowIfSubType(PositionedShape.class.getPackageName())
                            .allowIfSubType(Component.class.getPackageName())
                            .allowIfSubType(BasicVertex.class.getPackageName())
                            .allowIfSubType(ArrayList.class.getName())
                            .allowIfSubType(LinkedHashSet.class.getName())
                            .build();
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.activateDefaultTyping(typeValidator,
                            ObjectMapper.DefaultTyping.NON_FINAL)
                    .addMixIn(BasicVertex.class, BasicVertexMixin.class);
            LOGGER.info("Saving factory");
            objOutStream.writeObject(canvasModel);
        }
    }


    private File[] getFilesLocal() {
        //chooser.setFileFilter(fileNameFilter);
        File dir = new File("./");  // current directory
        File[] files = dir.listFiles((d, name) -> name.endsWith(".factory"));
        return files;
    }

}
