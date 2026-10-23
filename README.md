### 1. Run `on.py`

Execute the following command to start the ON server:
###NOTE: Before running the program, make sure to update the IP address in the bind() function to match the IP address of the machine running the program.

```bash
python3 on.py oracle-node-config.txt
```

### 2. Run `Vn.java`

**Step 1: Compile the Java program**

```bash
javac Vn.java
```

**Step 2: Run the program**

```bash
java Vn <ON-IP-Address> <VN-IP-Address> <UDPport>
```

**Example:**

```bash
java Vn 127.0.0.1 127.0.0.2 5000
```
