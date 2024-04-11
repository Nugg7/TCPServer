# Auction from: Steven Guevarra
This program is only part of the Auction project and it consists of the Server side which contains the multi-threaded server that runs the Auction and where clients connect.
*[CLIENT](https://github.com/Nugg7/TCPClient.git) - to get Client side.*
# Dependencies (from maven)
- JSON-simple ver. 1.1.1 (`com.googlecode.json-simple:json-simple:1.1.1`)
# Requirements
- JDK 22
# Installation
## Linux/Mac/Windows
install the release or
```java
git clone https://github.com/Nugg7/TCPServer.git
```
# Usage
After cloning the repo, just double click on the jar file
or if that doesn't work open terminal or cmd in folder and use:
```java
java -jar TCPServer.jar
```
# Functionalities- Accepts connections from the port selected (without considering firewall)
- Once auction is started other attempts at connecting to server will be refused
- Broadcasts messages sent by clients
- Distinguishes chat messages from other types of messages such as bids and auction operations
- Gets the products sent by admin and prepares them to be sent to clients
- Saves the Winners on the JSON file for every product (Username, UUID and the bid)
- Controls every operation of the Auction - the client's task is only to interpret the messages sent by the server

> [!warning]
> the current project executable (JAR) works fine with windows but breaks on Linux and Mac based OS
