package co.naes.aurora;

public class MockHandler implements Messenger.StatusHandler {

    private char[] passwordSent;

    private char[] passwordReceived;

    @Override
    public void self(Messenger messenger) {

    }

    @Override
    public void sendingPart(int sequenceNumber, String fileId, String emailAddress) {

    }

    @Override
    public void unableToSendPart(int sequenceNumber, String fileId, String emailAddress) {

    }

    @Override
    public void processingPart(int sequenceNumber, String fileId, String emailAddress) {

    }

    @Override
    public void discardedPart(int sequenceNumber, String fileId, String emailAddress) {

    }

    @Override
    public void processingConfirmation(int sequenceNumber, String fileId, String emailAddress) {

    }

    @Override
    public void errorsWhileSendingMessages(String message) {

    }

    @Override
    public void errorsWhileReceivingMessages(String message) {

    }

    @Override
    public void errorsWhileProcessingReceivedMessage(String message) {

    }

    @Override
    public void errorsWhileProcessingKeyMessage(String message) {

    }

    @Override
    public void fileComplete(String fileId, String emailAddress, String path) {

    }

    @Override
    public char[] keyMessageReceived(String sender) {

        return passwordReceived;
    }

    @Override
    public void keyMessageSent(char[] password) {

        passwordSent = password;
    }

    @Override
    public void keysStored(String emailAddress) {

    }

    protected char[] getPasswordSent() {

        return passwordSent;
    }

    protected void setPasswordReceived(char[] password) {

        passwordReceived = password;
    }
}
