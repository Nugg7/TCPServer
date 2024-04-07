import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {
    static int portNumber = 1234; // port number of the server
    static Socket clientSocket;

    private static JSONManagerServer json = new JSONManagerServer();

    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(portNumber); //creates server socket with the port number specified
            System.out.println("Server started!");
            json.clearProducts();
            while (true) {
                clientSocket = serverSocket.accept(); //accepts client conntection from server socket
                System.out.println("Accepted connection from: " + clientSocket.getInetAddress().getHostAddress()); //prints ip of the client which connected
                ClientHandler clientHandler = new ClientHandler(clientSocket); //creates client handler
                Thread thread = new Thread(clientHandler); //creates thread for the client that connected
                thread.start(); //starts the thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null)
                    serverSocket.close(); //closes the server socket
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}