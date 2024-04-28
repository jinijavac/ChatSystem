package ChatServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

public class ChatThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String id;
    private Map<String, PrintWriter> chatClients;
    private Map<String, Integer> chatRoom;
    private int currentRoom;

    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients, Map<String, Integer> chatRoom) {
        this.socket = socket;
        this.chatClients = chatClients;
        this.chatRoom = chatRoom;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                id = in.readLine();
                if (chatClients.containsKey(id)) {
                    out.println("이미 존재하는 이름! 다시 입력해주세요");
                } else {
                    out.println(socket.getInetAddress() + ":" + id);
                    break;
                }
            }
            synchronized (chatClients) {
                chatClients.put(this.id, out);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        out.println("방 목록 보기 : /list\n방 생성 : /create\n방 입장 : /join [방번호]\n방 나가기 : /exit\n접속종료 : /bye");

        String msg = null;
        try {
            while ((msg = in.readLine()) != null) {
                if ("/list".equalsIgnoreCase(msg)) {
                    list();
                } else if (msg.startsWith("/create")) {
                    create(msg);
                } else if (msg.startsWith("/join")) {
                    joinRoom(msg);
                } else if ("/exit".equalsIgnoreCase(msg)) {
                    exitRoom();
                } else if ("/users".equalsIgnoreCase(msg)) {
                    listUsers();
                } else if ("/roomusers".equalsIgnoreCase(msg)) {
                    listRoomUsers();
                } else if (msg.startsWith("/whisper")) {
                    whisper(msg);
                } else {
                    broadcast(id + ": " + msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void list() {
        out.println("=== 채팅방 목록 ===");
        for (Map.Entry<String, Integer> entry : chatRoom.entrySet()) {
            out.println("방 번호: " + entry.getValue() + ", 제목: " + entry.getKey());
        }
        out.println("===================");
    }

    public void create(String msg) {
        String[] parts = msg.split(" ", 2);
        if (parts.length < 2) {
            out.println("방 제목을 입력해주세요.");
            return;
        }

        String roomTitle = parts[1].trim();
        if (roomTitle.isEmpty()) {
            out.println("방 제목을 입력해주세요.");
            return;
        }

        int roomId = chatRoom.size() + 1;
        chatRoom.put(roomTitle, roomId);
        out.println("방 " + roomTitle + "가 생성되었습니다.");
        currentRoom = roomId;
    }

    public void joinRoom(String msg) {
        String[] parts = msg.split(" ");
        if (parts.length < 2) {
            out.println("방 번호를 입력해주세요.");
            return;
        }

        int roomId;
        try {
            roomId = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            out.println("방 번호는 숫자로 입력해주세요.");
            return;
        }

        String roomTitle = null;
        for (Map.Entry<String, Integer> entry : chatRoom.entrySet()) {
            if (entry.getValue() == roomId) {
                roomTitle = entry.getKey();
                break;
            }
        }

        if (roomTitle == null) {
            out.println("존재하지 않는 방 번호입니다.");
            return;
        }

        currentRoom = roomId;
        out.println("방 " + roomId + "에 입장하셨습니다.");
        broadcast(id + "님이 방에 입장했습니다.");
    }

    public void exitRoom() {
        if (currentRoom == 0) {
            out.println("이동할 방이 없습니다.");
            return;
        }

        broadcast(id + "님이 방을 나갔습니다.");
        currentRoom = 0;
    }

    public void listUsers() {
        out.println("=== 현재 접속 중인 사용자 목록 ===");
        for (String userName : chatClients.keySet()) {
            out.println(userName);
        }
        out.println("=============================");
    }


    //방 클라이언트 목록 보기 수정해야함 방 번호가 나옴;
    public void listRoomUsers() {
        if (currentRoom == 0) {
            out.println("방에 입장하지 않았습니다.");
            return;
        }
        out.println("=== 현재 방에 있는 사용자 목록 ===");
        for (Map.Entry<String, Integer> entry : chatRoom.entrySet()) {
            String userId = entry.getKey();
            int roomId = entry.getValue();
            if (roomId == currentRoom) {
                out.println(userId);
            }
        }
        out.println("===============================");
    }

    public void whisper(String msg) {
        String[] parts = msg.split(" ");
        if (parts.length < 3) {
            out.println("사용 방법: /whisper [수신자] [메시지]");
            return;
        }

        String to = parts[1];
        String message = msg.substring(msg.indexOf(parts[2]));
        PrintWriter pw = chatClients.get(to);
        if (pw != null) {
            pw.println(id + "님으로부터 온 귓속말 : " + message);
        } else {
            out.println("수신자를 찾을 수 없습니다.");
        }
    }

    private void broadcast(String message) {
        if (currentRoom == 0) {
            out.println("방에 입장해주세요.");
            return;
        }

        for (Map.Entry<String, PrintWriter> entry : chatClients.entrySet()) {
            PrintWriter writer = entry.getValue();
            if (writer != out) {
                writer.println(message);
            }
        }
    }
}

//채팅 내역 저장 추가하기!

