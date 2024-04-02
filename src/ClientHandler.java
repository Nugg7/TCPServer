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
        //function to read constantly the responses from client
        JSONManagerServer json = new JSONManagerServer();
        String s;
        JSONObject message = null;
        try {
            while ((s = in.readLine()) != null) {
                try{
                    message = json.parse(s); //parses the json message of the client from String to JSONObject
                }catch (ParseException e){
                    System.out.println("error parsing");
                }
                if (message.get("message").equals("")){ //if it's the first connection of the client the message will be empty and the server will save the client in the JSON file
                    clientUsername = (String) message.get("username");
                    clientUuid = UUID.fromString((String) message.get("UUID"));
                    System.out.println("connected : " + clientUsername + "\nUUID : " + clientUuid); //should be the code to save the client in the JSON file
                }
                else{
                    System.out.println("Received: " + message); //prints out the message of the client
                }
            }
            System.out.println("Client exited"); //prints out when client exits
        } catch (IOException e) {
            System.out.println("client exited"); //prints out when client exits
            //e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) { //code to execute when client connects
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