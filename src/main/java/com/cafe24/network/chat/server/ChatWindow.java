/*
 * 채팅창 객체로서 유저에게 채팅 메시지를 입력받고 보여주는 역할
 * ChatClientApp에서 소켓, 닉네임, PrintWriter를 받아온다.
 * 메시지 받는 스레드는 새로 하나 생성해서 입력 중에도 받을 수 있게 구현했다.
 */
package com.cafe24.network.chat.server;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	
	private BufferedReader br = null;
	private PrintWriter pr = null;

	public ChatWindow(String name, Socket socket, PrintWriter pr) throws IOException{
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		this.br = new BufferedReader( new InputStreamReader(socket.getInputStream() ,"utf-8"));
		this.pr = pr;
	}


	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			
			public void actionPerformed( ActionEvent actionEvent ) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
			}
		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
	}
	
	// 채팅창 종료
	private void finish() {
		//socket 정리
		System.exit(0);
	}
	
	// 채팅창 메시지 출력
	private void updateTextArea(String message) {
		textArea.append(message+"\r\n");
	}
	
	// 메시지 전송
	private void sendMessage() {
		String message = textField.getText();
		
		textField.setText("");
		textField.requestFocus();
		
		// 메시지 입력에 따른 다른 동작
		// quit을 입력할 경우 서버로 메시지 전달 후 채팅종료
		// 그 이외의 메시지는 서버로 메시지 전달
		if ("quit".equals(message)) {
			pr.println("quit:" + message);
			pr.flush();
			finish();
			
		} else {
			pr.println("msg:" + message);
			pr.flush();
		}
		
	}
	
	public void chatClientReceiveThread() {
		
		// 8-1.메시지 받기(데이터 수신 스레드)
		Thread thread = new Thread(()->{
			
			try {
				while(true) {
					String msg = br.readLine();
					
					if(msg == null) {
						System.out.println("closed by server");
						break;
					}
					
					// 8-2. 메시지 출력
					updateTextArea(msg);
				}
			
			} catch(SocketException e){
				System.out.println("[client] closed by server");
				finish();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		});
		
		thread.start();
		
		try {
			//해당 스레드가 종료될 때까지 기다린다
			thread.join();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
