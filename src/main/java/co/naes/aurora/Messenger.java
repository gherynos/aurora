/*
 * Copyright (C) 2020  Luca Zanconato (<luca.zanconato@naes.co>)
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package co.naes.aurora;

import co.naes.aurora.db.DBUtils;
import co.naes.aurora.db.IncomingFilePO;
import co.naes.aurora.db.OutgoingFilePO;
import co.naes.aurora.db.PartToReceivePO;
import co.naes.aurora.db.PartToSendPO;
import co.naes.aurora.db.PublicKeysUtils;
import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.in.ConfInMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.key.OutKeyMessage;
import co.naes.aurora.msg.out.ConfOutMessage;
import co.naes.aurora.msg.out.PartOutMessage;
import co.naes.aurora.parts.Joiner;
import co.naes.aurora.parts.Part;
import co.naes.aurora.parts.PartId;
import co.naes.aurora.parts.Splitter;
import co.naes.aurora.transport.AuroraTransport;
import co.naes.aurora.transport.IncomingMessageHandler;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Messenger implements IncomingMessageHandler  {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final AuroraTransport transport;

    private final AuroraSession session;

    private final StatusHandler handler;

    private final String incomingTempPath;

    protected static final int MAX_PARTS_TO_SEND_PER_FILE = 5;

    public interface StatusHandler {  // NOPMD

        void self(Messenger messenger);

        void sendingPart(int sequenceNumber, String fileId, String emailAddress);

        void unableToSendPart(int sequenceNumber, String fileId, String emailAddress);

        void processingPart(int sequenceNumber, String fileId, String emailAddress);

        void discardedPart(int sequenceNumber, String fileId, String emailAddress);

        void processingConfirmation(int sequenceNumber, String fileId, String emailAddress);

        void errorsWhileSendingMessages(String message);

        void errorsWhileReceivingMessages(String message);

        void errorsWhileProcessingReceivedMessage(String message);

        void errorsWhileProcessingKeyMessage(String message);

        void fileComplete(String fileId, String emailAddress, String path);

        char[] keyMessageReceived(String sender);

        void keyMessageSent(char[] password);

        void keysStored(String emailAddress);
    }

    protected Messenger(AuroraTransport transport, AuroraSession session, String confFolder, StatusHandler handler) throws AuroraException {

        this.transport = transport;
        this.session = session;
        this.handler = handler;

        incomingTempPath = confFolder + File.separator + "incoming";

        handler.self(this);

        transport.setIncomingMessageHandler(this);

        File iTemp = new File(incomingTempPath);
        if (!iTemp.exists() && !iTemp.mkdirs()) {

            throw new AuroraException("Unable to write to conf folder");
        }
    }

    public boolean addFileToSend(PublicKeys recipient, String filePath) throws AuroraException {

        if (!PublicKeysUtils.listAddresses(session.getDBUtils()).contains(recipient.getEmailAddress())) {

            // recipient not found
            throw new AuroraException(String.format("Recipient '%s' not found", recipient.getEmailAddress()));
        }

        String fileId = new File(filePath).getName();
        try {

            Splitter sp = new Splitter(fileId, filePath);
            OutgoingFilePO outgoingFile = new OutgoingFilePO(session.getDBUtils(), fileId, filePath, recipient.getEmailAddress(), sp.getTotalParts());
            outgoingFile.save();
            PartToSendPO.addAll(session.getDBUtils(), fileId, recipient.getEmailAddress(), sp.getTotalParts());
            sp.close();

            return true;

        } catch (AuroraException ex) {

            if (ex.getCause() instanceof JdbcSQLIntegrityConstraintViolationException) {

                // file already added
                logger.fine(String.format("File '%s' for recipient '%s' already added", fileId, recipient.getEmailAddress()));
                return false;

            } else {

                throw ex;
            }
        }
    }

    public void sendKeys(String emailAddress) throws AuroraException {

        logger.fine("Sending key message...");

        OutKeyMessage km = new OutKeyMessage(session, emailAddress, true);
        transport.sendKeyMessage(km);

        handler.keyMessageSent(km.getPassword());
    }

    public void send() {

        logger.fine("Sending messages...");

        try {

            // load pending files
            for (OutgoingFilePO pendingFile : OutgoingFilePO.getPending(session.getDBUtils())) {

                PublicKeys recipient = PublicKeysUtils.get(session.getDBUtils(), pendingFile.getEmailAddress());

                // load parts to send
                List<PartToSendPO> partsToSend = PartToSendPO.getNeverSent(session.getDBUtils(), pendingFile.getFileId(), recipient.getEmailAddress());
                if (partsToSend.size() > MAX_PARTS_TO_SEND_PER_FILE) {

                    partsToSend = partsToSend.subList(0, MAX_PARTS_TO_SEND_PER_FILE);
                }

                // send parts
                Splitter sp = new Splitter(pendingFile.getFileId(), pendingFile.getPath());  // NOPMD
                List<Integer> sent = new ArrayList<>();  // NOPMD
                for (PartToSendPO partToSend : partsToSend) {

                    try {

                        // send part message
                        logger.fine(String.format("Sending part %d for %s", partToSend.getSequenceNumber(), pendingFile.getFileId()));
                        handler.sendingPart(partToSend.getSequenceNumber(), partToSend.getFileId(), partToSend.getEmailAddress());
                        PartOutMessage msg = new PartOutMessage(session, recipient, sp.getPart(partToSend.getSequenceNumber()), true);  // NOPMD
                        transport.sendMessage(msg);
                        sent.add(partToSend.getSequenceNumber());

                    } catch (AuroraException ex) {

                        logger.log(Level.SEVERE, ex.getMessage(), ex);
                        handler.unableToSendPart(partToSend.getSequenceNumber(), partToSend.getFileId(), partToSend.getEmailAddress());
                    }
                }
                sp.close();

                // mark parts as sent
                PartToSendPO.markAsSent(session.getDBUtils(), sent, pendingFile.getFileId(), recipient.getEmailAddress());
            }

        } catch (AuroraException ex) {

            logger.log(Level.SEVERE, ex.getMessage(), ex);
            handler.errorsWhileSendingMessages(ex.getMessage());
        }
    }

    public void receive() {

        logger.fine("Receiving messages...");

        try {

            PartToSendPO.decreaseCounters(session.getDBUtils());
            transport.checkForMessages();

        } catch (AuroraException ex) {

            logger.log(Level.SEVERE, ex.getMessage(), ex);
            handler.errorsWhileReceivingMessages(ex.getMessage());
        }
    }

    @Override
    public boolean messageReceived(InMessage<?> message) {

        try {

            message.decrypt(session);

            if (message instanceof PartInMessage) {

                return processPartInMessage((PartInMessage) message);

            } else if (message instanceof ConfInMessage) {

                return processConfInMessage((ConfInMessage) message);

            } else {

                // unknown part type
                logger.warning(String.format("Unknown part type '%s'", message.getClass()));
                return false;
            }

        } catch (AuroraException ex) {

            logger.log(Level.SEVERE, ex.getMessage(), ex);
            handler.errorsWhileProcessingReceivedMessage(ex.getMessage());
        }

        return false;
    }

    private boolean processPartInMessage(PartInMessage message) throws AuroraException {

        Part part = message.getData();
        PublicKeys sender = PublicKeysUtils.get(session.getDBUtils(), message.getSender().getPublicKey());

        // check incoming file existence
        IncomingFilePO incomingFile = IncomingFilePO.get(session.getDBUtils(), part.getId().getFileId(), sender.getEmailAddress());
        if (incomingFile == null) {

            incomingFile = new IncomingFilePO(session.getDBUtils(), part.getId().getFileId(),
                    incomingTempPath + File.separator + part.getId().getFileId() + ".temp" ,
                    sender.getEmailAddress(), part.getTotal());
            incomingFile.save();

            // track parts to come
            PartToReceivePO.addAll(session.getDBUtils(), part.getId().getFileId(), sender.getEmailAddress(), part.getTotal());

        } else {

            if (incomingFile.isComplete()) {

                // part discarded
                logger.fine(String.format("Discarded part %d of %s", part.getId().getSequenceNumber(), part.getId().getFileId()));
                handler.discardedPart(part.getId().getSequenceNumber(), part.getId().getFileId(), sender.getEmailAddress());

                // send confirmation regardless
                ConfOutMessage conf = new ConfOutMessage(session, sender, part.getId(), true);
                transport.sendMessage(conf);

                return true;
            }
        }

        // store part in temporary file
        logger.fine(String.format("Processing part %d of %s", part.getId().getSequenceNumber(), part.getId().getFileId()));
        handler.processingPart(part.getId().getSequenceNumber(), part.getId().getFileId(), sender.getEmailAddress());
        Joiner joiner = new Joiner(incomingFile.getPath());
        joiner.putPart(part);
        joiner.close();

        // send confirmation
        ConfOutMessage conf = new ConfOutMessage(session, sender, part.getId(), true);
        transport.sendMessage(conf);

        // remove pending part to receive
        new PartToReceivePO(session.getDBUtils(), part.getId().getSequenceNumber(), part.getId().getFileId(), sender.getEmailAddress()).delete();

        IncomingFilePO.markFilesAsComplete(session.getDBUtils());
        incomingFile.refreshCompleteStatus();
        if (incomingFile.isComplete()) {

            try {

                // move file to incoming directory
                String newPath = String.format("%s%s%s",
                        session.getDBUtils().getProperties().get(DBUtils.INCOMING_DIRECTORY), File.separator, part.getId().getFileId());
                Files.move(
                        Paths.get(String.format("%s%s%s.temp", incomingTempPath, File.separator, part.getId().getFileId())),
                        Paths.get(newPath)
                );

                logger.fine(String.format("File %s complete", part.getId().getFileId()));
                handler.fileComplete(part.getId().getFileId(), sender.getEmailAddress(), newPath);

            } catch (IOException ex) {

                logger.log(Level.SEVERE, ex.getMessage(), ex);
                handler.errorsWhileProcessingReceivedMessage(ex.getMessage());
            }
        }

        // message processed successfully
        return true;
    }

    private boolean processConfInMessage(ConfInMessage message) throws AuroraException {

        // confirm part in DB
        PartId partId = message.getData();
        PublicKeys sender = PublicKeysUtils.get(session.getDBUtils(), message.getSender().getPublicKey());

        logger.fine(String.format("Processing confirmation %d of %s", partId.getSequenceNumber(), partId.getFileId()));
        handler.processingConfirmation(partId.getSequenceNumber(), partId.getFileId(), sender.getEmailAddress());
        new PartToSendPO(session.getDBUtils(), partId.getSequenceNumber(), partId.getFileId(), sender.getEmailAddress()).delete();

        // message processed successfully
        return true;
    }

    @Override
    public boolean keyMessageReceived(InKeyMessage keyMessage) {

        try {

            char[] password = handler.keyMessageReceived(keyMessage.getSender());
            if (password.length == 0) {

                return true;
            }

            PublicKeys keys = keyMessage.getPublicKeys(password);

            PublicKeysUtils.store(session.getDBUtils(), keys);

            handler.keysStored(keys.getEmailAddress());

        } catch (AuroraException ex) {

            handler.errorsWhileProcessingKeyMessage("Wrong password");
        }

        // remove key message in any case
        return true;
    }

    public DBUtils getDBUtils() {

        return session.getDBUtils();
    }
}
