/*
 * Copyright (C) 2020-2022  Luca Zanconato (<github.com/gherynos>)
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
import co.naes.aurora.msg.in.PublicKeysInMessage;
import co.naes.aurora.msg.key.InKeyMessage;
import co.naes.aurora.msg.key.OutKeyMessage;
import co.naes.aurora.msg.out.ConfOutMessage;
import co.naes.aurora.msg.out.PartOutMessage;
import co.naes.aurora.msg.out.PublicKeysOutMessage;
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

public class Messenger implements IncomingMessageHandler  {

    protected final LogUtils logUtils = LogUtils.getLogUtils(getClass().getName());

    private final AuroraTransport transport;

    private final AuroraSession session;

    private final DBUtils db;

    private final StatusHandler handler;

    private final String incomingTempPath;

    protected static final int MAX_PARTS_TO_SEND_PER_FILE = 5;

    public static final String TEMP_FILE_EXTENSION = ".temp";

    public interface StatusHandler {  // NOPMD

        void self(Messenger messenger);

        void sendingPart(int sequenceNumber, String fileId, Identifier identifier);

        void unableToSendPart(int sequenceNumber, String fileId, Identifier identifier);

        void processingPart(int sequenceNumber, String fileId, Identifier identifier);

        void discardedPart(int sequenceNumber, String fileId, Identifier identifier);

        void processingConfirmation(int sequenceNumber, String fileId, Identifier identifier);

        void errorsWhileSendingMessages(String message);

        void errorsWhileReceivingMessages(String message);

        void errorsWhileProcessingReceivedMessage(String message);

        void errorsWhileProcessingKeyMessage(String message);

        void fileComplete(String fileId, Identifier identifier, String path);

        char[] keyMessageReceived(String sender);

        void keyMessageSent(char[] password);

        void keysStored(Identifier identifier);

        boolean publicKeysReceived(Identifier identifier);
    }

    protected Messenger(AuroraTransport transport, AuroraSession session, String confFolder, StatusHandler handler) throws AuroraException {

        this.transport = transport;
        this.session = session;
        this.handler = handler;

        db = session.getDBUtils();

        incomingTempPath = confFolder + File.separator + "incoming";

        handler.self(this);

        transport.setIncomingMessageHandler(this);

        File iTemp = new File(incomingTempPath);
        if (!iTemp.exists() && !iTemp.mkdirs()) {

            throw new AuroraException("Unable to write to conf folder");
        }
    }

    public boolean addFileToSend(PublicKeys recipient, String filePath) throws AuroraException {

        if (!PublicKeysUtils.listIdentifiers(db).contains(recipient.getIdentifier())) {

            // recipient not found
            throw new AuroraException(String.format("Recipient '%s' not found", recipient.getIdentifier()));
        }

        String fileId = new File(filePath).getName();
        try {

            Splitter sp = new Splitter(fileId, filePath);
            OutgoingFilePO outgoingFile = new OutgoingFilePO(db, fileId, filePath, recipient.getIdentifier(), sp.getTotalParts());
            outgoingFile.save();
            PartToSendPO.addAll(db, fileId, recipient.getIdentifier(), sp.getTotalParts());
            sp.close();

            return true;

        } catch (AuroraException ex) {

            if (ex.getCause() instanceof JdbcSQLIntegrityConstraintViolationException) {

                // file already added
                logUtils.logFine("File '%s' for recipient '%s' already added", fileId, recipient.getIdentifier());
                return false;

            } else {

                throw ex;
            }
        }
    }

    public void sendKeys(Identifier recipientIdentifier) throws AuroraException {

        logUtils.logFine("Sending key message...");

        OutKeyMessage km = new OutKeyMessage(session, recipientIdentifier, transport.requiresArmoredMessages());
        transport.sendKeyMessage(km);

        handler.keyMessageSent(km.getPassword());
    }

    public void send() {

        logUtils.logFine("Sending messages...");

        try {

            // load pending files
            for (OutgoingFilePO pendingFile : OutgoingFilePO.getPending(db)) {

                PublicKeys recipient = PublicKeysUtils.get(db, pendingFile.getIdentifier());

                // load parts to send
                List<PartToSendPO> partsToSend = PartToSendPO.getNeverSent(db, pendingFile.getFileId(), recipient.getIdentifier());
                if (partsToSend.size() > MAX_PARTS_TO_SEND_PER_FILE) {

                    partsToSend = partsToSend.subList(0, MAX_PARTS_TO_SEND_PER_FILE);
                }

                // send parts
                Splitter sp = new Splitter(pendingFile.getFileId(), pendingFile.getPath());  // NOPMD
                List<Integer> sent = new ArrayList<>();  // NOPMD
                for (PartToSendPO partToSend : partsToSend) {

                    try {

                        // send part message
                        logUtils.logFine("Sending part %d for %s", partToSend.getSequenceNumber(), pendingFile.getFileId());
                        handler.sendingPart(partToSend.getSequenceNumber(), partToSend.getFileId(), partToSend.getIdentifier());
                        PartOutMessage msg = new PartOutMessage(session, recipient, sp.getPart(partToSend.getSequenceNumber()),   // NOPMD
                                transport.requiresArmoredMessages());
                        transport.sendMessage(msg);
                        sent.add(partToSend.getSequenceNumber());

                    } catch (AuroraException ex) {

                        logUtils.logError(ex);
                        handler.unableToSendPart(partToSend.getSequenceNumber(), partToSend.getFileId(), partToSend.getIdentifier());
                    }
                }
                sp.close();

                // mark parts as sent
                PartToSendPO.markAsSent(db, sent, pendingFile.getFileId(), recipient.getIdentifier());
            }

        } catch (AuroraException ex) {

            logUtils.logError(ex);
            handler.errorsWhileSendingMessages(ex.getMessage());
        }
    }

    public void receive() {

        logUtils.logFine("Receiving messages...");

        try {

            PartToSendPO.decreaseCounters(db);
            transport.checkForMessages();

        } catch (AuroraException ex) {

            logUtils.logError(ex);
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

            } else if (message instanceof PublicKeysInMessage) {

                return processPublicKeysInMessage((PublicKeysInMessage) message);

            } else {

                // unknown message type
                logUtils.logWarning("Unknown message type '%s'", message.getClass());
                return false;
            }

        } catch (AuroraException ex) {

            logUtils.logError(ex);
            handler.errorsWhileProcessingReceivedMessage(ex.getMessage());
        }

        return false;
    }

    private boolean processPartInMessage(PartInMessage message) throws AuroraException {

        Part part = message.getData();
        PublicKeys sender = PublicKeysUtils.get(db, message.getSender().getPublicKey());

        // check incoming file existence
        IncomingFilePO incomingFile = IncomingFilePO.get(db, part.getId().getFileId(), sender.getIdentifier());
        if (incomingFile == null) {

            incomingFile = new IncomingFilePO(db, part.getId().getFileId(),
                    incomingTempPath + File.separator + part.getId().getFileId() + TEMP_FILE_EXTENSION,
                    sender.getIdentifier(), part.getTotal());
            incomingFile.save();

            // track parts to come
            PartToReceivePO.addAll(db, part.getId().getFileId(), sender.getIdentifier(), part.getTotal());

        } else {

            if (incomingFile.isComplete()) {

                // part discarded
                logUtils.logFine("Discarded part %d of %s", part.getId().getSequenceNumber(), part.getId().getFileId());
                handler.discardedPart(part.getId().getSequenceNumber(), part.getId().getFileId(), sender.getIdentifier());

                // send confirmation regardless
                ConfOutMessage conf = new ConfOutMessage(session, sender, part.getId(), transport.requiresArmoredMessages());
                transport.sendMessage(conf);

                return true;
            }
        }

        // store part in temporary file
        logUtils.logFine("Processing part %d of %s", part.getId().getSequenceNumber(), part.getId().getFileId());
        handler.processingPart(part.getId().getSequenceNumber(), part.getId().getFileId(), sender.getIdentifier());
        Joiner joiner = new Joiner(incomingFile.getPath());
        joiner.putPart(part);
        joiner.close();

        // send confirmation
        ConfOutMessage conf = new ConfOutMessage(session, sender, part.getId(), transport.requiresArmoredMessages());
        transport.sendMessage(conf);

        // remove pending part to receive
        new PartToReceivePO(db, part.getId().getSequenceNumber(), part.getId().getFileId(), sender.getIdentifier()).delete();

        IncomingFilePO.markFilesAsComplete(db);
        incomingFile.refreshCompleteStatus();
        if (incomingFile.isComplete()) {

            try {

                // move file to incoming directory
                String newPath = String.format("%s%s%s",
                        db.getProperties().get(DBUtils.INCOMING_DIRECTORY), File.separator, part.getId().getFileId());
                Files.move(
                        Paths.get(String.format("%s%s%s%s", incomingTempPath, File.separator, part.getId().getFileId(),
                                TEMP_FILE_EXTENSION)), Paths.get(newPath)
                );

                // update DB path
                incomingFile.setPath(newPath);
                incomingFile.save();

                logUtils.logFine("File %s complete", part.getId().getFileId());
                handler.fileComplete(part.getId().getFileId(), sender.getIdentifier(), newPath);

            } catch (IOException ex) {

                logUtils.logError(ex);
                handler.errorsWhileProcessingReceivedMessage(ex.getMessage());
            }
        }

        // message processed successfully
        return true;
    }

    private boolean processConfInMessage(ConfInMessage message) throws AuroraException {

        // confirm part in DB
        PartId partId = message.getData();
        PublicKeys sender = PublicKeysUtils.get(db, message.getSender().getPublicKey());

        logUtils.logFine("Processing confirmation %d of %s", partId.getSequenceNumber(), partId.getFileId());
        handler.processingConfirmation(partId.getSequenceNumber(), partId.getFileId(), sender.getIdentifier());
        new PartToSendPO(db, partId.getSequenceNumber(), partId.getFileId(), sender.getIdentifier()).delete();

        OutgoingFilePO.markFilesAsComplete(db);

        // message processed successfully
        return true;
    }

    private boolean processPublicKeysInMessage(PublicKeysInMessage message) throws AuroraException {

        if (handler.publicKeysReceived(message.getData().getIdentifier())) {

            PublicKeysUtils.store(db, message.getData());

            handler.keysStored(message.getData().getIdentifier());
        }

        // remove keys message in any case
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

            PublicKeysUtils.store(db, keys);

            handler.keysStored(keys.getIdentifier());

            PublicKeysOutMessage km = new PublicKeysOutMessage(session, keys, session.getPublicKeys(),
                    transport.requiresArmoredMessages());
            transport.sendMessage(km);

        } catch (AuroraException ex) {

            handler.errorsWhileProcessingKeyMessage("Wrong password");
        }

        // remove key message in any case
        return true;
    }

    public DBUtils getDBUtils() {

        return db;
    }
}
