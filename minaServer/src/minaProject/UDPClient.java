package minaProject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class UDPClient {
	private static final int PORT = 8000;
//	private static final String HOSTNAME = "fe80::54f9:2dc:65f3:15e4";
	private static final String HOSTNAME = "fe80::5cba:fe5d:65cf:18d9";
	private int port;
	
	private String name;
	
	public UDPClient(String name, int port) {
		this.name = name;
		this.port = port;
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
	
	private void startSender(String msg, String guestIp, int port) {
		try {
			InetSocketAddress socketaddr = getSocketAddress(guestIp, port);
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					byte[] data = msg.getBytes();
					try {
						DatagramSocket ds = new DatagramSocket();	
//						ds.setBroadcast(true);
						DatagramPacket packet = new DatagramPacket(data, data.length, socketaddr);
						ds.send(packet);
						System.out.println("Client " + name + " send message: " + msg);
						ds.close();
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
	
	private void startServer(String guestIp, int port) {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
//					InetSocketAddress socketaddr = getSocketAddress(guestIp, port);
					DatagramSocket ds = new DatagramSocket(port);
//					ds.connect(socketaddr);
//					while(!ds.isConnected()) {
//						ds.connect(socketaddr);
//					}
//					System.out.println("Start server from user " + name);
					byte[] data = new byte[65535];
					String tmp;
					while(true) {
						DatagramPacket packet = new DatagramPacket(data, data.length);
						ds.receive(packet);
						tmp = new String(data).trim();
						System.out.println(name + " received message: " + tmp);
						if(tmp.equals("EXIT"))
							break;
						data = new byte[65535]; 
					}
					ds.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	public void run() {
		startServer(HOSTNAME, port);
//		try {
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		startSender("Hello " + name, HOSTNAME, port);
		startSender(name, "fe80:0:0:0:72bb:e9ff:fecc:8f6d", port);
		startSender("Bye " + name, "fe80:0:0:0:72bb:e9ff:fecc:8f6d", port);
//		try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
	
	public static void main(String[] args) {
		for(int i=1; i<=2; i++) {
			int index = i;
			Thread thread = new Thread(new Runnable() {
			  @Override
			  public void run() {
				  int port = 8000 + index;
				  UDPClient client = new UDPClient("client" + index, port);
				  client.run();
			  }
			});
			thread.start();
		}
//		UDPClient client = new UDPClient("test2");
//		client.run();
	}
}
