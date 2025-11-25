package fr.tp.inf112.projects.robotsim.persistance;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class WebServer {
    public static void main(String[] args) {
        try (
                ServerSocket serverSocket = new ServerSocket(8080)
        ) {
            do {
                try {
                    Socket socket = serverSocket.accept();
                    Runnable reqProcessor = new RequestProcessor(socket);
                    new Thread(reqProcessor).start();
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while (true);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
