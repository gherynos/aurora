package co.naes.aurora;

import co.naes.aurora.parts.Splitter;

import java.io.File;

public class Messenger {

    private LocalDB db;

    Messenger(LocalDB db) {

        this.db = db;
    }

    public void addFileToSend(PublicKeys recipient, String filePath) throws AuroraException {

        String fileId = new File(filePath).getName();
        db.addOutgoingFile(fileId, filePath, recipient.getEmailAddress());

        Splitter sp = new Splitter(fileId, filePath);
        db.addPartsToSend(fileId, sp.getTotalParts());
    }
}
