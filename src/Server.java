import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private final ArrayList <ConnectionHandler> connectionHandlers = new ArrayList<>();
    private ServerSocket serverSocket;
    private ExecutorService pool;

    @Override
    public void run(){
        try {
            serverSocket= new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
            Socket client = serverSocket.accept();
            ConnectionHandler connectionHandler = new ConnectionHandler(client);
            connectionHandlers.add(connectionHandler);
            pool.execute(connectionHandler);
        } catch (IOException e) {
            shutdown();
        }
    }

    public void broadcastMessage(String message){
        for(ConnectionHandler ch : connectionHandlers){
            if (ch != null){
                ch.sendMessage(message);
            }
        }
    }

    public void shutdown(){
        try {
            for(ConnectionHandler ch : connectionHandlers){
                ch.shutdown();
            }
            if(serverSocket != null && !serverSocket.isClosed()){
                serverSocket.close();
            }
        } catch (IOException e) {
            //ignore
        }
    }
    class ConnectionHandler implements Runnable{
        Socket client;
        PrintWriter out;
        BufferedReader in;

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a username: ");
                String username = in.readLine();
                System.out.println("welcome "+ username);
                broadcastMessage(username+" joined the chat");

                out.println("Please enter a message: ");
                String message = in.readLine();
                while (message != null){
                    if(message.equalsIgnoreCase("exit")){
                        out.println(username+" has left the chat");
                        broadcastMessage(message);
                        shutdown();
                    } else {
                        System.out.println(username + ": " + message);
                        broadcastMessage(username + ": " + message);
                    }
                    message = in.readLine();
                }

            } catch (IOException e) {
                shutdown();
            }

        }

        public void sendMessage(String message){
            if(out !=null){
                out.println(message);
            }
        }

        public void shutdown(){
            try {
                if(in != null) in.close();
                if (out != null) out.close();
                if(client!=null && !client.isClosed()) client.close();
            } catch (IOException e) {
                //ignore
            }
        }

    }


}
