package bomberman.arsw.server;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer
{
    private static List<Socket> clients = new ArrayList<>();
    private static String gameConfig = "Duración: 5 min, Jugadores: 4, Bloques: 20, Vidas: 3";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5000);
        System.out.println("Servidor esperando jugadores...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            clients.add(clientSocket);
            new ClientHandler(clientSocket).start();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println("Configuración del juego: " + gameConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
