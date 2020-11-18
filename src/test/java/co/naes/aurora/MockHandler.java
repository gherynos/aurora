package co.naes.aurora;

public class MockHandler implements Messenger.StatusHandler {

    private char[] passwordSent;

    private char[] passwordReceived;

    private String keyMessage = "";

    private boolean errorsWhileSendingMessages = false;

    private boolean errorsWhileReceivingMessages = false;

    @Override
    public void self(Messenger messenger) {

    }

    @Override
    public void sendingPart(int sequenceNumber, String fileId, Identifier identifier) {

    }

    @Override
    public void unableToSendPart(int sequenceNumber, String fileId, Identifier identifier) {

        errorsWhileSendingMessages = true;
    }

    @Override
    public void processingPart(int sequenceNumber, String fileId, Identifier identifier) {

    }

    @Override
    public void discardedPart(int sequenceNumber, String fileId, Identifier identifier) {

    }

    @Override
    public void processingConfirmation(int sequenceNumber, String fileId, Identifier identifier) {

    }

    @Override
    public void errorsWhileSendingMessages(String message) {

        errorsWhileSendingMessages = true;
    }

    @Override
    public void errorsWhileReceivingMessages(String message) {

        errorsWhileReceivingMessages = true;
    }

    @Override
    public void errorsWhileProcessingReceivedMessage(String message) {

        errorsWhileReceivingMessages = true;
    }

    @Override
    public void errorsWhileProcessingKeyMessage(String message) {

        errorsWhileReceivingMessages = true;

        keyMessage = message;
    }

    @Override
    public void fileComplete(String fileId, Identifier identifier, String path) {

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
    public void keysStored(Identifier identifier) {

    }

    @Override
    public boolean publicKeysReceived(Identifier identifier) {

        return true;
    }

    protected char[] getPasswordSent() {

        return passwordSent;
    }

    protected void setPasswordReceived(char[] password) {

        passwordReceived = password;
    }

    protected String getKeyMessage() {

        return keyMessage;
    }

    protected void resetErrors() {

        errorsWhileSendingMessages = false;
        errorsWhileReceivingMessages = false;
    }

    protected boolean hasErrorsWhileSendingMessages() {

        return errorsWhileSendingMessages;
    }

    protected boolean hasErrorsWhileReceivingMessages() {

        return errorsWhileReceivingMessages;
    }
}
