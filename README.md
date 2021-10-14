# Scalable Server Design
Using Thread Pools & Micro-Batching to Manage and Load-Balance Active Network Connections

(CS455 - Introduction to Distributed Systems)


## Functionality
- Managing incoming network connections
- Receiving data over the network connections
- Organizing data into batches to improve performance
- Sending data over the links

### How-to-run

If you are using the scripts I have created (`Makefile` and `start-node.sh`), please the follow these steps.
1. In the `Makefile`, under `run-server`, change the values that follow `cs455.scaling.server.Server` as you wish.
2. Run `make run-server` (I have used port `5600`)
3. Run `sh start-nodes.sh <server-host>` (example: `sh start-nodes albany`)

