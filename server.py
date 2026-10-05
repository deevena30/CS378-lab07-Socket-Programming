import socket

fd = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
fd.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

fd.bind(('127.0.0.1', 10000))
fd.listen(1)

conn, addr = fd.accept()

while True:
    data = conn.recv(4096).decode()
    if not data:
        break
    print("Client:", data)
    conn.send(data.encode())

conn.close()
fd.close()