### 자바 채팅 구현 프로젝트 (서버-클라이언트)

테킷 백엔드스쿨 10기 첫번째 프로젝트
간단한 Java 소켓 기반의 다중 클라이언트 채팅 서버 구
-----
#### 구현 기능
1. 채팅방 생성
2. 채팅방 입장/퇴장 
3. 채팅방 목록 조회
4. 사용자 목록 조회 (전체/현재 채팅방)
5. 특정 사용자에게 귓속말 보내기
6. 채팅방 내 메시지 브로드캐스트
7. 채팅 기록 저장 (텍스트 파일 저장)
8. 전체 메시지 브로드캐스트
9. 채팅방 비밀번호 설정

#### 사용법
1. 채팅 서버 실행 (ChatServer)
2. 채팅 클라이언트 실행 후 서버 접속 (ChatClient)
3. ID 입력 후 채팅방 생성/입장
4. 채팅방 명령어를 사용하여 새로운 채팅방을 생성하거나 기존 채팅방에 입장
    - `/list`: 생성된 채팅방 목록을 확인합니다.
    - `/create [방제목]`: 새로운 채팅방을 생성합니다.
    - `/join [방번호]`: 기존 채팅방에 입장합니다.
    - `/exit`: 현재 채팅방에서 나갑니다.
    - `/users`: 현재 접속 중인 사용자 목록을 확인합니다.
    - `/roomusers`: 현재 채팅방에 있는 사용자 목록을 확인합니다.
    - `/whisper [수신자] [메시지]`: 특정 사용자에게 귓속말을 보냅니다.
    - `/all [메시지]`: 모든 사용자에게 메시지를 전달합니다.

