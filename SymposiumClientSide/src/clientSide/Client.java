package clientSide;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

//- Be able to access both ip's from both classes
//- Interchange file names with server ip first or client ip first


public class Client extends JFrame{

	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private BufferedWriter writer;
	
	public Client(String host) {
		super("Symposium Client");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendMessage(event.getActionCommand());//server       client
					File check = new File("" + connection.getInetAddress().getHostName() + "+" + serverIP+ ".txt");
					if(check.isFile()){
						try {
							writer = new BufferedWriter(new FileWriter("C:/Users/" + System.getProperty("user.name") + "/git/symposium-clientside/"
									+ "SymposiumClientSide/"+connection.getInetAddress().getHostName() + "+" 
									+ serverIP+ ".txt", true));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
					}else{
						try{
							File texting = new File("" + connection.getInetAddress().getHostName() + "+" + serverIP+ ".txt");
							writer = new BufferedWriter(new FileWriter(texting, true));
						}catch(IOException e){
							e.printStackTrace();
						}
					}
					try {
						if (!event.getActionCommand().equals("END")){
							writer.write("Client: " + event.getActionCommand() + "\r\n");
							System.out.println(event.getActionCommand());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					userText.setText("");
				}
			}
		);
		add(userText, BorderLayout.NORTH);
		chatWindow = new JTextArea();
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(300,150);
		setVisible(true);
	}
	
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException){
			showMessage("\n Client terminated connection");
		}catch(IOException ioException){
			ioException.printStackTrace();
		}finally{
			closeAll();
		}
	}
	
	//connect to server
	public void connectToServer() throws IOException{
		showMessage("Attempting connection... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		showMessage("Connected to: " + connection.getInetAddress().getHostName());
	}
	
	//set up streams to send and receive messages
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are now good to go! \n");
	}
	
	//while chatting with server
	private void whileChatting() throws IOException{
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				showMessage("\n" + message);
			}catch(ClassNotFoundException classNotFoundException){
				showMessage("\n I don't know that object type");
			}
		}while(!message.equals("SERVER - END"));
	}
	
	//close the streams and sockets
	private void closeAll(){
		showMessage("\n Closing all down... ");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//send messages to the server
	private void sendMessage(String message){
		try{
			output.writeObject("CLIENT - " + message);
			output.flush();
			showMessage("\nCLIENT -" + message);
		}catch(IOException ioException){
			chatWindow.append("\n Something went wrong when sending message!");
		}
	}
	
	// change/update chatWindow
	private void showMessage(final String m){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(m);
				}
			}
		);
	}
	
	//gives user permission to type into the text box
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);	
				}
			}
		);		
	}
	
	public String getServerIP(){
		return connection.getInetAddress().getHostAddress();
	}
}
