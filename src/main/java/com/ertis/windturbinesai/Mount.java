package com.ertis.windturbinesai;

public class Mount {
    // Left movements
    private final String LEFT_1 = "ESPt00249F0!";
    private final String LEFT_2 = "ESPt00493E0!";
    private final String LEFT_3 = "ESPt006DDD0!";
    private final String LEFT_4 = "ESPt00927C0!";
    private final String LEFT_5 = "ESPt00B71B0!";
    private final String LEFT_6 = "ESPt00DBBA0!";

    // Right movements
    private final String DER_1 = "ESPt0FDB60F!";
    private final String DER_2 = "ESPt0FB6C1F!";
    private final String DER_3 = "ESPt0F9222F!";
    private final String DER_4 = "ESPt0F6D83F!";
    private final String DER_5 = "ESPt0F48E4F!";
    private final String DER_6 = "ESPt0F2445F!";

    private final String BASE = "ESPt0000000!";

    private String server;
    private int port;
    private TelnetConnection telnet;
    private double centerX;
    private int dir; // 1 = Left, 2 = Right
    private int cont;

    public Mount() {
        server = "192.168.47.1";
        port = 54372;

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    telnet = new TelnetConnection(server, port);
                    System.out.println("Correct connection");
                    toBase();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error connecting");
                }
            }
        });

        thread.start();
    }

    public void setCenterX(int[] values) {
        centerX = Double.valueOf(values[0])/2;
    }

    public void toBase() throws InterruptedException {
        dir = 0;
        cont = 1;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    telnet.sendCommand(BASE);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error sending the command");
                }
            }
        });

        thread.start();
        thread.join();
    }

    public int moveMount(int[] bbox) throws InterruptedException {
        double bboxCenterX = Double.valueOf(bbox[0] + bbox[2])/2;;

        Thread thread = new Thread(new Runnable() {

            String response;

            @Override
            public void run() {
                try  {
                    if(dir == 0) {
                        if (centerX > bboxCenterX) {
                            dir = 1;
                            response = telnet.sendCommand(DER_1);
                            System.out.println(response);
                            cont++;
                        } else if (centerX < bboxCenterX) {
                            dir = 2;
                            response = telnet.sendCommand(LEFT_1);
                            System.out.println(response);
                            cont++;
                        }
                    } else if(dir == 1) { // Look to the left (right movement)
                        if(cont == 2) {
                            response = telnet.sendCommand(DER_2);
                            System.out.println(response);
                        } else if(cont == 3) {
                            response = telnet.sendCommand(DER_3);
                            System.out.println(response);
                        } else if(cont == 4) {
                            response = telnet.sendCommand(DER_4);
                            System.out.println(response);
                        } else if(cont == 5) {
                            response = telnet.sendCommand(DER_5);
                            System.out.println(response);
                        } else if(cont == 6) {
                            response = telnet.sendCommand(DER_6);
                            System.out.println(response);
                        }
                        cont++;
                    } else if(dir == 2) { // Look to the right (left movement)
                        if(cont == 2) {
                            response = telnet.sendCommand(LEFT_2);
                            System.out.println(response);
                        } else if(cont == 3) {
                            response = telnet.sendCommand(LEFT_3);
                            System.out.println(response);
                        } else if(cont == 4) {
                            response = telnet.sendCommand(LEFT_4);
                            System.out.println(response);
                        } else if(cont == 5) {
                            response = telnet.sendCommand(LEFT_5);
                            System.out.println(response);
                        } else if(cont == 6) {
                            response = telnet.sendCommand(LEFT_6);
                            System.out.println(response);
                        }
                        cont++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error sending the command");
                }
            }
        });

        thread.start();
        thread.join();

        return cont;
    }

    public void disconnect() {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    telnet.sendCommand(BASE);
                    telnet.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error disconnecting");
                }
            }
        });

        thread.start();
    }
}
