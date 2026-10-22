import socket
import sys
import struct
import os
import time

config_file = sys.argv[1]

cost=[[-1 for _ in range(26)] for _ in range(26)]
for i in range(26):
    cost[i][i]=0

def parse_config(config_file):
    with open(config_file, 'r') as fi:
        cnt=0
        for line in fi:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            for idx,c in enumerate(map(int,line.split())):
                cost[cnt][cnt+idx+1]=c
                cost[cnt+idx+1][cnt]=c
            cnt=cnt+1
    return cnt+1

n_nodes=parse_config(config_file)

def linkMsgToVn(node):
    if(node>=n_nodes):
        msg = struct.pack("!I", 1)
        msg += struct.pack("!c4sII", bytes([65+node]), islands_conn[node-n_nodes][0], islands_conn[node-n_nodes][1], 0)
        return msg
    neighbors=[j for j in range(n_nodes) if cost[node][j]!=-1]
    msg=struct.pack("!I",len(neighbors))
    for j in neighbors:
        msg+=struct.pack("!c4sII",bytes([65+j]),vnIp[j],vnPorts[j],cost[node][j])
    return msg


server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(("172.20.15.244", 5000))
server.listen(26)

vnIp=[]
vnPorts=[]
conns=[]
islands_conn = []

def recv_exact(conn, n):
    buf = b""
    while len(buf) < n:
        chunk = conn.recv(n - len(buf))
        if not chunk:
            raise ConnectionError("socket closed")
        buf += chunk
    return buf

for i in range(n_nodes):
    conn, addr = server.accept()
    conns.append(conn)
    from_vn = recv_exact(conn, 8) 
    #from_vn = conn.recv(8)
    ip_addr = from_vn[:4]
    port = struct.unpack("!I", from_vn[4:8])[0]
    vnIp.append(ip_addr) # raw bytes
    vnPorts.append(port)
    print("connected  vn",chr(65+i),addr, port)

for i  in range(n_nodes):
    conns[i].send(linkMsgToVn(i)) 

server.setblocking(False)
last=os.path.getmtime(config_file)
while True:
    while True:
       try:
           conn,addr=server.accept()
       except BlockingIOError:
           break
       from_island=recv_exact(conn,8)
       node=n_nodes+len(islands_conn)
       if(node>=26):
           conn.close()
           continue
       islands_conn.append((from_island[:4],struct.unpack("!I",from_island[4:8])[0],conn))
       conn.send(linkMsgToVn(node))
       print("Connected island",chr(65+node),addr)

    time.sleep(.5)
    m=os.path.getmtime(config_file)
    if m!=last:
        last=m
        parse_config(config_file)
        print("config file modified")
        for i in range(n_nodes):
            conns[i].send(linkMsgToVn(i))
    