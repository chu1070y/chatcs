/*
 * 포트를 열어 클라이언트로부터 온 소켓을 받아 accept한다.
 * 사용자 정보는 List에 담아 저장한다.
 * 받은 소켓과 사용자 정보는 ChatServerThread 객체로 넘긴다.
 */
package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ChatServer {
	// 포트번호
	private static final int PORT = 7000;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		Map<String, Writer> writerMap = new HashMap<String, Writer>();
		
		try {
			// 1. 서버소켓생성
			serverSocket = new ServerSocket();
			
			// 2. 바인딩
			String hostAddress = InetAddress.getLocalHost().getHostAddress();
			serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
			log("연결 기다림 " + hostAddress + ":" + PORT);
			
			// 3. 요청대기
			// 서버 접속을 감지하는 스레드는 메인 스레드를 이용하고
			// 메시지가 들어왔을 때 뿌려주는 쓰레드는 새로 생성한다.
			while(true) {
				Socket socket = serverSocket.accept();
				new ChatServerThread(socket, writerMap).start();
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {	
			try {
				if(serverSocket != null && !serverSocket.isClosed()) {
					serverSocket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}
	
	public static void log(String log) {
		System.out.println("[server#" + Thread.currentThread().getId() + "] " + log);
	}

}
