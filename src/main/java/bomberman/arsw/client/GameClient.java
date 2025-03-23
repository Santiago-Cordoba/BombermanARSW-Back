package bomberman.arsw.client;

import java.io.*;
import java.net.*;

public class GameClient
{
    public static void main(String[] args) throws IOException
    {
        Socket socket = new Socket("localhost", 5000);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String config = in.readLine();
        System.out.println("Configuración recibida: " + config);

        socket.close();
    }
}
