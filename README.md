# Java NIO Pub/Sub System

A client-server application built with Java NIO (Non-blocking I/O) demonstrating a simple Publish-Subscribe messaging pattern.

## Components

* **`Server.java`**: The central multiplexing server using `Selector`. It handles non-blocking connections, manages topic subscriptions, and routes messages to subscribed clients.
* **`Client.java`**: A client featuring a basic Swing GUI to `Subscribe` and `Unsubscribe` from topics, while also allowing command-line interaction.
* **`Publisher.java`**: A basic terminal-based client implementation for connecting and interacting with the server.

## Prerequisites

* Java Development Kit (JDK) 8 or higher.

## Compilation

Compile all Java files in the directory:
```bash
javac Server.java Client.java Publisher.java
```

## Running the Application

1. **Start the Server:**
   ```bash
   java Server
   ```
   The server will start listening on `localhost:12345`.

2. **Start a Client:**
   Open a new terminal and run:
   ```bash
   java Client
   ```
   A GUI will appear allowing you to type a topic and subscribe/unsubscribe. You can also type commands directly into the terminal.

3. **Start a Publisher (Optional):**
   Open another terminal and run:
   ```bash
   java Publisher
   ```

## Available Commands

You can interact with the server via the terminal using the following commands:
* `New client` - Registers the client session on the server.
* `Show topics` - Lists all available topics.
* `Subscribe <topic>` - Subscribes the current client to a specific topic (e.g., `Subscribe Sport`).
* `Unsubscribe <topic>` - Unsubscribes from a topic.
* `Add topic <topic>` - Adds a new topic to the server's list.
* `Remove topic <topic>` - Removes a topic.
* `News to <topic> <message>` - Broadcasts a message to all clients subscribed to `<topic>`.
* `Bye` - Disconnects from the server.
