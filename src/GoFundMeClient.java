import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Models.FundraisingEvent;
import Models.FundraisingEventMessage;
import Models.RequestTypeEnum;
import Utils.AsciiTable;

public class GoFundMeClient {
    private static final int SERVER_PORT = 6789;
    private static final String SERVER_IP = "localhost";

    public static void main(String[] args) {

        try (DatagramSocket clientSocket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            Scanner scanner = new Scanner(System.in);

            System.out.println("Connected to the server.");
            boolean stayConnected = true;
            while (stayConnected) {
                displayOptionsMenu();
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        createEvent(clientSocket, serverAddress, SERVER_PORT, scanner);
                        break;
                    case "2":
                        donate(clientSocket, serverAddress, SERVER_PORT, scanner);
                        break;
                    case "3":
                        listEvents(clientSocket, serverAddress, SERVER_PORT);
                        break;
                    case "4":
                        System.out.println("Exiting the program.");
                        sendRequest(clientSocket, serverAddress, SERVER_PORT, "exit");
                        stayConnected = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a fundraising event
     * 
     * @param out
     * @param scanner
     * @param in
     */
    private static void createEvent(DatagramSocket socket, InetAddress address, int port, Scanner scanner) {
        System.out.print("Event Name: ");
        String eventName = scanner.nextLine();
        // Get target amount
        System.out.print("Target Amount: ");
        int targetAmount = 0;
        boolean validInput = false;
        // validate input
        while (!validInput) {
            try {
                targetAmount = Integer.parseInt(scanner.nextLine());
                if (targetAmount < 0) {
                    System.out.println("Invalid target amount. Please try again.");
                } else {
                    validInput = true;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
        // Get deadline
        System.out.print("Deadline (yyyy-mm-dd): ");
        String deadline = scanner.nextLine();
        // validate deadline
        while (!deadline.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("Invalid deadline. Ensure format is as shown. Please try again.");
            System.out.print("Deadline (yyyy-mm-dd): ");
            deadline = scanner.nextLine();
        }
        // Send event to server
        ArrayList<FundraisingEvent> events = new ArrayList<FundraisingEvent>();
        events.add(new FundraisingEvent(eventName, targetAmount, deadline));
        FundraisingEventMessage fundraisingEventMessage = new FundraisingEventMessage(RequestTypeEnum.CREATE_EVENT,
                events);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String eventMessageString = objectMapper.writeValueAsString(fundraisingEventMessage);
            String response = sendRequest(socket, address, port, eventMessageString);
            System.out.println(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * List all events
     * 
     * @param out
     * @param in
     */
    private static void listEvents(DatagramSocket socket, InetAddress address, int port) {
        FundraisingEventMessage eventMessage = new FundraisingEventMessage(RequestTypeEnum.GET_PAST_CURENT_EVENTS);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Request all events from server
            String eventMessageString = objectMapper.writeValueAsString(eventMessage);
            // Get all events from server
            String eventsString = sendRequest(socket, address, port, eventMessageString);

            ArrayList<FundraisingEvent> receivedEventMessage = objectMapper.readValue(eventsString,
                    new TypeReference<ArrayList<FundraisingEvent>>() {
                    });
            ArrayList<FundraisingEvent> events = receivedEventMessage;
            printAllEvents(events);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println("There are no events to display");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Donate to an event
     * 
     * @param out
     * @param scanner
     * @param in
     */
    private static void donate(DatagramSocket socket, InetAddress address, int port, Scanner scanner) {
        System.out.print("Event Number: ");
        int eventIndex = -1;
        boolean validInput = false;
        while (!validInput) {
            try {
                eventIndex = scanner.nextInt();
                if (eventIndex < 0) {
                    System.out.println("Invalid event number. Must be above or equal to 0 Please try again.");
                } else {
                    validInput = true;
                }
            } catch (Exception ex) {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.nextLine(); // Consume invalid input
            }
        }
        System.out.print("Amount: ");
        double amount = -1;
        validInput = false;
        while (!validInput) {
            try {
                amount = scanner.nextDouble();
                if (amount < 0) {
                    System.out.println("Invalid amount. Please try again.");
                } else {
                    validInput = true;
                }
            } catch (Exception ex) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Consume invalid input
            }
        }
        ArrayList<FundraisingEvent> events = new ArrayList<FundraisingEvent>();
        FundraisingEventMessage eventMessage = new FundraisingEventMessage(RequestTypeEnum.DONATE, events, amount);
        eventMessage.setarrayIndex(eventIndex);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(eventMessage);
            String response = sendRequest(socket, address, port, message);
            System.out.println(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String sendRequest(DatagramSocket socket, InetAddress address, int port, String message)
            throws IOException {
        byte[] buffer = message.getBytes();
        DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(request);

        byte[] responseBuffer = new byte[1024];
        DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
        socket.receive(response);
        return new String(response.getData(), 0, response.getLength());
    }

    /**
     * Print all events
     * 
     * @param events
     */
    private static void printAllEvents(ArrayList<FundraisingEvent> events) {
        AsciiTable table = new AsciiTable();
        table.setHeaders("Event Number", "Category", "Event Name", "Target Amount", "Amount Raised", "Deadline");
        int eventNumber = 0;
        for (FundraisingEvent event : events) {
            String category = LocalDate.parse(event.getDeadline()).isBefore(LocalDate.now()) ? "Past" : "Current";
            table.addRow(String.valueOf(eventNumber), category, event.getEventName(),
                    String.valueOf(event.getTargetAmount()),
                    String.valueOf(event.getAmountRaised()), event.getDeadline().toString());
            eventNumber++;
        }
        table.printTable();
    }

    /**
     * Display options menu
     */
    private static void displayOptionsMenu() {
        System.out.println();
        System.out.println("(1) Create a fundraising event");
        System.out.println("(2) Donate to a fundraising event");
        System.out.println("(3) View all fundraising events");
        System.out.println("(4) Exit");
        System.out.println("Please select an option by typing the corresponding number: ");
    }
}
