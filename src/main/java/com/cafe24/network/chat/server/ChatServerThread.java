/*
 * 클라이언트에서 온 메시지를 받아서 모든 클라이언트에게 메시지를 뿌려주는 역할
 * Thread를 상속받아서 새로운 쓰레드로 동작한다.
 */
package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

public class ChatServerThread extends Thread{
	
	private Socket socket;
	private String nickname;
	
	private BufferedReader br = null;
	private PrintWriter pr = null;
	private Map<String, Writer> writerMap;

	public ChatServerThread(Socket socket, Map<String, Writer> writerMap) {
		this.socket = socket;
		this.writerMap = writerMap;
		
	}

	@Override
	public void run() {
		
		//1. Remote Host Information
		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
		int remotePort = inetRemoteSocketAddress.getPort();
		
		ChatServer.log("connected by client["+ remoteHostAddress + ":" + remotePort + "]");
		
		try {
			//2. 스트림 얻기
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			//3. 요청 처리
			while(true) {
				
				String request = br.readLine();
				
				//클라이언트가 연결을 강제로 끊은 경우
				if (request == null) {
					ChatServer.log("클라이언트로 부터 연결 끊김");
					doQuit(pr);
					break;
				}
				
				String[] tokens = request.split(":");
				
				//귓속말 기능 추가
				if(tokens.length == 3) {
					
					if(!writerMap.containsKey(tokens[1])) {
						pr.println( tokens[1] + "닉네임은 없습니다.");
						pr.flush();
						continue;
					}
					
					whisper(tokens[1], tokens[2]);
					continue;
				}
				
				//메시지에 따라 다른 동작 실행
				if("join".equals(tokens[0])) {
					
					doJoin(tokens[1], pr);
					
				} else if("msg".equals( tokens[0] )) {
					
					doMessage( tokens[1] );
					
				} else if("quit".equals(tokens[0])) {
					
					doQuit(pr);
					
				} else {
					
					ChatServer.log("에러:알수 없는 요청(" + tokens[0] + ")" );
					
				}

			}
			
		} catch(SocketException e){
			System.out.println("[server] closed by client");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//채팅방 입장
	private void doJoin( String nickName, Writer writer ) {
		this.nickname = nickName;
		
		if(writerMap.get(nickName) != null) {
			pr.println("exists");
			pr.flush();
			return;
		}
		
		//ack
		pr.println("check");
		pr.flush();
		
		String data = "--------------------------------------\r\n";
		data += nickName + "님이 참여하였습니다.\r\n";
		data += "귓속말 기능은 문장앞에 '상대이름:'을 붙여주세요.\r\n";
		data += "--------------------------------------\r\n";
		broadcast(data);
		
		/*whisperMap에 저장*/
		writerMap.put(nickName, writer);
		   
		//ack
		pr.println(data);
		pr.flush();
	}
	
	//채팅 메시지 전송
	private void doMessage( String message ) {
		
		String data = nickname + ":" + message;
		broadcast(data);
		
	}
	
	//채팅방 나가기
	private void doQuit(Writer writer) {
		removeWriter(writer);
		
		String data = nickname + "님이 퇴장 하였습니다.";
		broadcast(data);
		
	}
	
	//모든 클라이언트에게 메시지 보내기
	private void broadcast( String data ) {
		synchronized( writerMap ) {
			
			for( String key : writerMap.keySet() ) {
				
				PrintWriter printWriter = (PrintWriter)writerMap.get(key);
				
				if(key == nickname) {
					continue;
				}
				
				printWriter.println( data );
				printWriter.flush();
			}
		}
	}
	
	//귓속말 보내기 기능 추가
	private void whisper(String nick,String data) {
		
		PrintWriter printWriter = (PrintWriter)writerMap.get(nick);
		
		data = nickname + "님의 귓속말:" + data;
		
		printWriter.println( data );
		printWriter.flush();
		
	}
	
	//채팅방 나간 클라이언트 목록 제거
	private void removeWriter(Writer writer) {
		synchronized (writerMap) {
			writerMap.remove(nickname);
			
		}
	}


}
