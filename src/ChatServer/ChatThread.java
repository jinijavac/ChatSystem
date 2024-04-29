package ChatServer;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ChatThread extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String id; //사용자 아이디
    private Map<String, PrintWriter> chatClients;
    private Map<Integer, Set<String>> chatRoom;
    private Map<Integer, String> chatTitles;
    private Map<Integer, String> chatPasswords; //채팅방 비밀번호 설정
    int currentRoom;
    private File chatHistoryFile;
    private PrintWriter historyWriter;


    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients, Map<Integer, Set<String>> chatRoom, Map<Integer, String> chatTitles) {
        this.socket = socket;
        this.chatClients = chatClients;
        this.chatRoom = chatRoom;
        this.chatTitles = chatTitles;
        this.chatPasswords = new HashMap<>(); // 비밀번호 맵 초기화
        chatHistoryFile = new File("chat_history.txt");

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            historyWriter = new PrintWriter(new FileWriter(chatHistoryFile, true));

            while (true) {
                id = in.readLine();
                if (chatClients.containsKey(id)) {
                    out.println("이미 존재하는 이름! 다시 입력해주세요");
                } else {
                    out.println(socket.getInetAddress() + " / " + id);
                    break;
                }
            }
            synchronized (chatClients) { // 동기화
                chatClients.put(this.id, out);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void run() {
        out.println("=== 채팅 명령어 목록 ===\n" +
                "/list : 방 목록 보기\n" +
                "/create [방제목] : 새로운 방 생성\n" +
                "/join [방번호] : 방 입장\n" +
                "/exit : 현재 방에서 나가기\n" +
                "/users : 현재 접속 중인 사용자 목록\n" +
                "/roomusers : 현재 방에 있는 사용자 목록\n" +
                "/whisper [수신자] [메시지] : 귓속말 보내기\n" +
                "/all [메시지] : 전체 메시지 브로드캐스트\n" +
                "==========================");

        String msg = null;
        try {
            while ((msg = in.readLine()) != null) {
                if ("/list".equalsIgnoreCase(msg)) {
                    listRoom();
                } else if (msg.startsWith("/create")) {
                    createRoom(msg);
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
                }else if (msg.startsWith("/all")) {
                    sendToAll(msg);
                } else {
                    broadcast("[" + id + "] : " + msg); //채팅방 안에서만 메세지 보내기 가능
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listRoom() {
        out.println("=== 채팅방 목록 ===");
        for (Map.Entry<Integer, String> entry : chatTitles.entrySet()) {
            out.println("방 번호 : " + entry.getKey() + ", 제목: " + entry.getValue());
        }
        out.println("===================");
    }

    public void createRoom(String msg) {
        String[] parts = msg.split(" ");
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
        chatRoom.put(roomId, new HashSet<>());
        chatTitles.put(roomId, roomTitle);
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
        if (!chatRoom.containsKey(roomId)) {
            out.println("존재하지 않는 방 번호입니다.");
            return;
        }
        String roomTitle = chatTitles.get(roomId);
        currentRoom = roomId;
        out.println("방 " + roomTitle + "에 입장하셨습니다.");
        broadcast(id + "님이 방에 입장했습니다.");
        chatRoom.get(roomId).add(id);
    }

        public void exitRoom () {
            if (currentRoom == 0) {
                out.println("이동할 방이 없습니다");
                return;
            }
            Set<String> participants = chatRoom.get(currentRoom);
            participants.remove(id);
            out.println("방을 나왔습니다");
            broadcast(id + "님이 방을 나갔습니다");
            currentRoom = 0;
        }

        public void listUsers () {
            out.println("=== 현재 접속 중인 사용자 목록 ===");
            for (String userName : chatClients.keySet()) {
                out.println(userName);
            }
            out.println("=============================");
        }

        public void listRoomUsers () {
            if (currentRoom == 0) {
                out.println("방에 입장하지 않았습니다");
                return;
            }
            out.println("=== 현재 방에 있는 사용자 목록 ===");
            Set<String> participants = chatRoom.get(currentRoom);
            if (participants != null) {
                for (String participant : participants) {
                    out.println(participant);
                }
            }
            out.println("===============================");
        }

        public void whisper (String msg){
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
                out.println("귓속말을 보냈습니다");
            } else {
                out.println("수신자를 찾을 수 없습니다.");
            }
        }
    public void sendToAll(String msg) {
        String message = msg.substring(5); // "/all" 제외한 메시지 부분

        if (message.isEmpty()) {
            out.println("메시지를 입력해주세요.");
            return;
        }

        for (PrintWriter writer : chatClients.values()) {
            writer.println("[전체 메시지] " + message);
        }
    }

        public void broadcast(String msg){
            if (currentRoom == 0) {
                out.println("방에 입장해주세요.");
                return;
            }

            Set<String> participants = chatRoom.get(currentRoom);
            if (participants != null) {
                for (String participant : participants) {
                    PrintWriter writer = chatClients.get(participant);
                    if (writer != null) {
                        writer.println(msg);
                        historyWriter.println(msg);
                        historyWriter.flush();
                    }
                }
            }
        }
    }


//나중에 추가할 기능
// 1. 대화 금칙어 처리
// 2. 공개방/비밀방 설정
// 3. 방장 기능 (강퇴, 방 폭파)
