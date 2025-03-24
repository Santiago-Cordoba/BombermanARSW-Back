package bomberman.arsw.client;

import java.io.*;
import java.net.*;

public class GameClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Leer configuración del servidor
            String config = in.readLine();
            System.out.println("Configuración recibida: " + config);

            // Confirmar recepción al servidor
            out.println("Jugador listo");

            // Esperar confirmación del servidor
            String response = in.readLine();
            if ("OK".equals(response)) {
                System.out.println("Servidor confirmó inicio de partida.");
            } else {
                System.out.println("Error: El servidor no confirmó la partida.");
            }

        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
        }
    }
}
