package com.fsck.k9.mailstore;


public interface LocalPart {
    String getAccountUuid();
    long getPartId();
    long getSize();
    LocalMessage getMessage();
}
