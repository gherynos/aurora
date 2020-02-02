package co.naes.aurora;

import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.in.StringInMessage;
import co.naes.aurora.parts.Part;
import co.naes.aurora.transport.IncomingMessageHandler;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.MailTransport;
import co.naes.aurora.ui.Settings;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    public static String CONF_FOLDER = String.format("%s%c.aurora", System.getProperty("user.home"), File.separatorChar);

    private char[] temp_pwd;

    Main() throws Exception {

        LogManager.getLogManager().readConfiguration(this.getClass().getResourceAsStream("/logging.properties"));

        LocalDB db = new LocalDB("theDbPassword");
        AuroraSession session = new AuroraSession(db);

        AuroraTransport transport = new MailTransport(db);

        Messenger messenger = new Messenger(db, transport, session, new Messenger.StatusHandler() {

            @Override
            public void sendingPart(int sequenceNumber, String fileId, String emailAddress) {

                System.out.println("Sending part " + sequenceNumber + " " + fileId + " " + emailAddress);
            }

            @Override
            public void unableToSendPart(int sequenceNumber, String fileId, String emailAddress) {

                System.out.println("Unable to send part " + sequenceNumber + " " + fileId + " " + emailAddress);
            }

            @Override
            public void discardedPart(int sequenceNumber, String fileId, String emailAddress) {

                System.out.println("Discarded part " + sequenceNumber + " " + fileId + " " + emailAddress);
            }

            @Override
            public void processingPart(int sequenceNumber, String fileId, String emailAddress) {

                System.out.println("Processing part " + sequenceNumber + " " + fileId + " " + emailAddress);
            }

            @Override
            public void processingConfirmation(int sequenceNumber, String fileId, String emailAddress) {

                System.out.println("Processing confirmation " + sequenceNumber + " " + fileId + " " + emailAddress);
            }

            @Override
            public void errorsWhileSendingMessages(String message) {

                System.out.println("Error: " + message);
            }

            @Override
            public void errorsWhileReceivingMessages(String message) {

                System.out.println("Error: " + message);
            }

            @Override
            public void errorsWhileProcessingReceivedMessage(String message) {

                System.out.println("Error: " + message);
            }

            @Override
            public void errorsWhileProcessingKeyMessage(String message) {

                System.out.println("Error: " + message);
            }

            @Override
            public void fileComplete(String fileId, String emailAddress, String path) {

                System.out.println("File complete " + " " + fileId + " " + emailAddress + " " + path);
            }

            @Override
            public char[] keyMessageReceived() {

                System.out.println("Key message received");

                return temp_pwd;
            }

            @Override
            public void keyMessageSent(char[] password) {

                System.out.println("Key message sent");

                temp_pwd = password;
            }
        });

//        new Settings(db);

//        messenger.sendKeys("luca.zanconato@naes.co");
//        Thread.sleep(30000);
//        messenger.receive();

//        PublicKeys self = db.getPublicKeys("service@naes.co");
//        messenger.addFileToSend(self, "/Users/gherynos/Downloads/BonificoOrdinario.pdf.pdf");
//        messenger.send();
//        messenger.receive();
    }

    public static void main(String[] args) throws Exception {

        new Main();
    }
}
