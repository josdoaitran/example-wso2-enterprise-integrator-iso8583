package org.wso2.carbon.message;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NewRequest {
    public static void main(String[] args) throws IOException, ISOException {
        // Create ISO Mesage
//        GenericPackager packager = new GenericPackager("CustomConfig.xml");
        GenericPackager packager = new GenericPackager("iso87ascii.xml");

//        String hostName = "10.209.23.27";
//        int portNumber = 5555;
        String hostName = "localhost";
        int portNumber = 5010;
        Socket socket = new Socket(hostName, portNumber);


        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);


        isoMsg.set("0", "1840");
        isoMsg.set("11", "850967");
        isoMsg.set("12", "220714251619");
        isoMsg.set("24", "831");

        byte[] isoSendMessage = isoMsg.pack();
        logISOMsg(isoMsg);


        clientHandler(socket, isoSendMessage);

        System.out.println("********** Response***********");



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