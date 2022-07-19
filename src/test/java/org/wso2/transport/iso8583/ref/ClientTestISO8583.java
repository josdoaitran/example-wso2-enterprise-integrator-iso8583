package org.wso2.transport.iso8583.ref;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.iso.ISOPackager;

import java.io.*;
import java.net.Socket;
//import java.nio.charset.StandardCharsets;

public class ClientTestISO8583 {
    public static void main(String[] args) throws IOException, ISOException {

        ISOPackager packager = new GenericPackager("jposdef.xml");
//        String hostName = "10.209.23.27";
//        int portNumber = 5555;
        String hostName = "localhost";
        int portNumber = 5010;
//        Socket socket = new Socket(hostName, portNumber);

        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);


        isoMsg.set("0", "1800");
        isoMsg.set("3", "110");
        isoMsg.set("5", "4200.00");
        isoMsg.set("48", "Simple Credit Transaction");
        isoMsg.set("6", "645.23");
        isoMsg.set("8", "66377125");

        byte[] isoSendMessage = isoMsg.pack();
        logISOMsg(isoMsg);

        BufferedOutputStream outStream = null;
        BufferedReader receive_PackedResponseData = null;
        Socket connection = new Socket("localhost", 5010);
        if (connection.isConnected()) {
            outStream = new BufferedOutputStream(
                    connection.getOutputStream());
            outStream.write(isoSendMessage);
            outStream.flush();
            System.out.println("-----Request sent to server---");
            System.out.println(ISOUtil.hexString(isoSendMessage));
        }

        /** Receive Response */
        System.out.println("********** Response***********");

        if (connection.isConnected()) {
            receive_PackedResponseData = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
            System.out.println("-- RESPONSE recieved---");
            System.out.println(receive_PackedResponseData);
             System.out.println("Response : "+ISOUtil.hexString(receive_PackedResponseData.readLine().getBytes()));
        }

        receive_PackedResponseData.close();
        outStream.close();
        connection.close();



//        clientHandler(socket, isoSendMessage);


    }

    private static void clientHandler(Socket connection, byte[] isoMessage) {

        DataOutputStream outStream = null;
        BufferedReader inFromServer = null;
        try {
            outStream = new DataOutputStream(connection.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            if (connection.isConnected()) {
                outStream.write(isoMessage);
                outStream.flush();

                String messageFromServer;
                while ((messageFromServer = inFromServer.readLine()) != null) {
                    System.out.println("Response From Server :" + messageFromServer);
                }

//                ISOMsg isoReceiveMessage = new ISOMsg();
//                GenericPackager packager = new GenericPackager("iso87ascii.xml");
//                isoReceiveMessage.setPackager(packager);
//                isoReceiveMessage.unpack(messageFromServer.getBytes(StandardCharsets.UTF_8));
//                logISOMsg(isoReceiveMessage);
//                logISOMsg(isoReceiveMessage);
            }
        } catch (IOException e) {
            System.out.println("An exception occurred in sending the iso8583 message" + e.getMessage());
//        } catch (ISOException e) {
//            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (inFromServer != null) {
                    inFromServer.close();
                }
            } catch (IOException e) {
                System.out.println("Couldn't close the I/O Streams" +  e.getMessage());
            }
        }
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
}
