package org.wso2.transport.iso8583;
/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND+ either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.log4j.Logger;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

public class ConnectionHandler {

    private Socket serverSocket;
    private DataOutputStream outToClient;
    private DataInputStream inputStreamReader;
    private GenericPackager packager;

    public ConnectionHandler(Socket socket) throws IOException, ISOException {
        this.packager = new GenericPackager("jposdef.xml");
        this.serverSocket = socket;
        this.inputStreamReader = new DataInputStream(serverSocket.getInputStream());
        this.outToClient = new DataOutputStream(serverSocket.getOutputStream());
        run();
    }

    private void run() {
        try {
            if (serverSocket.isConnected()) {
                System.out.println("There is a client connected");
                if (inputStreamReader.available() > 0) {
                    int length = inputStreamReader.available();
                    byte[] dataFromClient = new byte[length];
                    inputStreamReader.readFully(dataFromClient, 0, length);
                    System.out.println("Data From Client : " + new String(dataFromClient));
                    byte[] isomsg = unpackRequest(dataFromClient);
                    outToClient.write(isomsg);
                }
            }
        } catch (IOException ioe) {
            System.out.println("Error while receiving the messages" + ioe);
        } catch (ISOException e) {
            System.out.println("Error while unpack the messages"+ e);
        } catch (Exception e) {
            System.out.println("Error while send the ack to Sender"+ e);
        } finally {
            try {
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println("Couldn't close I/O Streams"+ e);
            }
        }
    }

    private byte[] unpackRequest(byte[] message) throws ISOException {
        ISOMsg isoMsg = new ISOMsg();
        isoMsg.setPackager(packager);
//        isoMsg.unpack(message);
        isoMsg.setMTI("1814");
        isoMsg.set("39", "000");
        return isoMsg.pack();

    }

    private String process(ISOMsg isomsg) throws Exception {
        System.out.println("ISO Message MTI is " + isomsg.getMTI());
        String message = "";
        for (int i = 0; i < 128; i++) {
            if (isomsg.hasField(i)) {
                message += getISO8583Properties().getProperty(Integer.toString(i)) + "=" + isomsg.getValue(i) + "\n";
            }
        }
        System.out.println(message);
        return message;
    }

    private Properties getISO8583Properties() {
        Properties prop = new Properties();
        try {
            FileInputStream input = new FileInputStream("jposdef.xml");
            prop.loadFromXML(input);
            input.close();
        } catch (IOException e) {
            System.out.println("Couldn't read the input file"+ e);
        }
        return prop;
    }
}
