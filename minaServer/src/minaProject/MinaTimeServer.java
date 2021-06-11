package minaProject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class MinaTimeServer {
	private final int PORT = 9123;
	private ArrayList<User> userList = new ArrayList<User>();
	
//	private static Client client = null;
	private void createDb() {
		for(int i=0; i<101; i++) {
			User u = new User(userList.size(), "client" + i, "pass" + i);
			userList.add(u);
		}
		
		for(User a: userList) {
			for(User b: userList) {
				if(!b.getName().equals(a.getName()))
					a.addFriend(b);
			}
		}
	}
	
	public void run() throws IOException {
		createDb();
		
		//1. Get public key, IP from client
		//Add SocketAcceptor to listen for incoming connections
		IoAcceptor acceptor = new NioSocketAcceptor();
		//Add filter to configuration
		//LoggingFilter: log all information (created sessions, messages received, sent, session closed)
		//ProtocolCodec Filter: translate binary or protocol specific data into msg object, vice versa (use TextLine factory)
		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
		acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));
//		acceptor.getFilterChain().addLast("executor", new ExecutorFilter());
		//Define handler to service client connections and requests
		acceptor.setHandler(new TimeServerHandler(userList, acceptor));
		//Make socket specific settings for socket: buffer size, idle property
		acceptor.getSessionConfig().setReadBufferSize(2048);
		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
		//Bind acceptor to a port
		acceptor.bind(new InetSocketAddress(PORT));
			
//		Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//            	Client client = new Client();
//                try {
//              	  client.run();
//              	  
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        thread.start();
	}
	
	public static void main(String[] args) throws IOException {
		MinaTimeServer server = new MinaTimeServer();
		server.run();
	}

}

class User {
	private int id;
	private String ip;
	private String name;
	private String pwd;
	private String pKey;
	private boolean connected;
	private ArrayList<User> friendsList;
	
	public User(int id, String name, String pwd) {
		this.id = id;
		this.pKey = null;
		this.name = name;
		this.pwd = pwd;
		this.ip = null;
		this.connected = false;
		this.friendsList = new ArrayList<User>();
	}
	
	public ArrayList<User> getFriendsList() {
		return this.friendsList;
	}
	
	public void addFriend(User u) {
		this.friendsList.add(u);
	}
	
	public String getIp() {
		return this.ip;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getPKey() {
		return this.pKey;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPwd() {
		return this.pwd;
	}
	
	public boolean isConnected() {
		return this.connected;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	public void setPKey(String pKey) {
		this.pKey = pKey;
	}
}

class TimeServerHandler extends IoHandlerAdapter
{
	private final Logger logger = Logger.getLogger(getClass());
//	private String hostname = null;
//	private String pKey = null;
	private IoAcceptor acceptor = null;
	private ArrayList<User> userList = null;
	private Map<String, String> connectedMap = new HashMap<String, String>();
	
	
	public TimeServerHandler(ArrayList<User> userList, IoAcceptor acceptor) {
		// TODO Auto-generated constructor stub
		this.userList = userList;
		this.acceptor = acceptor;
	}
    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception
    {
        cause.printStackTrace();
    }
        
    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception
    {
//    	super.messageReceived(session, message);
        String str = message.toString().trim();
//
        if( str.contains("LOGIN")) {
        	boolean connected = false;
        	str = str.replaceFirst("LOGIN: ", "");
        	String[] userInfo = str.split("--", 3);
        	String name = userInfo[0].trim();
        	String pwd = userInfo[1].trim();
        	String pKey = userInfo[2].trim();
        	InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
            String inetAddress = socketAddress.getHostName();
            inetAddress = inetAddress.replaceFirst("%.*", "");
//            System.out.println(name + "-" + pwd);
            for(User u: userList) {
//            	System.out.println(u.getName() + "-" + u.getPwd());
//            	System.out.println(u.getName().equals(name));
            	if(u.getName().equals(name) && u.getPwd().equals(pwd)) {
            		u.setIp(inetAddress);
            		u.setPKey(pKey);
            		u.setConnected(true);
            		connected = true;
            		session.write("ACCEPT LOGIN: " + name);
            		break;
            	}
            }
            if(connected == false) {
            	session.write("REJECT LOGIN: " + name);
            }
        }
        
        if(str.contains("LOGOUT")) {
        	String name = str.replaceAll("LOGOUT: ", "");
        	for (User u: userList) {
        		if(u.getName().equals(name)) {
        			u.setIp(null);
        			u.setPKey(null);
        			u.setConnected(false);
        			break;
        		}
        	}
        	
        	if(connectedMap.size() != 0) {
        		Iterator<String> it = connectedMap.keySet().iterator();
    			while(it.hasNext()) {
    				String users = it.next();
    				if(users.contains(name)) {
    					it.remove();
    				}
    			}
        	}
        	session.write("ACCEPT LOGOUT: " + name);
        }
        
        if(str.contains("GET REQUESTS")) {
        	String name = str.replaceFirst("GET REQUESTS: ", "");
        	String msg = "REQUESTS LIST " + name + ": ";
        	for(String connect: connectedMap.keySet()) {
        		if(connect.contains(name)) {
        			String[] names = connect.split("-");
        			if(names[0].equals(name)) msg += "--" + names[1];
        			else msg += "--" + names[0];
        		}        		
        	}
        	String finalMsg = msg.replaceFirst("--", "");
        	if(finalMsg == msg) finalMsg += "NULL";
        	session.write(finalMsg);
        }
        
        if(str.contains("GET FRIENDS")) {
        	String name = str.replaceFirst("GET FRIENDS: ", "");
        	for(User u: userList) {
        		if(u.getName().equals(name)) {
        			String msg = "FRIENDS LIST " + name + ": ";
        			for(User a: u.getFriendsList()) {
        				msg += "--" + a.getName();
        			}
        			String finalMsg = msg.replaceFirst("--", "");
                	if(finalMsg == msg) finalMsg += "NULL";
                	session.write(finalMsg);
        			break;
        		}
        	}
        }
        
//        if(str.contains("DISCONNECT")) {
//        	System.out.println(str);
//        	str = str.replaceAll("DISCONNECT ", "");
//        	String[] names = str.split("-", 2);
//        	
//        	if(connectedMap.size() != 0) {
//        		Iterator<String> it = connectedMap.keySet().iterator();
//    			while(it.hasNext()) {
//    				String users = it.next();
//    				if(checkExistConnection(users, names[0].trim(), names[1].trim())) {
//    					it.remove();
//    					break;
//    				}
//    			}
//        	}
//        	
//        }
        
        if(str.contains("CONNECT")) {     	
        	str = str.replaceFirst("CONNECT: ", "");
        	String[] info = str.split("--", 2);
        	String serverUser = info[0].trim();
        	String clientUser = info[1].trim();
        	String clientIp = null;
        	String clientPubKey = null;
        	String port = null;
        	boolean accepted = true;
        	
        	for(User u: userList) {
        		if(u.getName().equals(clientUser)) {
        			if(u.isConnected() == true) {
        				if(connectedMap.size() != 0) {
        					Iterator<Map.Entry<String, String>> it = connectedMap.entrySet().iterator();
        					while(it.hasNext()) {
        						Map.Entry<String, String> item = it.next();
        						String users = item.getKey();
        						if(checkExistConnection(users, serverUser, clientUser)) {
        							port = item.getValue();
        							String[] names = users.split("-");
        							serverUser = names[0];
        							clientUser = names[1];
        							accepted = true;
        							break;
        						}
        					}
        				}	
        			} else {
        				accepted = false;
        			}
        			clientIp = u.getIp();
        			clientPubKey = u.getPKey();
//        			System.out.println(clientIp);
        			break;
        		}
        	}
        	
        	if(accepted) {
				if(port == null) {
					port = (int)Math.floor(Math.random()*(65535-1024+1)+1024) + "";
					while(connectedMap.containsValue(port + "") && port.equals("9123")) {
						// (int)Math.floor(Math.random()*(max-min+1)+min);
						port = (int)Math.floor(Math.random()*(65535-1024+1)+1024) + "";
					}
					connectedMap.put(serverUser + "-" + clientUser, port);
				}
			
				session.write("ACCEPT CONNECT: " + serverUser + "--" + clientUser + "--" + clientIp + "--" + port + "--" + clientPubKey);
					
//				String serverIp = null;
//				String serverPubKey = null;
//				for(User a: userList) {
//					if(a.getName().equals(serverUser)) {
//						serverIp = a.getIp();
//						serverPubKey = a.getPKey();
////						System.out.println(serverIp);
//						break;
//					}
//				}
//				
//				for(IoSession sess: acceptor.getManagedSessions().values()) {
//					InetSocketAddress socketAddress = (InetSocketAddress) sess.getRemoteAddress();
//			        String inetAddress = socketAddress.getHostName();
//			        inetAddress = inetAddress.replaceFirst("%.*", "");
////			        System.out.println(inetAddress);
//			        if(inetAddress.equals(clientIp)) {
//			          	sess.write("ACCEPT CONNECT " + serverUser + "-" + clientUser + "-" + serverIp + "-" + port + "-" + serverPubKey);
//			           	break;
//			        }
//				}
			} else {
        		session.write("REJECT CONNECT: " + serverUser + "--" + clientUser);
        	}
        }
        
    }
    
    @Override
    public void sessionIdle( IoSession session, IdleStatus status ) throws Exception
    {
    	InetSocketAddress socketAddress = (InetSocketAddress) session.getRemoteAddress();
        String inetAddress = socketAddress.getHostName();
        inetAddress = inetAddress.replaceFirst("%.*", "");
        System.out.println( "IDLE " + inetAddress);
//        if(pKey != null) {
//        	WriteFuture writeFuture = session.write(pKey);
////        	writeFuture = writeFuture.await();
//        	System.out.println("Message sent? " + writeFuture.isWritten());
//    	}
    }
       
    @Override 
    public void sessionCreated(IoSession session) throws Exception 
    {
//    	System.out.println("Get new session: " + session);
    	super.sessionCreated(session);
    }
    
    private boolean checkExistConnection(String users, String serverUser, String clientUser) {
    	return users.equals(serverUser + "-" + clientUser) || users.equals(clientUser + "-" + serverUser);
    }
}
