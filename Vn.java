import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.xml.crypto.Data;

import java.io.*;


public  class Vn{
    static InetAddress[] ninet=new InetAddress[26];
    static int[] nport=new int[26];
    static int[] ncost=new int[26];
    static int[][] graph=new int[26][26];
    static int[] seq=new int[26];
    static int myIdx;
    static InetAddress inetIp;
    static int udpPort;
    static DatagramChannel udpChannel;

    public  static void main(String []a) throws Exception
    {

        inetIp=InetAddress.getByName(a[1]); 
        udpPort=Integer.parseInt(a[2]);
        
        udpChannel=DatagramChannel.open();
        udpChannel.configureBlocking(false);
        InetSocketAddress udpAddress=new InetSocketAddress(inetIp,udpPort);
        udpChannel.bind(udpAddress);


        // Socket s=new Socket(a[0],5001);
        // s.setReuseAddress(true);

        InetAddress onInetIp=InetAddress.getByName(a[0]);
        int onPort=5000;

        SocketChannel tcpChannel=SocketChannel.open();
        InetSocketAddress tcpAddress=new InetSocketAddress(onInetIp,onPort);
        tcpChannel.bind(tcpAddress);
        tcpChannel.configureBlocking(false);

        for(int i=0;i<26;i++)ncost[i]=-1;
        for(int i=0;i<26;i++)seq[i]=-1;
        for(int i=0;i<26;i++)for(int j=0;j<26;j++)graph[i][j]=-1;

        // DataOutputStream out=new DataOutputStream(s.getOutputStream());
        // DataInputStream in=new DataInputStream(s.getInputStream());

        // out.write(InetAddress.getByName(a[1]).getAddress());
        // out.writeInt(Integer.parseInt(a[2]));
        // out.flush();

        ByteBuffer reg=ByteBuffer.allocate(8);
        reg.put(inetIp.getAddress());
        reg.putInt(udpPort);
        reg.flip();
        tcpChannel.write(reg);

        Selector select=Selector.open();
        ByteBuffer udpBuffer=ByteBuffer.allocate(4096);
        ByteBuffer tcpBuffer=ByteBuffer.allocate(4096);
        
        SelectionKey tcpSelectionKey =tcpChannel.register(select, SelectionKey.OP_READ);
        SelectionKey udpSelectionKey=udpChannel.register(select, SelectionKey.OP_READ);



        while(true)
        {
            
            
            // int n=in.readInt();
            // String details="";
            // for(int i=0;i<n;i++)
            // {
            //         char id=(char)in.readByte();

            //         byte[] ip=new byte[4];
            //         in.readFully(ip);
            //         InetAddress inet=InetAddress.getByAddress(ip);
            //         ninet[id-'A']=inet;
            
            //         String ipAddress = inet.getHostAddress();

            //         int port=in.readInt();
            //         nport[id-'A']=port;

            //         int cost=in.readInt();
            //         ncost[id-'A']=cost;

            //         details+="ID: "+id+" IP: "+ipAddress+" Port: "+port+" Cost: "+cost+"\n";
            // }
            // System.out.println("Link Message");
            // System.out.print(details);
            // System.out.flush();
        }
    }
}