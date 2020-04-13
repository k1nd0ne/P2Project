# Run your client into a docker to test multiple clients

This docker file will create an image that fireup a client in a container.

The goal is to create a virtual client in the same host network to download files.

# Requirements  

You need to install docker : https://docs.docker.com/install/

# Installation 

Navigate to the root project directory then build the docker image. 

```
# cd FTP-P2P/
# docker build ./ -t p2pjava:latest
```

Then you can enter into your image container an fire up a bash
```
# docker run --network host -it p2pjava:latest bash
# cd FTP-P2P; bash Client
```

You'll need to indicate the ip of the host that runs the Repository.


Type help to begin.
The files you download (and share !), from the other clients are stored into the ./src/loot directory.

```
Example : 
$>get image.jpg 
```

