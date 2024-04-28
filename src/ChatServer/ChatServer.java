package ChatServer;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
    private static Map<String, PrintWriter> chatClients = new HashMap<>();
    private static Map<String, Integer> chatRoom = new HashMap<>();
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)){
            System.out.println("서버 준비 완료!");
            while (true){
                Socket socket = serverSocket.accept();
                new ChatThread(socket, chatClients, chatRoom).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
