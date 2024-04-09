import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * TODO:
 *     // add ban message type to ban any client
 *     // add search for client method to send that client ban message
 *     // add ban method
 *     // add auction class to make methods for auction operations
 */

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    private Socket clientSocket;
    private UUID clientUuid;
    private String clientUsername;
    protected BufferedReader reader;
    protected BufferedWriter writer;
    private JSONManagerServer json = new JSONManagerServer();
    private JSONObject response = new JSONObject();

    public static String leftMessageConv;

    static double highestBid = 0;
    static String highestBidder = "";
    static UUID highestBidderUUID = null;

    static JSONArray products = new JSONArray();
    static int currentProductIndex = 0;
    static JSONObject currentProduct = new JSONObject();
    static int numberOfProducts = 1;

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
            setLeftMessage(clientUsername);
            JSONObject joinMessage = new JSONObject();
            joinMessage.put("CODE", "MESSAGE");
            joinMessage.put("MESSAGE", "SERVER : " + clientUsername + " JOINED");
            String joinMessageConv = joinMessage.toString();
            System.out.println(joinMessageConv);
            broadcastMessage(joinMessageConv);
            responseClear(joinMessage);
            clients.add(this);
        } catch (IOException e) {
            System.out.println("ERROR: socket is closed");
        } catch (ParseException e){
            System.out.println("error at clientHandler constructor - parser");
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
                if (!(message.get("message").equals("")) && message.get(("message")) != null && !(message.get("message").equals("/START"))){ //checks if it's a first connection
                    String msg = message.get("username") + ": " + message.get("message");
                    System.out.println(message);
                    System.out.println(msg);
                    try {
                        double bid = Double.parseDouble(message.get("message").toString());
                        String JSONMessage = setResponse("BID", msg + "$");
                        if (currentProductIndex < numberOfProducts && numberOfProducts != 1) {
                            setHighestBid(bid);
                            setHighestBidderProfile(UUID.fromString(message.get("UUID").toString()));
                        }
                        debug();
                        broadcastMessage(JSONMessage); //broadcasts the bid of the client to all the other clients
                        System.out.println(JSONMessage); //prints out the bid of the client
                        responseClear(response);
                    } catch (Exception e) {
                        if (message.get("username").equals("PRODUCT")){
                            JSONObject product = new JSONObject();
                            product.put("name", message.get("message"));
                            product.put("HighestBidder", "");
                            product.put("HighestBidderUUID", "");
                            product.put("Bid", 0);
                            json.putProducts(product);
                            numberOfProducts++;
                            responseClear(response);
                        }
                        else if (message.get("username").equals("AUCTION") && message.get("message").equals("/FIRST PRODUCT")) {
                            setProducts();
                            for(Object p : products){ // debug
                                System.out.println(p.toString());
                            }
                            System.out.println(numberOfProducts); // debug
                            currentProduct = (JSONObject) products.get(currentProductIndex);
                            JSONObject firstProduct = new JSONObject();
                            firstProduct.put("CODE", "AUCTION");
                            firstProduct.put("MESSAGE", currentProduct.get("name"));
                            broadcastMessage(firstProduct.toString());
                        }
                        else if (message.get("username").equals("AUCTION") && message.get("message").equals("/NEXT PRODUCT")) {
                            currentProduct.remove("HighestBidder");
                            currentProduct.remove("HighestBidderUUID");
                            currentProduct.remove("Bid");
                            currentProduct.put("HighestBidder", highestBidder);
                            try {
                                currentProduct.put("HighestBidderUUID", highestBidderUUID.toString());
                            }
                            catch (Exception n){
                                currentProduct.put("HighestBidderUUID", "0");
                            }
                            //currentProduct.put("HighestBidderUUID", highestBidderUUID.toString());
                            currentProduct.put("Bid", highestBid);
                            highestBid = 0;
                            highestBidder = "";
                            highestBidderUUID = null;
                            if (currentProductIndex < numberOfProducts) {
                                sendResetBid();
                                for(Object p : products){ // debug
                                    System.out.println(p.toString());
                                }
                                currentProductIndex++;
                                currentProduct = (JSONObject) products.get(currentProductIndex);
                                JSONObject nextProduct = new JSONObject();
                                nextProduct.put("CODE", "AUCTION");
                                nextProduct.put("MESSAGE", currentProduct.get("name"));
                                broadcastMessage(nextProduct.toString());
                            }

                        }
                        else if (message.get("username").equals("AUCTION") && message.get("message").equals("/END")){
                            currentProduct.remove("HighestBidder");
                            currentProduct.remove("HighestBidderUUID");
                            currentProduct.remove("Bid");
                            currentProduct.put("HighestBidder", highestBidder);
                            try {
                                currentProduct.put("HighestBidderUUID", highestBidderUUID.toString());
                            }
                            catch (Exception n){
                                currentProduct.put("HighestBidderUUID", "0");
                            }
                            //currentProduct.put("HighestBidderUUID", highestBidderUUID.toString());
                            currentProduct.put("Bid", highestBid);
                            highestBid = 0;
                            highestBidder = "";
                            highestBidderUUID = null;
                            sendResetBid();
                            JSONObject endingMessage = new JSONObject();
                            endingMessage.put("CODE", "MESSAGE");
                            endingMessage.put("MESSAGE", "Auction Ended");
                            broadcastMessage(endingMessage.toString());
                            String end = "";
                            for(Object p : products){
                                JSONObject prod = (JSONObject) p;
                                end = "Product: " + prod.get("name") + "\nWinner: " + prod.get("HighestBidder") + "\nfor: " + prod.get("Bid");
                                System.out.println(end);
                                endingMessage.put("MESSAGE", end);
                                broadcastMessage(endingMessage.toString());
                                endingMessage.remove("MESSAGE");
                            }

                            JSONObject endAuction = new JSONObject();
                            endAuction.put("CODE", "AUCTION");
                            endAuction.put("MESSAGE", "Ended");
                            broadcastMessage(endAuction.toString());

                            for(Object p : products){
                                JSONObject prod = (JSONObject) p;
                                json.putProducts(prod);
                            }
                        }
                        else{
                            String JSONMessage = setResponse("MESSAGE", msg);
                            broadcastMessage(JSONMessage); //broadcasts the message of the client to all the other clients
                            System.out.println(JSONMessage); //prints out the message of the client
                            responseClear(response);
                        }
                    }
                }
                else if (message.get("username").equals("ADMIN") && message.get("message").equals("/START")) {
                    Server.refuseConnection();
                }
            }
            ClientHandler clientLeft = getUser(this.clientUuid);
            setLeftMessage(clientLeft.getClientUsername());
            System.out.println(leftMessageConv); //prints out when client exits
            try {
                if (clientSocket.isConnected()) {
                    broadcastMessage(leftMessageConv); //broadcasts the message of the client to all the other clients
                }
            } catch (Exception e) {
                System.out.println(leftMessageConv);//prints out when client exits
            }
        } catch (IOException e) {
            try {
                if (clientSocket.isConnected()) {
                    broadcastMessage(leftMessageConv); //broadcasts the message of the client to all the other clients
                }
            } catch (Exception o) {
                System.out.println(leftMessageConv); //prints out when client exits
            }
            System.out.println(leftMessageConv); //prints out when client exits
            //e.printStackTrace();
        }
    }

    public void setLeftMessage(String userName){
        JSONObject leftMessage = new JSONObject();
        leftMessage.put("CODE", "MESSAGE");
        leftMessage.put("MESSAGE", "SERVER : " + userName + " LEFT");
        leftMessageConv = leftMessage.toString();
    }

    public void responseClear(JSONObject response){
        response.remove("CODE");
        response.remove("MESSAGE");
    }

    public String setResponse(String CODE,String MESSAGE){
        response.put("CODE", CODE);
        response.put("MESSAGE", MESSAGE);
        return response.toJSONString();
    }

    public void broadcastMessage(String message) {
        for(ClientHandler client : clients){
            try {
                client.writer.write(message);
                client.writer.newLine();
                client.writer.flush();
            } catch (IOException e) {
            }
        }
    }

    public String getClientUsername(){
        return clientUsername;
    }

    public UUID getClientUuid(){
        return clientUuid;
    }

    public ClientHandler getUser(UUID uuid){
        for(ClientHandler client : clients){
            if (client.clientUuid.equals(uuid)){
                return client;
            }
        }
        return null;
    }

    public synchronized void setHighestBid(double bid){
        if (bid > highestBid){
            highestBid = bid;
            sendhighestBid(highestBid);
        }
    }

    public synchronized void setHighestBidderProfile(UUID uuid){
        ClientHandler client = getUser(uuid);
        highestBidder = client.getClientUsername();
        highestBidderUUID = client.getClientUuid();
    }

    public void debug(){
        System.out.println("highestBid: " + highestBid + " highestBidder: " + highestBidder + " highestBidderUUID: " + highestBidderUUID);
    }

    public void sendResetBid(){
        JSONObject message = new JSONObject();
        message.put("CODE", "RESET");
        message.put("MESSAGE", 0);
        broadcastMessage(message.toString());
    }

    public void setProducts(){
        products = json.getProducts();
    }

    public synchronized void sendhighestBid(double bid){
        JSONObject message = new JSONObject();
        message.put("CODE", "PRODUCT");
        message.put("MESSAGE", bid);
        broadcastMessage(message.toString());
    }

    @Override
    public void run() {
        try { //code to execute when client connects
            readLoop(writer, reader);
        } catch (Exception e) {
            System.out.println("ERROR: Auction started");
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
