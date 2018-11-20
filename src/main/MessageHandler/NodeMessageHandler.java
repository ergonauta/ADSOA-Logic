package main.MessageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import main.Entity.Entity;
import main.Entity.Node.Node;
import main.Enum.EnumContentCode;
import main.Enum.EnumType;
import main.JSONMessage.JSONMessage;
import main.Sockets.Linker;

import java.io.IOException;
import java.net.Socket;

public class NodeMessageHandler extends AbstractMessageHandler {

    private Node node;
    private String message;
    private String response;
    private JSONMessage jsonMessage;

    public NodeMessageHandler(Socket socket, Node node) throws JsonProcessingException {
        this.node = node;
        entity = new Entity();
        jsonMessage = new JSONMessage();

        initializeEntityValues(socket);
        sendFirstMessage();
        this.start();
    }

    private void initializeEntityValues(Socket socket) {
        Linker linker = new Linker(socket);
        entity.setLinker(linker);
    }

    private void sendFirstMessage() throws JsonProcessingException {
        Linker linker = entity.getLinker();
        message = node.getConnectNodeMessage();
        linker.sendMessage(message);
    }

    public boolean processMessage(String message) throws IOException {
        Integer contentCode = jsonMessage.getContentCode(message);
        Integer type;
        String footprint;

        System.out.println("Processing message: " + message);
        switch(contentCode) {
            case EnumContentCode.INITIAL_NODE_CONF:
                System.out.println("Entering INITIAL_NODE_CONF");
                Integer portNumber = jsonMessage.getPortNumber(message);
                type = jsonMessage.getType(message);
                footprint = jsonMessage.getFootprint(message);
                entity.setType(type);
                entity.setFootprint(footprint);
                System.out.println("Adding incoming entity");
                printEntitySettings();
                node.addIncomingLinker(entity);
                return true;
            case EnumContentCode.INITIAL_INTERFACE_CONF:
                System.out.println("Adding interface from: " + message);
                type = jsonMessage.getType(message);
                entity.setType(type);
                return true;
            case EnumContentCode.SUM:
            case EnumContentCode.SUBSTRACTION:
            case EnumContentCode.MULTIPLICATION:
            case EnumContentCode.DIVISION:
                Integer origin = jsonMessage.getOrigin(message);
                System.out.println("Entity before: " + message);
                message = jsonMessage.updateOriginMessage(EnumType.NODE, message);
                if (origin == EnumType.NODE) {
                    System.out.println("Node: " + message);
                    node.sendMessageToNonNodeLinkers(message);
                } else {
                    System.out.println("Entity after: " + message);
                    node.sendMessageToConnectedLinkers(message);
                }
                break;
        }
        return false;
    }

    private void printEntitySettings() {
        EnumType enumType = new EnumType();
        String type = enumType.toString(entity.getType());

        System.out.println("Entity settings: ");
        System.out.println("Entity type: " + type);
        System.out.println("Entity footprint: " + entity.getFootprint());
    }

}
