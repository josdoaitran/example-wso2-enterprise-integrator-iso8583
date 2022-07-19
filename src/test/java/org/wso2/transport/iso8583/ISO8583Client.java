package org.wso2.transport.iso8583;


import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ISO8583Client {
    private Logger log = Logger.getLogger(getClass());
    private static final String host = "localhost";
    private static final int port = 5010;

    private ISO8583Client() {

        Socket socket = null;
        try {
            ISOPackager packager = new GenericPackager("jposdef.xml");
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);

            isoMsg.set("0", "1800");
            isoMsg.set("3", "110");
            isoMsg.set("5", "4200.00");
            isoMsg.set("48", "Simple Credit Transaction");
            isoMsg.set("6", "645.23");
            isoMsg.set("8", "66377125");

            byte[] isoSendMessage = isoMsg.pack();
            socket = new Socket(host, port);
            clientHandler(socket, isoSendMessage);

        } catch (IOException e) {
            log.error("Couldn't create Socket", e);
        } catch (ISOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Couldn't close the Socket" +  e.getMessage());
                }
            }
        }
    }

    /**
     * Handle the iso8583 message request and responses
     * @param connection  Socket connection with backend Test server
     * @param isoMessage  packed ISOMessage
     */

    private void clientHandler(Socket connection, byte[] isoMessage) {

        DataOutputStream outStream = null;
        BufferedReader inFromServer = null;
        try {
            outStream = new DataOutputStream(connection.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            if (connection.isConnected()) {
                outStream.write(isoMessage);
                outStream.flush();

                /* Sender will receive the Acknowledgement here */
                String output = inFromServer.readLine();
                System.out.println("Response From Server: " + output);

                byte[] isoServerMessage = output.getBytes();
                unpackMessage(isoServerMessage);
            }
        } catch (IOException e) {
            System.out.println("An exception occurred in sending the iso8583 message" +  e.getMessage());
        } catch (ISOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (inFromServer != null) {
                    inFromServer.close();
                }
            } catch (IOException e) {
                System.out.println("Couldn't close the I/O Streams"+ e.getMessage());
            }
        }
    }

    private void unpackMessage(byte[] message) throws ISOException {
        ISOPackager packager = new GenericPackager("jposdef.xml");
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);
        isoMsg.unpack(message);
        logISOMsg(isoMsg);
    }

    private static void logISOMsg(ISOMsg msg) {
        System.out.println("----ISO MESSAGE to Pack-----");
        try {
            System.out.println("  MTI : " + msg.getMTI());
            for (int i = 1; i <= msg.getMaxField(); i++) {
                if (msg.hasField(i)) {
                    System.out.println("    Field-" + i + " : " + msg.getString(i));
                }
            }
        } catch (ISOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("--------------------");
        }
    }

    public static void main(String[] args) {

        new ISO8583Client();
    }
}
