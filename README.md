# Auction
This program is only part of the Auction project and it consists of the Server side which contains the multi-threaded server that runs the Auction and where clients connect.
*[CLIENT](https://github.com/Nugg7/TCPClient.git) - to get Client side.*
# Dependencies
- JSON-simple ver. 1.1.1
# Requirements
- JDK 8+
- IDE to run program (like Intellij)
# Installation
## Linux/Mac/Windows (git bash)
```java
git clone https://github.com/Nugg7/TCPServer.git
```
## Windows (without git bash)
- Download the zip
- extract
# Usage
Open the IDE and open the project downloaded/cloned, change the port in the `Server.java` class in `src` (if needed - default is : 1234), then run the `Server.java` file. Once ran the project just wait for the clients.
# Functionalities
- Accepts connections from the port selected (without considering firewall)
- Once auction is started other attempts at connecting to server will be refused
- Broadcasts messages sent by clients
- Distinguishes chat messages from other types of messages such as bids and auction operations
- Gets the products sent by admin and prepares them to be sent to clients
- Saves the Winners on the JSON file for every product (Username, UUID and the bid)
- Controls every operation of the Auction - the client's task is only to interpret the messages sent by the server

> [!warning]
> the current project works fine with Linux based operating systems but on windows operating systems has quite some bugs
