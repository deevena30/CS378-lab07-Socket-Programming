import java.net.*;
import java.io.*;

public  class Vn{
    public  static void main(String []a) throws Exception
    {
        Socket s=new Socket(a[0],5000);
        DataOutputStream out=new DataOutputStream(s.getOutputStream());
        DataInputStream in=new DataInputStream(s.getInputStream());
        out.write(InetAddress.getByName(a[1]).getAddress());
        out.writeShort(Integer.parseInt(a[2]));

        while(true)
        {
            int n=in.readInt();
            String details="";
            for(int i=0;i<n;i++)
            {
                    char id=(char)in.readByte();
                    byte[] ip=new byte[4];
                    in.readFully(ip);
                    String ipAddress = InetAddress.getByAddress(ip).getHostAddress();
                    int port=in.readShort();
                    int cost=in.readInt(); 
                    details+="ID: "+id+" IP: "+ipAddress+" Port: "+port+" Cost: "+cost+"\n";
            }
            System.out.println("Link Message");
            System.out.print(details);
        }
    }
}