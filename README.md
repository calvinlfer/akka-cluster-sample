# Akka Cluster Sample (using ConstructR with ZooKeeper)
_This is a demonstration of how to use Docker Swarm to bootstrap an Akka Cluster._

[![Build Status](https://travis-ci.org/calvinlfer/akka-cluster-sample.svg?branch=master)](https://travis-ci.org/calvinlfer/akka-cluster-sample)

## Problem statement
The process of discovering seed nodes and joining an existing cluster is a perilous journey especially in cloud 
environments due to the ephemeral nature of instances. You can get into situations where you form two or more clusters
if you incorrectly detect that no seed nodes are up causing massive problems for your data. Thankfully, service discovery 
mechanisms like Consul, etcd and ZooKeeper are there to help find out if existing seed nodes exist and ensure that a 
single cluster forms correctly. However, now service orchestration mechanisms are becoming increasingly intelligent and
allow us to utilize their native features (e.g. Docker Swarm and Kubernetes) to avoid using a separate coordination 
service in order to discover seed nodes.

## What does this project do?
This project utilizes Docker Swarm services, overlay networks and stacks in order to deploy a highly-available and 
resilient Akka Cluster. 

## Running the cluster on Swarm
Simply hop onto any manager node in the Swarm and copy the `docker-compose.yaml` to that machine. Then create a stack
```bash
docker stack deploy -c docker-compose.yaml <<your-stack-name>>
```

This will create the necessary services and overlay networks on the Swarm to run the application

## Running locally using Docker Compose
The nice part about using Docker Swarm is that it plays well with Docker Compose on your local machine. You can run all
the containers locally using
```bash
docker-compose up 
```

## Under the hood
We make use of 3 services:
- application-seed-1: the first seed node with a replication of 1
- application-seed-2: the second seed node with a replication of 1
- application: the application that relies on the above seed nodes for cluster bootstrapping with a replication of N 
  where N is odd

We make use of 2 seed nodes because in case one of the seed nodes go down, it can rely on the other seed node to re-join
the already existing cluster. If we used one seed node, we would not be able to re-join the cluster as the Session ID 
would be different.

This architecture relies on the fact that all seed nodes cannot go down. If this were to happen (all seed nodes do go 
down but the normal nodes remain in the formed cluster) then the seed nodes would not be able to join the already 
formed cluster then when the seed nodes came back up, they would begin to form a new cluster thereby causing split brain.
As you can tell, the more seed node services you have, the higher the guarantee of being able to lose seed nodes and 
having them re-join the cluster without causing split brain to occur. Here we use 2 for illustration purposes but 3 or 5
would be a better number. The more of these services you have, the bigger the docker-compose.yaml becomes.

### Resiliency
Resiliency testing has been done on a 3 node Docker Swarm cluster in AWS. First, the stack was deployed using 
`docker stack deploy -c docker-compose.yaml test-stack`. You can start to identify containers by drilling down into
the stack using `docker stack ps test-stack` and finding the services that make up the stack. Once you find the 
services, you can use `docker service ps <service-name>` to find out which nodes are hosting the tasks of interest.
Once the seed node service tasks have been discovered (i.e. which node in the Swarm they are running on), we hop into 
those machines and do a `docker container ls` to find out the container ID. Then we `docker rm -f <container-id-of-seed>`
to forcefully terminate and remove them from the Akka cluster. Doing this will cause the Split Brain Resolver to take 
effect and begin removing them. In addition to this, the Docker Swarm orchestrator will realize there is a mismatch 
between our desired state and the running state and re-deploy another task to make up for the container we terminated.
Viewing the logs of that newly brought up container, we see that it successfully joins the Akka cluster using 
`docker container logs <container-id>`, you can add on `| grep Younger` to ensure that it joins the existing Akka 
cluster and that it is not the oldest node in the cluster for the Shard coordinator. This shows that the application can
sustain the loss of a single seed node and that it can heal automatically without intervention. You can freely kill any
of the tasks in the `application` service and they will also come back up and join the cluster using the tasks in the
seed services. 

**Note**: This project takes care of split brain resolution (keep-majority style) and bootstrapping a cluster. We use an 
open-source Split Brain Resolver from [akka-batteries](https://github.com/PaytmLabs/akka-batteries#role-based-split-brain-resolver).
