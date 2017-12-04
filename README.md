# Akka Cluster Sample (using ConstructR with ZooKeeper)
_This is a demonstration of how to use ZooKeeper with the help of [ConstructR](https://github.com/hseeberger/constructr#constructr) 
to bootstrap an Akka Cluster._

[![Build Status](https://travis-ci.org/calvinlfer/akka-cluster-sample.svg?branch=master)](https://travis-ci.org/calvinlfer/akka-cluster-sample)

## Problem statement
The process of discovering seed nodes and joining an existing cluster is a perilous journey especially in cloud 
environments due to the ephemeral nature of instances. You can get into situations where you form two or more clusters
if you incorrectly detect that no seed nodes are up causing massive problems for your data. Thankfully, service discovery 
mechanisms like Consul, etcd and ZooKeeper are there to help find out if existing seed nodes exist and ensure that a 
single cluster forms correctly.

## What does this project do?
This project uses [ConstructR](https://github.com/hseeberger/constructr#constructr) backed by [ConstructR-ZooKeeper](https://github.com/typesafehub/constructr-zookeeper)
in order to bootstrap the cluster i.e. discover existing seed nodes if they exist and ensure that new nodes join the
existing cluster. Once the application is ready, Greeter Actors on all nodes will begin messaging Cluster Sharded 
Member Actors so you can see some activity.

## Running the cluster
In order to run the cluster, we make use of [Docker Compose](https://docs.docker.com/compose) and the 
[SBT Native Packager](https://github.com/sbt/sbt-native-packager). First, create a Docker image in your local Docker 
registry using `sbt docker:publishLocal` or using `./scripts/docker-image.sh`. Once this has been complete, spin up the 
set of services (ZooKeeper and 3 clustered application nodes) that represent this application via `docker-compose up`.
When you are satisfied, use `docker-compose down -v` to bring down the application.

**Note**: If you want to run multiple clustered applications directly on your host machine, you will need to [change the default 
port Akka Cluster uses for gossip](http://doc.akka.io/docs/akka/2.5.2/java/cluster-usage.html#a-simple-cluster-example) 
since all applications use the default port.

### Under the hood
Using Docker Compose means that a Docker network is created and containers are automatically placed into this network. 
This means that the containers are free to communicate with each other. We make the Akka Cluster applications talk to 
ZooKeeper by using the Container DNS mechanism offered by Docker. The Akka Cluster applications will use ZooKeeper 
(via ConstructR) to discover the seed nodes and join the same cluster and will communicate with each other. 


**Note**: This does not take care of Split Brain Resolution, this takes care of bootstrapping a cluster. If you are 
looking for an open-source Split Brain Resolver then see [akka-batteries](https://github.com/PaytmLabs/akka-batteries#role-based-split-brain-resolver) 
for an excellent example and also refer to Niko Will's [talk](https://www.youtube.com/watch?v=ke9r0yQnaqA).

## Where to go next
- See a more [detailed example](https://github.com/LoyaltyOne/theatre-booking-akka-example)
- A detailed [blog post](https://medium.com/@ukayani/deploying-clustered-akka-applications-on-amazon-ecs-fbcca762a44c) of the entire process
