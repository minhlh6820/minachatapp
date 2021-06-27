package minaProject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

/**
 * @author javasight
 */

public class Client {
	private int PORT = 9123;
	private String serverIp = "fe80::54f9:2dc:65f3:15e4";
//	private static final String HOSTNAME = "fe80::5cba:fe5d:65cf:18d9";
//  private static final String HOSTNAME = "fe80::b1b6:c7e8:90b8:b913";
//	private static final String HOSTNAME = "fe80::78c:1956:9911:b3ec";
	
//	private ServerSocket serverSocket = null;
//	private Socket socket = null;
//	private BufferedReader in = null;
//	private BufferedWriter out = null;
	private FileInputStream fis = null;
	private FileOutputStream fos = null;
	
	private String name;
	private String pwd;
	private ArrayList<Message> receivedMsgList;
	private ArrayList<Message> sendMsgList;
	private ArrayList<Message> removeMsgList;
	private Map<String, String[]> fileContentMap;
	private int tagNumber;
//	private String finalMsg;
//	private boolean connected = false;
	
	//Testing
	private boolean sending = false;
	
	public Client(String name, String pwd) {
		this.name = name;
		this.pwd = pwd;
		this.receivedMsgList = new ArrayList<Message>();
		this.sendMsgList = new ArrayList<Message>();
		this.removeMsgList = new ArrayList<Message>();
		this.fileContentMap = new HashMap<String, String[]>();
		this.tagNumber = 1;
//		this.finalMsg = "NULL";
	}
  
	private KeyPair generateKeyPair() throws Exception {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }

    private String getPubKeyStr(KeyPair keyPair) {
        PublicKey pubKey = keyPair.getPublic();
        byte[] byte_pubKey = pubKey.getEncoded();
        String str_pubKey = Hex.toHexString(byte_pubKey);
        return str_pubKey;
    }

    private String getPriKeyStr(KeyPair keyPair) {
        PrivateKey priKey = keyPair.getPrivate();
        byte[] byte_priKey = priKey.getEncoded();
        String str_priKey = Hex.toHexString(byte_priKey);
        return str_priKey;
    }

    private PublicKey getPubKeyFromStr(String pubKeyStr) throws Exception {
        byte[] bytePubKey = Hex.decode(pubKeyStr);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey pubKey = kf.generatePublic(new X509EncodedKeySpec(bytePubKey));
        return pubKey;
    }

    public PrivateKey getPriKeyFromStr(String priKeyStr) throws Exception {
        byte[] bytePriKey = Hex.decode(priKeyStr);
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey priKey = kf.generatePrivate(new PKCS8EncodedKeySpec(bytePriKey));
        return priKey;
    }
  
    private InetSocketAddress getSocketAddress(String guestIp, int port) throws IOException {
    	Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
    	NetworkInterface wifiInterface = null;
    	while (networkInterfaces.hasMoreElements()) {
    		NetworkInterface networkInterface = networkInterfaces.nextElement();
    		if (networkInterface.getDisplayName().equals("wlan0")) {
    			wifiInterface = networkInterface;
    			break;
    		}
    	}
    	InetAddress a = InetAddress.getByName(guestIp);
    	Inet6Address dest = Inet6Address.getByAddress(null, a.getAddress(), wifiInterface );
    	InetSocketAddress socketaddr = new InetSocketAddress(dest, port);
    	return socketaddr;
    }
    
    private boolean containNull(String[] strList) {
    	for(int i=0; i<strList.length; i++) {
    		if(strList[i] == null) 
    			return true;
    	}
    	return false;
    }
  
//    private boolean searchTCPServer(String guestIp, int port) {
//    	try {
//    		InetSocketAddress socketaddr = getSocketAddress(guestIp, port); 
//    		socket = new Socket();
//    		socket.connect(socketaddr);
//    		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//    		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
////    		System.out.println("Find server");
//    		return true;
//    	} catch (Exception e) {
////          e.printStackTrace();
//    		return false;
//    	}
//    }
//
//    private void createTCPServer(String guestIp, int port) {
//    	try {
//    		serverSocket = new ServerSocket(port);
//    		socket = serverSocket.accept();
//    		
//    		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//    		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));    		
//    	} catch (IOException e) {
//
//    	}
//    }
    
    private void startSender(String msg, String guestIp, int port) {
		try {
			
			InetSocketAddress socketaddr = getSocketAddress(guestIp, port);
//			System.out.println(socketaddr + "-" + tagNumber);
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					byte[] data = msg.getBytes();
					try {
						DatagramSocket das = new DatagramSocket();	
						das.setBroadcast(true);
						DatagramPacket packet = new DatagramPacket(data, data.length, socketaddr);
//						System.out.println(packet.toString());
						das.send(packet);
//						System.out.println("Client " + name + " send message: " + msg);
						das.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			});
			t.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void startServer(String guestIp, int port, PrivateKey priKey, PublicKey guestPubKey, String guestname) {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					DatagramSocket ds = new DatagramSocket(port);
					
//					System.out.println("Start server!");
					byte[] data = new byte[65535];
					String tmp;
					while(true) {
//						System.out.println("Received message");
						DatagramPacket packet = new DatagramPacket(data, data.length);
                        ds.receive(packet);
                        tmp = new String(data).trim();
                        if(tmp != "") {
                        	Message msgObj = new Message(tmp, guestname, priKey, guestPubKey, 0);
                        	msgObj.readSecuredMsg();
                            String content = msgObj.getContent();
                            if (content == null) {
                               System.out.println("Error message from " + guestname);
                            } else {                      
                                if (content.equals("EXIT ROOM " + port)) {
                                	receivedMsgList.clear();
                                	tagNumber = 1;
                                	sending = false;
//                                    break;
                                } else if (content.equals("JOIN ROOM " + port)) {
                                	sending = true;
//                                    break;
                                } else {
                                	if(!content.contains("RECEIVED")) {
                                    	boolean isReceived = false;
                                        int tagNum = msgObj.getTagNumber();
                                        Iterator i = receivedMsgList.iterator();
                                        while(i.hasNext()) {
                                            Message msgObjTmp = (Message) i.next();
                                            if(msgObjTmp.getTagNumber() == tagNum && msgObjTmp.getContent().equals(content)) {
                                                isReceived = true;
                                                break;
                                            }
                                        }
                                        
                                    	if(!isReceived) {
                                    		if(content.contains("FILE")) {
                                                String fileMsg = content.replaceAll("FILE ", "");
//                                                System.out.println(fileMsg);
                                                String[] infoFile = fileMsg.split("::");
                                                String fileName = infoFile[0].trim();
                                                int index = Integer.valueOf(infoFile[1].trim());
                                                int numMsg = Integer.valueOf(infoFile[2].trim());
                                                String tmpData = infoFile[3].trim();
                                                
//                                                System.out.println(fileName + "::" + index + "::" + numMsg + "::" + tmpData);
                                                
                                                if(fileContentMap.containsKey(fileName)) {
                                                	String[] fileContent = fileContentMap.get(fileName);
                                                	fileContent[index] = tmpData;
//                                                	System.out.println(containNull(fileContent));
                                                	
                                                	if(!containNull(fileContent)) {
                                                		String fileData = "";
                                                		for(String s: fileContent)
                                                			fileData += s;
                                                		byte[] fileStr = Base64.getDecoder().decode(fileData);
//            	                                        
            				  	  		 				fos = new FileOutputStream(fileName);
            				  	  		 				fos.write(fileStr);
            				  	  		 				fos.flush();
            				  	  		 				fos.close();
//            		  		 							System.out.println(file.length() + "-" + fileSize);
            				  	  		 				System.out.println("File " + fileName + " created");
            				  	  		 				String completedMsg = "RECEIVED FILE " + fileName;
            				  	  		 				Message obj = new Message(completedMsg, name, priKey, guestPubKey, 0);
            				  	  		 				completedMsg = obj.createSecuredMsg();
            				  	  		 				startSender(completedMsg, guestIp, port);
                                                	}
                                                } else {
                                                	String[] fileContent = new String[numMsg+1];
                                                	fileContent[index] = tmpData;
                                                	fileContentMap.put(fileName, fileContent);
                                                }
                                                
//                                                System.out.println("Received msg file " + fileName);
                                                
                                        	} else {
                                        		System.out.println(name + " received message: " + content);                                   		
                                        	}
                                    		receivedMsgList.add(msgObj);
                                    	}                                	                              	
                                                                        
                                        String responseMsg = "RECEIVED MESSAGE " + tagNum;
//                                        System.out.println("Send response msg: " + responseMsg);
                                        Message resMsgObj = new Message(responseMsg, name, priKey, guestPubKey, 0);
                                        String resMsg = resMsgObj.createSecuredMsg();
                                        startSender(resMsg, guestIp, port);
                                           
//                                        Iterator it = sendMsgList.iterator();
//                                        while(it.hasNext()) {
//                                            msgObj = (Message) it.next();
//                                            String securedMsg = msgObj.createSecuredMsg();
//                                            startSender(securedMsg, guestIp, port, priKey, guestPubKey);
//                                        }                                                                       
                                    }
                                    
                                    if(content.contains("RECEIVED")) {
                                    	if(content.contains("RECEIVED FILE")) {
                                    		String fileName = content.replaceAll("RECEIVED FILE ", "");
                                    		if(fileContentMap.containsKey(fileName))
                                    			fileContentMap.remove(fileName);
//                                    		System.out.println(fileContentMap.containsKey(fileName));
                                    		System.out.println(guestname + " get file " + fileName + " from " + name);
                                    	} else {
                                    		int tagNum = Integer.valueOf(content.replaceAll("RECEIVED MESSAGE ", ""));
                                    		
                                    		Iterator i = sendMsgList.iterator();
                                            while(i.hasNext()) {
                                                Message msgObjTmp = (Message) i.next();
                                                if(msgObjTmp.getTagNumber() == tagNum) {
                                                    removeMsgList.add(msgObjTmp);
                                                }
                                            }
                                            
//                                            if(!removeMsgList.isEmpty()) {
//                                                i = removeMsgList.iterator();
//                                                while(i.hasNext()) {
//                                                    Message obj = (Message) i.next();
//                                                    tagNum = obj.getTagNumber();
//                                                    System.out.println(guestname + " get message #" + tagNum);
//                                                  
//                                                }
//                                            }
                                            
                                    	}
                                    }
                                }
                                                                         
                            }
                        }
                        data = new byte[65535];
					}
//					System.out.println("Exit");
//					ds.close();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(port);
				}
			}
		});
		t.start();
	}

    public void run() {     
    	try {
    		KeyPair keyPair = generateKeyPair();
    		String str_pKey = getPubKeyStr(keyPair);
//    		String name = "client1";
//    		String pwd = "pass1";
    		InetSocketAddress socketaddr = getSocketAddress(serverIp, PORT);
    		// Converting string to Bytes
    		// byte_pubkey  = Base64.getDecoder().decode(str_pKey);
    		String msg = "LOGIN: " + name + "--" + pwd + "--" + str_pKey;  
    		NioSocketConnector connector = new NioSocketConnector();
    		// Create filter chain
    		connector.getFilterChain().addLast("logger", new LoggingFilter());
    		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
         
    		ClientHandler handler = new ClientHandler(name);
    		connector.setHandler(handler);
    		//Bind to server
    		ConnectFuture future = connector.connect(socketaddr);
    		future.awaitUninterruptibly();
    		IoSession session = future.getSession();
    		session.write(msg);
    		session.getCloseFuture().awaitUninterruptibly();
    		boolean accepted = handler.isAccepted();
//  		 System.out.println(accepted);
    		if(accepted) {
//  			 handler.stopLogger();
    			
     		 	future = connector.connect(socketaddr);
    	 		future.awaitUninterruptibly();
    	 		session = future.getSession();
    	 		session.write("CONNECT: " + name + "--client0");
    	 		session.getCloseFuture().awaitUninterruptibly();
    	 		
    	 		int port = Integer.valueOf(handler.getPort());
  	  		 	String guestIp = handler.getGuestIP();
  	  		 	String guestname = handler.getGuestname();
  	  		 	String guestPubKeyStr = handler.getGuestPubKey();
  			 
//    			future = connector.connect(socketaddr);
//  	  		 	future.awaitUninterruptibly();
//  	  		 	session = future.getSession();
//  	  		 	session.getCloseFuture().awaitUninterruptibly();
  	  		 	handler.stopLogger();
  	  		 	connector.dispose();
  	  		 	
//  	  		 	boolean server = handler.isServer();
  	  	
  	  		 	PublicKey guestPubKey = getPubKeyFromStr(guestPubKeyStr);
  	  		 
//  	  		 	System.out.println(name + "-" + guestname + "-" + guestIp + "-" + port);
  	  		 
//  	  		 DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
//  	  		 LocalDateTime now = LocalDateTime.now();
  	  		 	
//  	  		 	Thread thread = new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						try {
//							while(true) {
//								if(!searchServer(guestIp, port)) {
//				  	  		 		createServer(guestIp, port);
//				  	  		 	}
//								if(socket.isConnected()) {
//									System.out.println("Connected");
//									connected = true;
//									tagNumber = 0;
//									receivedMsgList.clear();
//									
//									in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//				  	  		 		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//				  	  		 		
//				  	  		 		String testMsg = "Test message";
//				  	  		 		Message msgObj = new Message(testMsg, name, keyPair.getPrivate(), guestPubKey);
//				  	  		 		tagNumber++;
//				  	  		 		out.write(msgObj.createSecuredMsg(tagNumber));
//				  	  		 		out.newLine();
//				  	  		 		out.flush();
//				  	  		 		
//				  	  		 		while(connected) {
//				  	  		 			String line = in.readLine();
//				  	  		 			if(line != null) {
//				  	  		 				msgObj = new Message(line, guestname, keyPair.getPrivate(), guestPubKey);
//				  	  		 				String receivedMsg = msgObj.readSecuredMsg(receivedMsgList.size() + 1);
//				  	  		 				if(receivedMsg == null) {
//				  	  		 					System.out.println("Error msg from " + guestname);
//				  	  		 				} else {
//				  	  		 					receivedMsgList.add(receivedMsg);
//				  	  		 					
//				  	  		 					if(receivedMsg.equals("EXIT")) {
//				  	  		 						connected = false;
//				  	  		 						System.out.println("Disconnected");
//				  	  		 					} else if(receivedMsg.contains("File")) {
//				  	  		 						String fileMsg = receivedMsg.replaceAll("File ", "");
//				  	  		 						String[] infoFile = fileMsg.split("---", 2);
//				  	  		 						String fileName = infoFile[0].trim();
//				  	  		 						byte[] fileStr = Base64.getDecoder().decode(infoFile[1].trim());
//	                                        
//				  	  		 						fos = new FileOutputStream(fileName);
//				  	  		 						fos.write(fileStr);
//				  	  		 						fos.flush();
//				  	  		 						fos.close();
////		  		 									System.out.println(file.length() + "-" + fileSize);
//				  	  		 						System.out.println("File " + fileName + " created");
//				  	  		 					} else {
//				  	  		 						System.out.println("Message from " + guestname + " to " + name + ": " + receivedMsg);
//				  	  		 					}
//				  	  		 				}
//				  				 
//				  	  		 			}
//				  	  		 			
//				  	  		 		}
//				  	  		 		in.close();
//				  	  		 		out.close();
//				  	  		 		socket.close();
//				  	  		 		if(serverSocket != null) serverSocket.close();
//								} else {
//									connected = false;
//								}
//							}
//							
//						} catch (Exception e) {
//							
//						}
//					}
//				});
//  	  		 	thread.start();
//  	  		 
//  	  		 	if(!searchServer(guestIp, port)) {
//  	  		 		createServer(port);
//  	  		 	}
//  	  		 	if(server) createServer(port);
//  	  		 	else {
//  	  		 		Thread.sleep(3000);
//  	  		 		searchServer(guestIp, port);
//  	  		 	}
  	  		 	
//  	  		 	if(socket.isConnected()) {
//  	  		 		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//  	  		 		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
  	  		 		 	  		 	
//  	  		 		String testMsg = "Test message";
//  	  		 		Message msgObj = new Message(testMsg, name, keyPair.getPrivate(), guestPubKey);
//  	  		 		tagNumber++;
//  	  		 		out.write(msgObj.createSecuredMsg(tagNumber));
//  	  		 		out.newLine();
//  	  		 		out.flush();
  	  		 		
//  	  		 		testMsg = "EXIT";
//  	  		 		msgObj = new Message(testMsg, name, keyPair.getPrivate(), guestPubKey);
//	  		 		tagNumber++;
//	  		 		out.write(msgObj.createSecuredMsg(tagNumber));
//	  		 		out.newLine();
//	  		 		out.flush();
	  		 		
//	  		 		//Send message to Mina Server
//                    InetSocketAddress socketAddress = getSocketAddress(HOSTNAME, PORT);
//                    socket = new Socket();
//                    socket.connect(socketAddress);
//
//                    out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//                    out.write("DISCONNECT " + name + "-" + guestname);
//                    out.newLine();
//                    out.flush();
//                    out.close();
//                    socket.close();
	  		 		  		 		
//  	  		 		in.close();
//	  		 		out.close();
//	  		 		socket.close();
//	  		 		if(serverSocket != null) serverSocket.close();
  	  		 		
//  	  		 	}
  	  		 	
//  	  		 	Thread t = new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						try {
//							while (socket.isConnected()) {
//			  		 			String line = in.readLine();
//			  		 			if(line != null) {
//			  		 				Message msgObj = new Message(line, guestname, keyPair.getPrivate(), guestPubKey);
//			  		 				String receivedMsg = msgObj.readSecuredMsg(receivedMsgList.size() + 1);
//			  		 				if(receivedMsg == null) {
//			  		 					System.out.println("Error msg from " + guestname);
//			  		 				} else {
//			  		 					receivedMsgList.add(receivedMsg);
//  	  		 							System.out.println("Get message from " + guestname + " to " + name);
//  	  		 							if(receivedMsg.equals("EXIT"))
//  	  		 								break;
//  	  		 							if(receivedMsg.contains("File")) {
//  	  		 								String fileMsg = receivedMsg.replaceAll("File ", "");
//  	  		 								String[] infoFile = fileMsg.split("---", 2);
//  	  		 								String fileName = infoFile[0].trim();
//  	  		 								byte[] fileStr = Base64.getDecoder().decode(infoFile[1].trim());
//                                        
//  	  		 								fos = new FileOutputStream(fileName);
//  	  		 								fos.write(fileStr);
//  	  		 								fos.flush();
//  	  		 								fos.close();
////	  		 								System.out.println(file.length() + "-" + fileSize);
//  	  		 								System.out.println("File " + fileName + " created");
//  	  		 							}
//			  		 				}
//			  				 
//			  		 			}
//			  		 		}
//							System.out.println("Exit");
//						} catch(Exception e) {
//							
//						}
//						
//					}
//				});
//  	  		 	t.start();
  	  		 	
//  	  		 	System.out.println("Disconnected");
//  	  		 	Thread.sleep(8000);
//  	  		 	if(!searchServer(guestIp, port)) {
//	  		 		createServer(port);
//	  		 	}
//  	  		 	
//  	  		 	if(socket.isConnected()) {
//  	  		 		System.out.println("Reconnected");
//  	  		 		tagNumber = 0;
//  	  		 		receivedMsgList.clear();
//  	  		 		
//  	  		 		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//	  		 		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//	  		 	
//	  		 		String testMsg = "Another test message";
//	  		 		Message msgObj = new Message(testMsg, name, keyPair.getPrivate(), guestPubKey);
//	  		 		tagNumber++;
//	  		 		out.write(msgObj.createSecuredMsg(tagNumber));
//	  		 		out.newLine();
//	  		 		out.flush();
//	  		 		
//	  		 		String fileName = "Screenshot from 2021-03-29 18-06-32.png";
//	  		 		File file = new File(fileName);
//	  		 		if(file != null) {
//	  		 			byte[] bytes = new byte[(int) file.length()];
//	  		 			fis = new FileInputStream(file);
//	  		 			fis.read(bytes, 0, bytes.length);
//	  		 			String fileStr = Base64.getEncoder().encodeToString(bytes);
//	  		 			String fileMsg = "File " + fileName + "---" + fileStr;
//	  		 			msgObj = new Message(fileMsg, name, keyPair.getPrivate(), guestPubKey);
//	  		 			tagNumber++;
//	  		 			out.write(msgObj.createSecuredMsg(tagNumber));
//	  		 			out.newLine();
//	  		 			out.flush();
//	  		 		}
//  	  		 		
//  	  		 	}
//  	  		 	t = new Thread(new Runnable() {
//				
//  	  		 		@Override
//  	  		 		public void run() {
//  	  		 			// TODO Auto-generated method stub
//  	  		 			try {
//  	  		 				while (socket.isConnected()) {
//  	  		 					String line = in.readLine();
//  	  		 					if(line != null) {
//  	  		 						Message msgObj = new Message(line, guestname, keyPair.getPrivate(), guestPubKey);
//  	  		 						String receivedMsg = msgObj.readSecuredMsg(receivedMsgList.size() + 1);
//  	  		 						if(receivedMsg == null) {
//  	  		 							System.out.println("Error msg from " + guestname);
//  	  		 						} else {
//  	  		 							receivedMsgList.add(receivedMsg);
//  	  		 							System.out.println("Get message from " + guestname + " to " + name);
//  	  		 							if(receivedMsg.equals("EXIT"))
//  	  		 								break;
//  	  		 							if(receivedMsg.contains("File")) {
//  	  		 								String fileMsg = receivedMsg.replaceAll("File ", "");
//  	  		 								String[] infoFile = fileMsg.split("---", 2);
//  	  		 								String fileName = infoFile[0].trim();
//  	  		 								byte[] fileStr = Base64.getDecoder().decode(infoFile[1].trim());
//                                        
//  	  		 								fos = new FileOutputStream(fileName);
//  	  		 								fos.write(fileStr);
//  	  		 								fos.flush();
//  	  		 								fos.close();
////	  		 								System.out.println(file.length() + "-" + fileSize);
//  	  		 								System.out.println("File " + fileName + " created");
//  	  		 							}
//  	  		 						}
//		  				 
//  	  		 					}
//  	  		 				}
//  	  		 				System.out.println("Exit");
//  	  		 				in.close();
//  	  		 				out.close();
//  	  		 				socket.close();
//  	  		 				if(serverSocket != null) serverSocket.close();
//  	  		 			} catch(Exception e) {
//						
//  	  		 			}
//					
//  	  		 		}
//  	  		 	});
//  	  		 	t.start();
//  	  		 	System.out.println(socket.isClosed());
//  	  		 	Thread.sleep(10000);
  	  		 	startServer(guestIp, port, keyPair.getPrivate(), guestPubKey, guestname);

  	  		 	//Sending file
//	  		 	String fileName = "F6MUU6(VJ)2.pdf";
		 		String fileName = "Screenshot from 2021-03-29 18-06-32.png";
		 		File file = new File(fileName);
		 		if(file != null) {
		 			try {
		 				byte[] bytes = new byte[(int) file.length()];
		 				fis = new FileInputStream(file);
		 				fis.read(bytes, 0, bytes.length);
		 				String fileStr = Base64.getEncoder().encodeToString(bytes);
		 				int sizeData = 16384;
                
		 				int numMsg = (int) fileStr.length()/sizeData;
		 				for(int start = 0; start < fileStr.length(); start += sizeData) {
		 					int index = (int) start/sizeData;
//                    		byte[] tmpBytes = Arrays.copyOfRange(bytes, start, (int) Math.min(file.length(), start + sizeData));
//                    		String subFileStr = Base64.getEncoder().encodeToString(tmpBytes);
		 					String subFileStr = fileStr.substring(start, Math.min(fileStr.length(), start + sizeData));
		 					String fileNameMsg = "FILE " + file.getName() + "::" + index + "::" + numMsg + "::" + subFileStr;
//                    		System.out.println(fileNameMsg);
		 					Message msgObj = new Message(fileNameMsg, name, keyPair.getPrivate(), guestPubKey, tagNumber);
		 					sendMsgList.add(msgObj);
		 					tagNumber++;
		 				}
		 				fis.close();
//		 				System.out.println("Sending file " + fileName);
		 				sending = false;
                
		 			} catch (Exception e) {
		 				System.out.println("Error when " + name + " sending file " + fileName);
		 			}
		 		}
		 		
		 		//Sending text
//	  		 	for(int i=0; i<40; i++) {
//  		 		sendMsgList.add(new Message("Test message #" + i, name, keyPair.getPrivate(), guestPubKey, tagNumber));
//		 			tagNumber++;
//  		 	}	
  	  		 	
  	  		 	Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						String checkMsg = "";
		                try {
		                    Message msgObj = new Message("JOIN ROOM " + port, name, keyPair.getPrivate(), guestPubKey, 0);
		                    checkMsg = msgObj.createSecuredMsg();
		                } catch (Exception e) {
		                    System.err.println("Cannot create check message");
		                }
						
						while(true) {
							try {
								if(removeMsgList.size() != 0) {
			  	  		 			sendMsgList.removeAll(removeMsgList);
			  	  		 			removeMsgList.clear();
			  	  		 		}
//			  	  		 			System.out.println(sendMsgList.size());
								startSender(checkMsg, guestIp, port);
		                        Thread.sleep(1000);
//		                        if(sending)
//		                        	System.out.println(name + " check join: " + sending);
								
			  	  		 		if(sending && sendMsgList.size() != 0) {
			  	  		 			try {
			  	  		 				Iterator i = sendMsgList.iterator();
			  	  		 				while(i.hasNext()) {
			  	  		 					Message msgObj = (Message) i.next();
			  	  		 					String securedMsg = msgObj.createSecuredMsg();
			  	  		 					startSender(securedMsg, guestIp, port);
			  	  		 				}
			  	  		 				sending = false;
			  	  		 				Thread.sleep(3000);
			  	  		 			} catch (Exception e) {
			  	  		 				e.printStackTrace();
			  	  		 			}
			  	  		 		}
							} catch (Exception e) {
								e.printStackTrace();
							}
		  	  		 		
		  	  		 	}	 	
					}
				});
  	  		 	t.start();	  		 	
  	  		 	
    		}
    	} catch(Exception e) {
          e.printStackTrace();
    	}
      
  }

  public static void main(String[] args) {
	    
//	  for(int i=1; i<101; i++) {
//		  int index = i;
//		  Thread thread = new Thread(new Runnable() {
//			  @Override
//			  public void run() {
//				  Client client = new Client("client" + index, "pass" + index);
//				  client.run();
//			  }
//		  });
//		  thread.start();
//	  }
//      
	  Client client = new Client("client1", "pass1");
	  client.run();
  }
}
