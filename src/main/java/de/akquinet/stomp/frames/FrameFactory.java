package de.akquinet.stomp.frames;

import java.util.Map;

import static de.akquinet.stomp.frames.FrameType.CONNECT;
import static de.akquinet.stomp.frames.FrameType.SUBSCRIBE;
import static de.akquinet.stomp.frames.FrameType.UNSUBSCRIBE;
import static java.lang.String.valueOf;

public class FrameFactory {

    public static Frame createConnectFrame(String host, String username, String password) {
        Frame connectFrame = new Frame(CONNECT);
        connectFrame.withHeaderField("accept-version", "1.1");
        connectFrame.withHeaderField("host", host);
        if (username != null && password != null) {
            connectFrame.withHeaderField("login", username);
            connectFrame.withHeaderField("passcode", password);
        }

        return connectFrame;
    }

    public static Frame createSendFrame(String destination, String message) {
        Frame sendFrame = new Frame(FrameType.SEND);
        sendFrame.withHeaderField("destination", destination);
        sendFrame.withHeaderField("id", "0");
        sendFrame.withHeaderField("destination", destination);
        sendFrame.withHeaderField("content-type", "text/plain");
        sendFrame.withHeaderField("content-length", valueOf(message.length()));
        sendFrame.setBody(message);

        return sendFrame;
    }

    public static Frame createDisconnectFrame(String receiptId) {
        Frame disconnectFrame = new Frame(FrameType.DISCONNECT);
        disconnectFrame.withHeaderField("receipt", valueOf(receiptId));

        return disconnectFrame;
    }

    public static Frame createSubscribeFrame(String destination) {
        Frame subscribeFrame = new Frame(SUBSCRIBE);
        subscribeFrame.withHeaderField("destination", destination);

        return subscribeFrame;
    }

    public static Frame createUnsubscribeFrame(String destination) {
        Frame subscribeFrame = new Frame(UNSUBSCRIBE);
        subscribeFrame.withHeaderField("destination", destination);

        return subscribeFrame;
    }

    public static Frame createCustomFrame(String command, Map<String, String> header, String body) {
        Frame frame = new Frame(FrameType.valueOf(command));
        frame.withHeaderFields(header);
        frame.setBody(body);

        return frame;
    }
}
