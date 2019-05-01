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
	
	private void finish() {
		//socket 정리
		System.exit(0);
	}
	
	private void updateTextArea(String message) {
		textArea.append(message+"\r\n");
	}
	
	private void sendMessage() {
		String message = textField.getText();
		
		textField.setText("");
		textField.requestFocus();
		
		// 메시지 입력에 따른 다른 동작
		if ("quit".equals(message)) {
			pr.println("quit:" + message);
			pr.flush();
			finish();
			
		} else {
			pr.println("msg:" + message);
			pr.flush();
		}
		
	}
	
	public void receiveMsg() {
		
		// 7-1.메시지 받기(데이터 수신 스레드)
		new Thread(()->{
			
			try {
				while(true) {
					String msg = br.readLine();
					
					if(msg == null) {
						System.out.println("closed by server");
						break;
					}
					
					// 7-2. 메시지 출력
					updateTextArea(msg);
				}
			
			} catch(SocketException e){
				System.out.println("[client] closed by server");
				finish();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}).start();
	}

}
