# Run your client into a docker to test multiple clients

This docker file will create an image that fireup a client in a container.

The goal is to create a virtual client in the same host network to download files.

THIS PART IS IN DEVELOPPEMENT.
# Requirements  

You need to install docker : https://docs.docker.com/install/

# Installation 

Navigate to the root project directory then build the docker image. 

```
# cd P2Project/
# docker build ./ -t p2pjava:latest
```

Then you can enter into your image container an fire up a bash
```
# docker run -e DISPLAY=$DISPLAY --network host -it p2pjava:latest bash
# cd P2Project; bash Client
```

You'll need to indicate the ip of the host that runs the Repository.

