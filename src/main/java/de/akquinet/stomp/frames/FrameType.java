package de.akquinet.stomp.frames;

public enum FrameType {
    // Server frames
    CONNECTED("CONNECTED"),
    MESSAGE("MESSAGE"),
    RECEIPT("RECEIPT"),
    ERROR("ERROR"),

    // Client frames
    CONNECT("CONNECT"),
    SEND("SEND"),
    SUBSCRIBE("SUBSCRIBE"),
    UNSUBSCRIBE("UNSUBSCRIBE"),
    BEGIN("BEGIN"),
    COMMIT("COMMIT"),
    ABORT("ABORT"),
    ACK("ACK"),
    NACK("NACK"),
    DISCONNECT("DISCONNECT");

    private final String command;

    private FrameType(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }
}
