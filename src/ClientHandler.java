import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private UUID clientUuid;
    private String clientUsername;
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    protected void readLoop(PrintWriter out, BufferedReader in){
        JSONManagerServer json = new JSONManagerServer();
        String s;
        JSONObject message = null;
        try {
            while ((s = in.readLine()) != null) {
                try{
                    message = json.parse(s);
                }catch (ParseException e){
                    System.out.println("error parsing");
                }
                if (message.get("message").equals("")){
                    clientUsername = (String) message.get("username");
                    clientUuid = UUID.fromString((String) message.get("UUID"));
                    System.out.println("connected : " + clientUsername + "\nUUID : " + clientUuid);
                }
                else{
                    System.out.println("Received: " + message);
                }
                if (s.equalsIgnoreCase("exit")) {
                    break;
                }
            }

            System.out.println("Client exited");
        } catch (IOException e) {
            System.out.println("client exited");
            //e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            readLoop(out, in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null)
                    clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}