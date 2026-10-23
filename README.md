### 1. Run `on.py`

Execute the following command to start the ON server:

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
