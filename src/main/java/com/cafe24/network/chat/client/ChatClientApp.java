/*
 * 채팅 접속하는 객체
 * 서버와 소켓을 연결한 후
 * 자신의 닉네임을 서버로 전송한다.
 */
package com.cafe24.network.chat.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.cafe24.network.chat.server.ChatWindow;

public class ChatClientApp {
	
	private static final String SERVER_IP = "192.168.1.67";
	private static final int SERVER_PORT = 7000;

	public static void main(String[] args) {
		String name = null;
		Scanner scanner = null;
		Socket socket = null;

		try {
			//1. 키보드 연결
			scanner = new Scanner(System.in);
			
			//2. 소켓 생성
			socket = new Socket();
			
			//3. 소켓 연결
			socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT));
			
			//4. writer/reader 생성
			PrintWriter pr = new PrintWriter( new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true );
			BufferedReader br = new BufferedReader( new InputStreamReader(socket.getInputStream() ,"utf-8"));
			
			//5. join 프로토콜
			while( true ) {
				
				System.out.println("닉네임을 입력하세요.");
				System.out.print(">>>");
				name = scanner.nextLine();
				
				// 닉네임 미입력시 재요청
				if (name.isEmpty() == true ) {
					System.out.println("닉네임은 한글자 이상 입력해야 합니다.\n");
					continue;
				}
				
				// 닉네임 중복 검사
				// 서버로 닉네임 전달
				pr.println("join:" + name);
				pr.flush();
				
				//서버로부터 닉네임 중복 확인 수신
				String sysMsg = br.readLine();
				
				if("check".equals(sysMsg)) {
					break;
				}
				System.out.println("닉네임이 중복입니다. 다시 작성해주세요.");
				
			}
			
			
			// 6. 채팅창 객체 생성
			ChatWindow window = new ChatWindow(name,socket,pr,br);
			
			// 7. 채팅창 열기
			window.show();
			
			// 8. 메세지 수신 스레드 생성
			window.chatClientReceiveThread();
			
		} catch( IOException e) {
			System.out.println("error:" + e);
			
		} finally {
			try {
				if(scanner != null) {
					scanner.close();
				}
				
				if (socket != null && socket.isClosed() == false) {
					socket.close();
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
