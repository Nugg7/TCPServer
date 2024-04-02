import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    private Socket clientSocket;
    private UUID clientUuid;
    private String clientUsername;
    protected BufferedReader reader;
    protected BufferedWriter writer;
    private JSONManagerServer json = new JSONManagerServer();

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        String profileToParse;
        JSONObject profile;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            profileToParse = reader.readLine();
            profile = json.parse(profileToParse);
            clientUsername = (String)profile.get("username");
            clientUuid = UUID.fromString((String)profile.get("UUID"));
            System.out.println("SERVER : " + clientUsername + " JOINED");
            broadcastMessage("SERVER : " + clientUsername + " JOINED");
            clients.add(this);
        } catch (IOException e) {
            System.out.println("error at clientHandler constructor");
            throw new RuntimeException(e);
        } catch (ParseException e){
            System.out.println("error at clientHandler constructor - parser");
            throw new RuntimeException(e);
        }
    }

    protected void readLoop(BufferedWriter out, BufferedReader reader){
        //function to read constantly the responses from client
        String s;
        JSONObject message = null;
        try {
            while ((s = reader.readLine()) != null) {
                try{
                    message = json.parse(s); //parses the json message of the client from String to JSONObject
                }catch (ParseException e){
                    System.out.println("error parsing");
                }
                if (!(message.get("message").equals(""))){ //checks if its a first connection
                    String msg = message.get("username") + ": " + message.get("message");
                    broadcastMessage(msg); //broadcasts the message of the client to all the other clients
                    System.out.println(msg); //prints out the message of the client
                }
            }
            System.out.println("SERVER : " + clientUsername + " LEFT"); //prints out when client exits
            try {
                if (clientSocket.isConnected()) {
                    broadcastMessage("SERVER : " + clientUsername + " LEFT"); //broadcasts the message of the client to all the other clients
                }
            } catch (Exception e) {
                System.out.println("SERVER : " + clientUsername + " LEFT"); //prints out when client exits
            }
        } catch (IOException e) {
            try {
                if (clientSocket.isConnected()) {
                    broadcastMessage("SERVER : " + clientUsername + " LEFT"); //broadcasts the message of the client to all the other clients
                }
            } catch (Exception o) {
                System.out.println("SERVER : " + clientUsername + " LEFT"); //prints out when client exits
            }
            System.out.println("SERVER : " + clientUsername + " LEFT"); //prints out when client exits
            //e.printStackTrace();
        }
    }

    public void broadcastMessage(String message) {
        for(ClientHandler client : clients){
                try {
                    client.writer.write(message);
                    client.writer.newLine();
                    client.writer.flush();
                } catch (IOException e) {
                    System.out.println("socket is closed");
                }
        }
    }


    @Override
    public void run() {
        try { //code to execute when client connects
            readLoop(writer, reader);
        } catch (Exception e) {
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