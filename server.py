import socket
fd=socket.socket(socket.AF_INET,socket.SOCK_STREAM)
fd.bind(('127.0.0.1',10000))
fd.listen(1)
conn, addr=fd.accept()
data=conn.recv(4096).decode()
conn.send("I am server\n".encode())
print(data)
fd.close()