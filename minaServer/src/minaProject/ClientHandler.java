package minaProject;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * @author giftsam
 */
public class ClientHandler extends IoHandlerAdapter {
	private final Logger logger = Logger.getLogger(getClass());
    
	private boolean server = false;
    private boolean accepted = false;
    private ArrayList<String> connectedList = new ArrayList<String>();
    private String username = null;
    private String guestIP = null;
    private String guestname = null;
    private String guestPubKey = null;
    private String port = null;

    public ClientHandler(String username) {
        this.username = username;
    }

    public String getGuestIP() {
        return this.guestIP;
    }
    
    public String getGuestname() {
        return this.guestname;
    }
    
    public String getGuestPubKey() {
        return this.guestPubKey;
    }

    public String getPort() {
        return this.port;
    }

    public boolean isAccepted() {
        return this.accepted;
    }
    
    public boolean isServer() {
        return this.server;
    }

    public void stopLogger() {
        Logger.shutdown();
    }

    public String[] getConnectedList() {
        String str[] = new String[connectedList.size()];
        for(int i=0; i<connectedList.size(); i++) {
            str[i] = connectedList.get(i);
        }
        return str;
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        String str = message.toString().trim();
//        logger.info(str);
        // List msg:
        // ACCEPT LOGIN ... / REJECT LOGIN ...
        // ACCEPT LOGOUT ...
        // FRIENDS LIST ...
        // ACCEPT CONNECT ...
        if(str.contains("REJECT")) {
            this.accepted = false;
        }

        if(str.contains("ACCEPT LOGIN")) {
            this.accepted = true;
        }

        if(str.contains("FRIENDS LIST")) {
        	str = str.replaceFirst("FRIENDS LIST " + username + ": ", "");
        	connectedList.clear();
            String[] list = str.split("--", 2);
            for (String s: list) {
                connectedList.add(s);
            }
        }

        if(str.contains("ACCEPT CONNECT")) {
            this.accepted = true;
            str = str.replaceFirst("ACCEPT CONNECT: ", "");
            String[] list = str.split("--");
            if(list[0].trim().equals(username)) {
                guestname = list[1].trim();
                server = true;
            } else {
                guestname = list[0].trim();
                server = false;
            }
            port = list[3].trim();
            guestIP = list[2].trim();
            guestPubKey = list[4].trim();
        }
        session.close();

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.close();
    }
}
