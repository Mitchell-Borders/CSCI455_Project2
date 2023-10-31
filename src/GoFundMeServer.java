
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Models.FundraisingEvent;
import Models.FundraisingEventMessage;

public class GoFundMeServer {
    public static ArrayList<FundraisingEvent> allEvents = new ArrayList<FundraisingEvent>();
    private static final int PORT = 6789;

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("The GoFundMe UDP server is on.");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                serverSocket.receive(request);

                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                System.out.println("New client connected: " + clientAddress.getHostAddress());

                ClientHandler clientThread = new ClientHandler(serverSocket, clientAddress, clientPort, request);
                clientThread.start();
            }
        } catch (IOException e) {
            System.out.println("Connection exception caught");
            e.printStackTrace();
        }
    }

    public static class ClientHandler extends Thread {

        private DatagramSocket socket;
        private InetAddress clientAddress;
        private int clientPort;
        private DatagramPacket request;

        public ClientHandler(DatagramSocket socket, InetAddress clientAddress, int clientPort, DatagramPacket request) {
            this.socket = socket;
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
            this.request = request;
        }

        @Override
        public void run() {
            try {
                byte[] buffer = request.getData();
                String clientMessage = new String(buffer, 0, request.getLength());

                // Process the client request
                processClientRequest(clientMessage);

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        private void processClientRequest(String clientMessage) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                FundraisingEventMessage eventMessage = mapper.readValue(clientMessage, FundraisingEventMessage.class);
                ArrayList<FundraisingEvent> events = eventMessage.events;
                String serverMessage = "";
                // Determine what type of request the client is making
                switch (eventMessage.getRequestType()) {
                    case CREATE_EVENT:
                        synchronized (allEvents) {
                            System.out.println("Creating event");
                            allEvents.add(events.get(0));
                            System.out.println("Event successfully created");
                            serverMessage = "Event successfully created";
                        }
                        break;
                    case DONATE:
                        synchronized (allEvents) {
                            System.out.println("Donating to event");
                            // Guard structure to clean data.
                            if (allEvents.isEmpty()) {
                                System.out.println("There are no events to donate to");
                                serverMessage = "There are no events to donate to";
                                break;
                            }
                            int eventIndex = eventMessage.getArrayIndex();
                            if (eventIndex < 0 || eventIndex >= allEvents.size()) {
                                System.out.println("Invalid event number. Please try again.");
                                serverMessage = "Invalid event number. Please try again.";
                                break;
                            }
                            double donation = eventMessage.getDonation();
                            double currAmountRaised = allEvents.get(eventIndex).getAmountRaised();
                            allEvents.get(eventIndex).setAmountRaised(currAmountRaised + donation);
                            System.out.println("Donation successfully made");
                            serverMessage = "Donation successfully made";
                        }
                        break;
                    case GET_PAST_CURENT_EVENTS:
                        synchronized (allEvents) {
                            System.out.println("Getting all events");
                            if (allEvents.isEmpty()) {
                                serverMessage = "There are no events to display";
                            } else {
                                System.out.println("Sending all events to client");
                                serverMessage = mapper.writeValueAsString(allEvents);
                            }
                        }
                        break;
                    default:
                        System.out.println("Invalid request type");
                }
                // Send the response
                byte[] responseBuffer = serverMessage.getBytes();
                DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length, clientAddress,
                        clientPort);
                socket.send(response);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
