import socket
import sys

config_file = sys.argv[1]

with open(config_file, 'r') as fi:
    lines = [line for line in fi if line.strip() and not line.strip().startswith("#")]
    for line in fi:
        line = line.strip()
        if not line or line.startswith("#"):
            continue:

n_nodes = len(lines)

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(("127.0.0.1", 5000))
server.listen()


while True:
    conn, addr = server.accept()
    print("connected! vn", addr)

