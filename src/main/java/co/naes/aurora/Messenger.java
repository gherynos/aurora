package co.naes.aurora;

import co.naes.aurora.msg.InMessage;
import co.naes.aurora.msg.in.ConfInMessage;
import co.naes.aurora.msg.in.PartInMessage;
import co.naes.aurora.msg.key.InKeyMessage;
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
import java.util.ArrayList;
import java.util.List;

public class Messenger implements IncomingMessageHandler  {

    private LocalDB db;

    private AuroraTransport transport;

    private AuroraSession session;

    private String incomingTempPath = Main.CONF_FOLDER + File.separator + "incoming";

    private static final int MAX_PARTS_TO_SEND_PER_FILE = 5;

    Messenger(LocalDB db, AuroraTransport transport, AuroraSession session) {

        this.db = db;
        this.transport = transport;
        this.session = session;

        transport.setIncomingMessageHandler(this);

        File iTemp = new File(incomingTempPath);
        if (!iTemp.exists() && !iTemp.mkdirs())
            throw new RuntimeException("Unable to write to conf folder");
    }

    public void addFileToSend(PublicKeys recipient, String filePath) throws AuroraException {

        try {

            String fileId = new File(filePath).getName();
            db.addOutgoingFile(fileId, filePath, recipient.getEmailAddress());

            Splitter sp = new Splitter(fileId, filePath);
            db.addPartsToSend(fileId, sp.getTotalParts());

        } catch (AuroraException ex) {

            if (ex.getCause() instanceof JdbcSQLIntegrityConstraintViolationException) {

                // TODO: log + UI
                System.out.println("File already added");

            } else
                throw ex;
        }
    }

    public void send() {

        try {

            // load pending files
            for (String[] pendingFile : db.getPendingOutputFiles()) {

                String fileId = pendingFile[0];
                String path = pendingFile[1];
                PublicKeys recipient = db.getPublicKeys(pendingFile[2]);

                // load parts to send
                List<Integer> partsToSend = db.getPartsToSend(fileId);
                if (partsToSend.size() > MAX_PARTS_TO_SEND_PER_FILE)
                    partsToSend = partsToSend.subList(0, MAX_PARTS_TO_SEND_PER_FILE);

                // send parts
                Splitter sp = new Splitter(fileId, path);
                List<Integer> sent = new ArrayList<>();
                for (Integer sequenceNumber : partsToSend) {

                    try {

                        // send part message
                        System.out.println("Sending part " + sequenceNumber + " for " + fileId);
                        PartOutMessage msg = new PartOutMessage(session, recipient, sp.getPart(sequenceNumber), true);
                        transport.sendMessage(msg);
                        sent.add(sequenceNumber);

                    } catch (AuroraException ex) {

                        // TODO: log
                    }
                }

                // mark parts as sent
                db.markPartsAsSent(sent, fileId);
            }

        } catch (AuroraException ex) {

            ex.printStackTrace();  // TODO: log + UI
        }
    }

    public void receive() {

        try {

            transport.checkForMessages();

        } catch (AuroraException ex) {

            ex.printStackTrace();  // TODO: log + UI
        }
    }

    @Override
    public boolean messageReceived(InMessage<?> message) {

        try {

            message.decrypt(session);

            if (message instanceof PartInMessage) {

                Part part = ((PartInMessage) message).getData();
                PublicKeys sender = db.getPublicKeys(message.getSender().getPublicKey());

                // check incoming file existence
                String[] incomingFile = db.getIncomingFile(part.getId().getFileId());
                if (incomingFile == null) {

                    incomingFile = new String[3];
                    incomingFile[0] = part.getId().getFileId();
                    incomingFile[1] = incomingTempPath + File.separator + part.getId().getFileId() + ".temp";
                    incomingFile[2] = sender.getEmailAddress();
                    db.addIncomingFile(incomingFile[0], incomingFile[1], incomingFile[2]);

                    // track parts to come
                    db.addPartsToReceive(part.getId().getFileId(), part.getTotal());
                }

                // store part in temporary file
                System.out.println("Processing part " + part.getId().getSequenceNumber() + " of " + part.getId().getFileId());
                Joiner joiner = new Joiner(incomingFile[1]);
                joiner.putPart(part);

                // send confirmation
                ConfOutMessage conf = new ConfOutMessage(session, sender, part.getId(), true);
                transport.sendMessage(conf);

                // remove pending part to receive
                db.deletePartToReceive(part.getId().getSequenceNumber(), part.getId().getFileId());

                // TODO: check file complete

            } else if (message instanceof ConfInMessage) {

                // confirm part in DB
                PartId partId = ((ConfInMessage) message).getData();
                System.out.println("Processing confirmation " + partId.getSequenceNumber() + " of " + partId.getFileId());
                db.deletePartToSend(partId.getSequenceNumber(), partId.getFileId());

            } else {

                // TODO: log unknown type
                return false;
            }

            // message processed successfully
            return true;

        } catch (AuroraException ex) {

            ex.printStackTrace(); // TODO: log + UI
        }

        return false;
    }

    @Override
    public boolean keyMessageReceived(InKeyMessage keyMessage) {

        try {

            char[] password = "aRandomPasswordToGenerate".toCharArray(); // TODO: fix
            PublicKeys keys = keyMessage.getPublicKeys(password);

            db.storePublicKeys(keys);

            // key processed successfully
            return true;

        } catch (AuroraException ex) {

            ex.printStackTrace(); // TODO: log + UI
        }

        return false;
    }
}
