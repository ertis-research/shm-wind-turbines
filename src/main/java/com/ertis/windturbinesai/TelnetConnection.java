package com.ertis.windturbinesai;

import org.apache.commons.net.telnet.TelnetClient;
import java.io.InputStream;
import java.io.PrintStream;

public class TelnetConnection {
    private TelnetClient telnet;
    private InputStream in;
    private PrintStream out;

    public TelnetConnection(String server, int port) {
        try {
            telnet = new TelnetClient();
            telnet.connect(server, port);

            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String value) {
        try {
            System.out.println("Sending command: " + value);
            out.println(value);
            out.flush();

            String response = readResponse();
            if (response.trim().isEmpty()) {
                System.out.println("Empty response");
            } else {
                System.out.println("Response: " + response);
            }

            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String readResponse() {
        StringBuffer sb = new StringBuffer();

        try {
            do {
                if (in.available() > 0) {
                    char ch = (char)in.read();
                    sb.append(ch);
                } else {
                    Thread.sleep(1000);
                }
            } while (in.available() > 0);

            String output = new String(sb);
            return output;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
