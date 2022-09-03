package org.smssecure.smssecure.mms;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.smssecure.smssecure.transport.UndeliverableMessageException;

import ws.com.google.android.mms.pdu.SendConf;

public interface OutgoingMmsConnection {
  @Nullable SendConf send(@NonNull byte[] pduBytes, int subscriptionId) throws UndeliverableMessageException;
}
