import socket
import sys
import struct

config_file = sys.argv[1]

costs = []
with open(config_file, 'r') as fi:
    # lines = [line for line in fi if line.strip() and not line.strip().startswith("#")]
    for line in fi:
        line = line.strip()
        if not line or line.startswith("#"):
            continue

        cost = list(map(int, line.split()))
        costs.append(cost)

n_nodes = len(costs) + 1

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(("10.53.0.64", 5000))
server.listen()

cnt = 0
while True:
    conn, addr = server.accept()
    cnt = cnt + 1
    if cnt > n_nodes:
        break
    print("connected! vn", addr)
    from_vn = conn.recv(6)
    ip_addr = from_vn[:4]
    port = struct.unpack("!H", from_vn[4:6])
    print(socket.inet_ntoa(ip_addr), port)
    
