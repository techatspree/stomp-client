package de.akquinet.stomp.frames;

import java.util.LinkedHashMap;
import java.util.Map;

public class Frame {
    public static char NEWLINE = '\n';
    public static char END = 0;

    private final FrameType type;
    private final Map<String, String> header = new LinkedHashMap<String, String>();
    private String body;

    Frame(FrameType type) {
        this.type = type;
    }

    void withHeaderField(String name, String value) {
        header.put(name, value);
    }

    void withHeaderFields(Map<String, String> header) {
        this.header.putAll(header);
    }

    public final String getHeaderValue(String headerName) {
        return header.get(headerName);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public final String getBody() {
        return body;
    }

    public FrameType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(type.toString()).append(NEWLINE);

        for (String headerKey : header.keySet()) {
            sb.append(headerKey).append(":").append(header.get(headerKey)).append(NEWLINE);
        }
        sb.append(NEWLINE);
        if (body != null)
            sb.append(body);
        sb.append(END);

        return sb.toString();
    }
}
