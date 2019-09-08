package Client;

import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;

public class Client extends JFrame {
	private JTextField userMessage;
	private JTextArea userMessages;
	private String username;
	private String message;
	private String serverIP;
	private InetAddress multiCastAddress;
	private InetAddress uniCastAddress;
	private MulticastSocket multiCastConnection;
	private DatagramSocket uniCastconnection;

	public Client(String host) {
		super("DIY Messenger Client");
		serverIP = host;
		username = "Anonymous";
		setupUI();
	}

	private void setupUI() {
		userMessage = new JTextField();
		userMessage.setEditable(false);
		userMessage.addActionListener(
			e -> {
				sendMessage(e.getActionCommand());
				userMessage.setText("");
			}
		);
		TopBar topbar = new TopBar(this);
		add(topbar, BorderLayout.NORTH);
		add(userMessage, BorderLayout.SOUTH);
		userMessages = new JTextArea();
		add(new JScrollPane(userMessages), BorderLayout.CENTER);
		setSize(450, 250);
		setVisible(true);
	}

	public void start() {
		try {
			connectToServer();
		} catch (IOException e) {
			showMessage("\n Unable to connect at this time");
		}
	}

	public void setUsername(String newUserName) {
		username = newUserName;
	}

	private void connectToServer() throws IOException {

		// Connect to multicast socket and join group
		multiCastAddress = InetAddress.getByName(serverIP);
		multiCastConnection = new MulticastSocket();
		multiCastConnection.joinGroup(multiCastAddress);

		// Set up datagram socket for sending text messages
		uniCastAddress = InetAddress.getByName("127.0.0.1");
		uniCastconnection = new DatagramSocket();
		showMessage("You are now connected! Say Hi.");
		fieldEditable(userMessage, true);

		// Start thread that handles setting up receiving messages
		Thread messageReceiverWorker = new MessageReceiverWorker(uniCastconnection, this, uniCastAddress);
		messageReceiverWorker.start();

		// Start thread that handles sending audio
		Thread audioSenderWorker = new AudioSenderWorker(multiCastConnection, this, multiCastAddress);
		audioSenderWorker.start();
	}

	private void sendMessage(String message) {
		try {
			// Create buffer to store message bytes
			byte[] userMessageBuffer = new byte[1024];

			// String to bytes
			userMessageBuffer = message.getBytes();

			// Build datagram packet to send to server
			DatagramPacket packet = new DatagramPacket(userMessageBuffer, userMessageBuffer.length, uniCastAddress, 3001);

			// Send to server
			uniCastconnection.send(packet);
		} catch(IOException e) {
			userMessages.append("\n Error sending message");
		}
	}

	public void showMessage(final String message) {
		SwingUtilities.invokeLater(
			() -> userMessages.append(message)
		);
	}

	private void fieldEditable(JTextField field, final boolean ableToType) {
		SwingUtilities.invokeLater(
			() -> field.setEditable(ableToType)
		);
	}

}
