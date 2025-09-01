# Computer--Networks

# Java Client-Server File Transfer System

A simple **Computer Networks project** implemented in **Java** that demonstrates **file transfer between client and server** using **socket programming** and **multithreading**.

---

## Features
- **Server** that can handle multiple clients concurrently using threads  
- **Client** that connects to the server and transfers files efficiently  
- Demonstrates **real-time communication** over **TCP/IP**  
- Modular code for easy understanding and extension  


## Project Structure
├── FileTransferServer.java # Server-side implementation
├── ThreadedServer.java # Handles multiple clients with multithreading
└── FileTransferClient.java # Client-side implementation


## How It Works
1. Start the server:
   
   javac FileTransferServer.java ThreadedServer.java
   java FileTransferServer
Run the client (in a different terminal / system):

javac FileTransferClient.java
java FileTransferClient
The client connects to the server and can transfer files.

## Key Learnings:

Socket programming in Java
Client-Server architecture
Handling multiple clients using multithreading
Bridging theoretical TCP/IP concepts with practical implementation

## Future Improvements:

Add authentication for clients
Support for multiple file formats & larger files
GUI-based client
