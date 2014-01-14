package Main;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class Chat extends ReceiverAdapter {
    JChannel channel;
    
    //RpcDispatcher disp;
    
    ArrayList<String> strLines = new ArrayList<String>();
 
    @Override
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    @Override
    public void receive(Message msg) {
        
    	String line="[" + msg.getSrc() + "]: " + msg.getObject();
        System.out.println(line);
        
        strLines.add( (String) msg.getObject() );
        
    }

    @Override
    public void suspect(Address suspected_mbr) {
        System.out.println("Suspected(" + suspected_mbr + ')');
    }
    
    @Override
    public void getState(OutputStream ostream) throws Exception {
        Util.objectToStream(strLines, new DataOutputStream(ostream));
    }

    @SuppressWarnings("unchecked")
    @Override
	public void setState(InputStream istream) throws Exception {
        
    	strLines = (ArrayList<String>) Util.objectFromStream(new DataInputStream(istream) );

    	System.out.println( "Old Messages: " + Integer.toString( strLines.size() ) );
    	
    	for ( String strLine : strLines ) {
    		
    		System.out.println( strLine );
    		
    	}
    	
    }

    /** Method called from other app, injecting channel */
    public void start(JChannel ch) throws Exception {
        channel=ch;
        channel.setReceiver(this);
        channel.connect("ChatCluster");
        eventLoop();
        channel.close();
    }

    private void start(String props, String name) throws Exception {
        channel=new JChannel(props);
        if(name != null)
            channel.setName(name);
        channel.setReceiver(this);
        //disp=new RpcDispatcher(channel, this, this, this);
        channel.connect("ChatCluster");
        channel.getState(null, 0);
        eventLoop();
        channel.close();
    }

    private void eventLoop() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                Message msg=new Message(null, null, line);
                channel.send(msg);
            }
            catch(Exception e) {
            }
        }
    }


    public static void main(String[] args) throws Exception {
        String props="udp.xml";
        String name=null;

        for(int i=0; i < args.length; i++) {
            if(args[i].equals("-props")) {
                props=args[++i];
                continue;
            }
            if(args[i].equals("-name")) {
                name=args[++i];
                continue;
            }
            help();
            return;
        }

        new Chat().start(props, name);
    }

    protected static void help() {
        
    	System.out.println("ChatDemo [-props XML config]");
    	
    }
}