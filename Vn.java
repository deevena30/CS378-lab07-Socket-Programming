import java.util.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.io.*;
// usage is java Vn ONIP VNIP udpPort
class link
{
   int nid;
   int cost;
}

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
        int onPort=5001;

        SocketChannel tcpChannel=SocketChannel.open();
        InetSocketAddress tcpAddress=new InetSocketAddress(onInetIp,onPort);
        tcpChannel.connect(tcpAddress);
        //tcpChannel.bind(tcpAddress)

        tcpChannel.configureBlocking(false);
 
        for(int i=0;i<26;i++)ncost[i]=-1;
        for(int i=0;i<26;i++)seq[i]=-1;
        for(int i=0;i<26;i++)
          for(int j=0;j<26;j++)
        {
            if(i==j)graph[i][j]=0;
           else graph[i][j]=-1;
        }
        myIdx=-1;
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
        
        long next=System.currentTimeMillis()+15000;

        tcpChannel.register(select, SelectionKey.OP_READ);
        udpChannel.register(select, SelectionKey.OP_READ);

        while(true)
        {
            select.select(Math.max(1,next-System.currentTimeMillis()));
            Set<SelectionKey> keys=select.selectedKeys();
            Iterator<SelectionKey> it=keys.iterator();
            while(it.hasNext())
            { 
                SelectionKey key=it.next();
                it.remove(); // is this needed? 

                if(key.channel()==tcpChannel)
                {
                    int r=tcpChannel.read(tcpBuffer);
                    if(r==-1 || r==0) continue;

                    tcpBuffer.flip();
  
                    while(tcpBuffer.remaining()>=4)
                    {
                        int n=tcpBuffer.getInt(tcpBuffer.position());
                        int need=4+n*13;
                        if(tcpBuffer.remaining()<need)
                                break;

                        tcpBuffer.getInt();
                        for(int i=0;i<26;i++)ncost[i]=-1;

                        for(int i=0;i<n;i++)
                        {
                            int id=tcpBuffer.get()-'A';
                            byte[] ip=new byte[4];
                            tcpBuffer.get(ip);
                            ninet[id]=InetAddress.getByAddress(ip);
                            nport[id]=tcpBuffer.getInt();
                            ncost[id]=tcpBuffer.getInt();
                        }
                        if(myIdx==-1)
                        {
                            for(int i=0;i<26;i++)
                            {
                               if(ncost[i]==0)
                               {
                                myIdx=i; break;
                               }
                            }
                        }
                        originate();
                    }
                    tcpBuffer.compact();
                }
                else
                {
                    udpBuffer.clear();
                    if(udpChannel.receive(udpBuffer)==null)continue;
                    udpBuffer.flip();
                    byte[] data=new byte[udpBuffer.remaining()];
                    udpBuffer.get(data);
                    handleLSP(data);
                }
            }

            if(System.currentTimeMillis()>=next)
            {
                originate();
                printGraph();  
                next =System.currentTimeMillis()+15000;
            }
            
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

    static void originate() throws IOException
    {
        seq[myIdx]++;
        for(int i=0;i<26;i++)
        {
            if(i==myIdx)continue;
            graph[myIdx][i]=ncost[i];
            graph[i][myIdx]=ncost[i];
        }
        // originId-1 byte,srcSeq-4 bytes, count=# neighbours - 4 bytes,
        // for each neighbor
        //nbrId=j-> 1 byte
        // cost-4 bytes 
        
        int nNbrs=0;
        for(int i=0;i<26;i++)if(ncost[i]>0)nNbrs++;
        ByteBuffer buf=ByteBuffer.allocate(1+4+4+5*nNbrs);
        buf.put((byte)myIdx);
        buf.putInt(seq[myIdx]);
        buf.putInt(nNbrs);
        
        for(int i=0;i<26;i++)
        {
            if(ncost[i]<=0)continue;
            buf.put((byte)i);
            buf.putInt(ncost[i]);
        } 
        buf.flip();
        flood(buf);
    }
  
    static void handleLSP(byte[] data)throws IOException
    {
        ByteBuffer buf=ByteBuffer.wrap(data);
        /// decode
         int originId=buf.get();
         int originSeq=buf.getInt();
         int neighbor_count=buf.getInt();
         link links[]=new link[neighbor_count];
         for(int i=0;i<neighbor_count;i++)
         {
            link l= new link();
            l.nid=buf.get();
            l.cost=buf.getInt();
            links[i]=l;
         }
         // validate
         if(originId==myIdx)
            return;
         if(originSeq<=seq[originId])
            return;
         
         seq[originId]=originSeq;

         for(int i=0;i<26;i++)
         {
            if(i==originId)continue;
             graph[originId][i]=-1;
             graph[i][originId]=-1;
         }

         for(link l: links)
         {
            graph[originId][l.nid]=l.cost;
            graph[l.nid][originId]=l.cost;
         }
         flood(ByteBuffer.wrap(data));
    }
    static void flood(ByteBuffer buf)throws IOException
    {
        byte[] packet=new byte[buf.remaining()];
        buf.get(packet);
         for(int i=0;i<26;i++)
         {
            if(ncost[i]<=0)continue;
            InetSocketAddress addr=new InetSocketAddress(ninet[i],nport[i]);
            udpChannel.send(ByteBuffer.wrap(packet),addr);  
            // buf.rewind(); 
         }
    }

    static void printGraph()
    {
        for(int i=0;i<26;i++)
        {
            for(int j=i+1;j<26;j++)
            {
                System.out.print(graph[i][j]+" ");
            }
            System.out.println("\n");
        }
    }
}