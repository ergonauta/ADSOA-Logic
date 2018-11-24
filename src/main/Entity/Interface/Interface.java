package main.Entity.Interface;

import main.Entity.Entity;
import main.Enum.EnumContentCode;
import main.Enum.EnumType;
import main.JSONMessage.InterfaceMessage;
import main.JSONMessage.JSONMessage;
import main.MessageHandler.InterfaceMessageHandler;
import main.Sockets.Linker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Interface {

    private Entity entity;
    private JSONMessage jsonMessage;

    private Interface(Integer portNumber) throws IOException {
        entity = new Entity();
        jsonMessage = new JSONMessage();

        initializeEntityValues(portNumber);
        initializeMessageHandler();

        sendOutput();
    }

    private void initializeEntityValues(Integer portNumber) throws IOException {
        Linker linker = createLinker(portNumber);
        this.entity.setLinker(linker);
        this.entity.setType(EnumType.INTERFACE);
        this.entity.generateFootprint();
    }

    private Linker createLinker(Integer portNumber) throws IOException {
        Socket socket = new Socket(getHostname(), portNumber);
        return new Linker(socket);
    }

    private String getHostname() {
        String hostName = "";

        try {
            hostName = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()).toString();
            hostName = hostName.replace("/","");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return hostName;
    }

    private void initializeMessageHandler() {
        InterfaceMessageHandler interfaceMessageHandler = new InterfaceMessageHandler(entity, this);
    }

    private void sendOutput() throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String fromUser;
        Linker linker = entity.getLinker();

        while(true) {
            fromUser = stdIn.readLine();
            if (fromUser != null) {
                InterfaceMessage interfaceMessage = createInterfaceMessage();
                String message = jsonMessage.createInterfaceMessage(interfaceMessage);
                linker.sendMessage(message);
            }
        }
    }

    private InterfaceMessage createInterfaceMessage() {
        InterfaceMessage interfaceMessage = new InterfaceMessage();
        interfaceMessage.setContentCode(EnumContentCode.SUM);
        interfaceMessage.setOrigin(EnumType.INTERFACE);
        interfaceMessage.setOriginFootprint(entity.getFootprint());
        interfaceMessage.setFirstValue(1);
        interfaceMessage.setSecondValue(1);
        return interfaceMessage;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter a port number: ");
        String portNumber = stdIn.readLine();

        Interface client = new Interface(Integer.parseInt(portNumber));
    }
}
