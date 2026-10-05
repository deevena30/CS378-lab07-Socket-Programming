import socket 
fd=socket.socket(socket.AF_INET, socket.SOCK_STREAM)
fd.connect(('127.0.0.1',10000))
fd.send("I am client\n".encode())
from_server=fd.recv(4096).decode()
print(from_server)
fd.close()
