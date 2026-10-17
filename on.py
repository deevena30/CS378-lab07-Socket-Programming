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
    neighbors=[j for j in range(n_nodes) if cost[node][j]!=-1]
    msg=struct.pack("!I",len(neighbors))
    for j in neighbors:
        msg+=struct.pack("!c4sII",bytes([65+j]),vnIp[j],vnPorts[j],cost[node][j])
    return msg


server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind(("192.168.1.102", 5001))
server.listen(26)

vnIp=[]
vnPorts=[]
conns=[]

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
    ip_addr = from_vn[:4]
    port = struct.unpack("!I", from_vn[4:8])[0]
    vnIp.append(ip_addr) # raw bytes
    vnPorts.append(port)
    print("connected  vn",chr(65+i),addr, port)

for i  in range(n_nodes):
    conns[i].send(linkMsgToVn(i)) 

last=os.path.getmtime(config_file)
while True:
    m=os.path.getmtime(config_file)
    if m!=last:
        last=m
        time.sleep(.5)
        parse_config(config_file)
        print("config file modified")
        for i in range(n_nodes):
            conns[i].send(linkMsgToVn(i))
    