package org.smssecure.smssecure.crypto;

import android.content.Context;
import android.support.annotation.NonNull;

import org.smssecure.smssecure.crypto.storage.SilenceSessionStore;
import org.smssecure.smssecure.recipients.Recipient;
import org.whispersystems.libaxolotl.AxolotlAddress;
import org.whispersystems.libaxolotl.state.SessionStore;

public class SessionUtil {

  public static boolean hasSession(Context context, MasterSecret masterSecret, Recipient recipient) {
    return hasSession(context, masterSecret, recipient.getNumber());
  }

  public static boolean hasSession(Context context, MasterSecret masterSecret, @NonNull String number) {
    SessionStore   sessionStore   = new SilenceSessionStore(context, masterSecret);
    AxolotlAddress axolotlAddress = new AxolotlAddress(number, 1);

    return sessionStore.containsSession(axolotlAddress);
  }
}
