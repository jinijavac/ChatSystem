package ChatServer;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChatServer {
    private static Map<String, PrintWriter> chatClients = new HashMap<>(); //<클라이언트 아이디, 메세지 출력>
    private static Map<Integer, Set<String>> chatRoom = new HashMap<>(); //<방 번호, 방에 입장한 클라이언트 ID> --> 방 만들기!
    private static Map<Integer, String> chatTitles = new HashMap<>(); //<방 번호, 방 제목> --> 방 제목 저장
    private static Map<Integer, String> chatPasswords = new HashMap<>();
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(9999)){
            System.out.println("서버 준비 완료!");
            while (true){
                //연결 끊기지 않게 반복
                Socket socket = serverSocket.accept(); //클라이언트 쪽에서 accept 해주면 시작
                new ChatThread(socket, chatClients, chatRoom, chatTitles, chatPasswords ).start();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
