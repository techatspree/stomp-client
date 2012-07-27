package de.akquinet.stomp;

import de.akquinet.stomp.frames.Frame;

import java.io.*;
import java.net.Socket;
import java.util.*;

import static de.akquinet.stomp.frames.Frame.END;
import static de.akquinet.stomp.frames.FrameFactory.*;
import static de.akquinet.stomp.frames.FrameType.*;
import static java.lang.String.valueOf;

public class StompClient {
    private final String host;
    private final int port;

    private BufferedReader in;
    private BufferedWriter out;
    private Socket socket;

    private boolean isConnected;
    private String disconnectReceiptId;

    private Thread receiver;
    private Map<String, MessageListener> subscriptions = new HashMap<String, MessageListener>();

    public StompClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        receiver = new Receiver();
    }

    public void connect() {
        if (!isConnected) {
            sendFrame(createConnectFrame(host, null, null));
            receiver.start();
        }
    }

    public void connectWith(String username, String password) {
        if (!isConnected) {
            sendFrame(createConnectFrame(host, username, password));
            receiver.start();
        }
    }

    public void send(String destination, String message) throws IOException {
        sendFrame(createSendFrame(destination, message));
    }

    public void subscribeTo(String destination, MessageListener listener) throws IOException {
        subscriptions.put(destination, listener);
        sendFrame(createSubscribeFrame(destination));
    }

    public void unsubscribeFrom(String destination) throws IOException {
        subscriptions.remove(destination);
        sendFrame(createUnsubscribeFrame(destination));
    }

    public void disconnect() throws IOException {
        disconnectReceiptId = String.valueOf(new Random().nextInt(10000));
        sendFrame(createDisconnectFrame(disconnectReceiptId));
        new Disconnector().start();
    }

    public void reconnectIfNecessary() throws IOException {
        if (socket.isClosed()) {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
    }


    private void sendFrame(Frame frame) {
        System.out.println("Send: ");
        System.out.println(frame.toString());
        try {
            out.write(frame.toString());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        StompClient stompClient = new StompClient("localhost", 6163);

        MessageListener listener = new MessageListener() {
            @Override
            public void onMessage(String message) {
                System.out.println(message);
            }
        };

        stompClient.connectWith("guest", "password");
        stompClient.subscribeTo("jms.topic.testTopic", listener);
        stompClient.send("jms.queue.testQueue", "Hello World");
        stompClient.disconnect();
        Thread.sleep(10000);
    }

    private class Disconnector extends Thread {
        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            receiver.interrupt();
            try {
                out.close();
                in.close();
                socket.close();
            } catch (IOException e) {
            }
            isConnected = false;
        }
    }

    private class Receiver extends Thread {
        @Override
        public void run() {
            try {
                do {
                    Frame frame = receiveFrame();
                    System.out.println("Received: ");
                    System.out.println(frame.toString());
                    if (frame.getType() == MESSAGE) {
                        String destination = frame.getHeaderValue("destination");
                        MessageListener subscriber = subscriptions.get(destination);
                        subscriber.onMessage(frame.getBody());
                    } else if (frame.getType() == RECEIPT) {
                        String receiptId = frame.getHeaderValue("receipt-id");
                        if (receiptId.equals(disconnectReceiptId)) {
                            out.close();
                            in.close();
                            socket.close();
                            return;
                        }
                    } else if (frame.getType() == CONNECTED) {
                        isConnected = true;
                    }
                } while (true);
            } catch (IOException e) {
            }
        }

        private String readLine() throws IOException {
            while (true) {
                final String line = in.readLine();
                return line != null ? line : "";
            }
        }

        private Frame receiveFrame() throws IOException {
            String line = "";
            String body = "";
            String command = null;
            Map<String, String> header = new LinkedHashMap<String, String>();
            boolean isBody = false;

            while (!line.endsWith(valueOf(END))) {
                line = readLine();

                if (line == null)
                    continue;

                if (command == null) {
                    if (!line.trim().isEmpty())
                        command = line.trim();
                } else if (!isBody) {
                    StringTokenizer headerToken = new StringTokenizer(line, ":");
                    if (headerToken.countTokens() == 2)
                        header.put(headerToken.nextToken(), headerToken.nextToken());
                    else
                        isBody = true;
                }

                if (isBody)
                    body += line.trim();
            }

            return createCustomFrame(command, header, body);
        }
    }
}
