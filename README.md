# FTP-P2P

This project is proposed by my teacher for my 1st year in STRI/SSIR Master.

The main purpose of this project is to recreate our own version of FTP protocol with then a P2P added in.
This project is just a demonstration of the socket usage with java and it's purpose is strictly educative.

We recommand that you pull the github repo with the following command : 

```
# git clone --depth 1 https://github.com/k1nd0ne/FTP-P2P.git
```


# P2P - Latest Version Instructions 

To use the P2P V2 program, execute the following code with Linux/Mac Os terminal :

First, open one terminal and navigate to the github project directory.

```
# cd FTP-P2P/
```
Then, launch the install.sh script

```
# bash install.sh
```

After, you can launch the Server.

```
# bash Repository
```

Then the client in an other terminal.

```
# cd ./FTP-P2P
# bash Client
```

Type help to begin.
The files you download (and share !), from the other clients are stored into the ./src/loot directory.

```
Example : 
$>get image.jpg 
```
# FTP usage instructions

To use the FTP program, execute the following code with Linux/Mac Os terminal.


First, open one terminal and navigate to the github project directory.

```
# cd FTP-P2P/
```

Because it is juste an intermediate version you'll have to compile the files.

```
# javac ./src/FTP/*.java
# mkdir ./bin/FTP
# mv ./src/FTP/*.class ./bin/FTP
```


Then, launch the FTP server.

```
# java -cp ./bin FTP.MainServerFTP
```
Secondly launch the client in an other terminal and execute the following instructions : 

```
# java -cp ./bin FTP.MainClientFTP
```

A authentication process will start, you can add your creds into the FTP-P2P/src/passwd.txt file or with login=user, password=password

Then type help to begin.

The files you download from the server are stored into the ./src/loot directory.

# P2P - V1 - Instructions 

To use the P2P program, execute the following code with Linux/Mac Os terminal :


First, open one terminal and navigate to the github project directory.

```
# cd FTP-P2P/
```
Because it is juste an intermediate version you'll have to compile the files.

```
# javac ./src/P2P/*.java
# mkdir ./bin/P2P
# mv ./src/P2P/*.class ./bin/P2P
```

Then, launch the P2P Master repository server.

```
# java -cp ./bin P2P.MainMasterRepo
```
Secondly launch as many as Distributed repository as you want in new terminals.

```
# java -cp ./bin P2P.MainDistributedRepo
```

Indicate ./src/myhome to the server path prompt and ./src/myhome2 for a second server for example (in an other terminal).

Finally, launch the client an authentifiate with user:password creds.
```
# java -cp ./bin P2P.MainClientP2P 
```
Type help to begin.
The files you download from the server are stored into the ./src/loot directory.

```
Example : 
# user>get image.jpg 
```

