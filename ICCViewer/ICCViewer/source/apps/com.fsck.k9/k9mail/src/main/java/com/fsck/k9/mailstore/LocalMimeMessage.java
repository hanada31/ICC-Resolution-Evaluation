package com.fsck.k9.mailstore;


import com.fsck.k9.mail.internet.MimeMessage;


public class LocalMimeMessage extends MimeMessage implements LocalPart {
    private final String accountUuid;
    private final LocalMessage message;
    private final long messagePartId;

    public LocalMimeMessage(String accountUuid, LocalMessage message, long messagePartId) {
        super();
        this.accountUuid = accountUuid;
        this.message = message;
        this.messagePartId = messagePartId;
    }

    @Override
    public String getAccountUuid() {
        return accountUuid;
    }

    @Override
    public long getPartId() {
        return messagePartId;
    }

    @Override
    public LocalMessage getMessage() {
        return message;
    }
}
