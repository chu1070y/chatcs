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
import java.util.List;

public class ChatServerThread extends Thread{
	
	private Socket socket;
	private String nickname;
	private List<Writer> listWriters;
	
	private BufferedReader br = null;
	private PrintWriter pr = null;

	public ChatServerThread(Socket socket, List<Writer> listWriters) {
		this.socket = socket;
		this.listWriters = listWriters;
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
				
				System.out.println("-+---request:" + request);
				
				if (request == null) {
					ChatServer.log("클라이언트로 부터 연결 끊김");
					doQuit(pr);
					break;
				}
				
				String[] tokens = request.split(":");
				System.out.println(tokens[0]);
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
	
	private void doJoin( String nickName, Writer writer ) {
		this.nickname = nickName;
		
		String data = nickName + "님이 참여하였습니다.";
		broadcast(data);
		   
		/* writer pool에  저장 */
		addWriter(writer);
		   
		//ack
		pr.println(data);
		pr.flush();
	}
	
	private void doMessage( String message ) {
		
		String data = nickname + ":" + message;
		
		broadcast(data);
		
	}
	
	private void doQuit(Writer writer) {
		removeWriter(writer);
		
		String data = nickname + "님이 퇴장 하였습니다.";
		broadcast(data);
		
	}

	private void addWriter( Writer writer ) {
	   synchronized( listWriters ) {
	      listWriters.add( writer );
	   }
	}
	
	private void broadcast( String data ) {
		synchronized( listWriters ) {
			
			for( Writer writer : listWriters ) {
				PrintWriter printWriter = (PrintWriter)writer;
				printWriter.println( data );
				printWriter.flush();
			}
		}
	}
	
	private void removeWriter(Writer writer) {
		synchronized (listWriters) {
			
			listWriters.remove(listWriters.indexOf(writer));
			
		}
	}


	

}
