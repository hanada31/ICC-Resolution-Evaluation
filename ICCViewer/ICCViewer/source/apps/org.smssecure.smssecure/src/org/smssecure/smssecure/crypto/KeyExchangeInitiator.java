/**
 * Copyright (C) 2011 Whisper Systems
 * Copyright (C) 2013 Open Whisper Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.smssecure.smssecure.crypto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.crypto.storage.SilenceIdentityKeyStore;
import org.smssecure.smssecure.crypto.storage.SilencePreKeyStore;
import org.smssecure.smssecure.crypto.storage.SilenceSessionStore;
import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.sms.MessageSender;
import org.smssecure.smssecure.sms.OutgoingKeyExchangeMessage;
import org.smssecure.smssecure.util.Base64;
import org.smssecure.smssecure.util.ResUtil;
import org.whispersystems.libaxolotl.AxolotlAddress;
import org.whispersystems.libaxolotl.SessionBuilder;
import org.whispersystems.libaxolotl.protocol.KeyExchangeMessage;
import org.whispersystems.libaxolotl.state.IdentityKeyStore;
import org.whispersystems.libaxolotl.state.PreKeyStore;
import org.whispersystems.libaxolotl.state.SessionRecord;
import org.whispersystems.libaxolotl.state.SessionStore;
import org.whispersystems.libaxolotl.state.SignedPreKeyStore;

public class KeyExchangeInitiator {

  public static void initiate(final Context context, final MasterSecret masterSecret, final Recipients recipients, boolean promptOnExisting, final int subscriptionId) {
    if (promptOnExisting && hasInitiatedSession(context, masterSecret, recipients)) {
      AlertDialog.Builder dialog = new AlertDialog.Builder(context);
      dialog.setTitle(R.string.KeyExchangeInitiator_initiate_despite_existing_request_question);
      dialog.setMessage(R.string.KeyExchangeInitiator_youve_already_sent_a_session_initiation_request_to_this_recipient_are_you_sure);
      dialog.setIconAttribute(R.attr.dialog_alert_icon);
      dialog.setCancelable(true);
      dialog.setPositiveButton(R.string.KeyExchangeInitiator_send, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          initiateKeyExchange(context, masterSecret, recipients, subscriptionId);
        }
      });
      dialog.setNegativeButton(android.R.string.cancel, null);
      dialog.show();
    } else {
      initiateKeyExchange(context, masterSecret, recipients, subscriptionId);
    }
  }

  private static void initiateKeyExchange(Context context, MasterSecret masterSecret, Recipients recipients, int subscriptionId) {
    Recipient         recipient         = recipients.getPrimaryRecipient();
    SessionStore      sessionStore      = new SilenceSessionStore(context, masterSecret);
    PreKeyStore       preKeyStore       = new SilencePreKeyStore(context, masterSecret);
    SignedPreKeyStore signedPreKeyStore = new SilencePreKeyStore(context, masterSecret);
    IdentityKeyStore  identityKeyStore  = new SilenceIdentityKeyStore(context, masterSecret);

    SessionBuilder    sessionBuilder    = new SessionBuilder(sessionStore, preKeyStore, signedPreKeyStore,
                                                             identityKeyStore, new AxolotlAddress(recipient.getNumber(), 1));

    KeyExchangeMessage         keyExchangeMessage = sessionBuilder.process();
    String                     serializedMessage  = Base64.encodeBytesWithoutPadding(keyExchangeMessage.serialize());
    OutgoingKeyExchangeMessage textMessage        = new OutgoingKeyExchangeMessage(recipients, serializedMessage, subscriptionId);

    MessageSender.send(context, masterSecret, textMessage, -1, false);
  }

  private static boolean hasInitiatedSession(Context context, MasterSecret masterSecret,
                                             Recipients recipients)
  {
    Recipient     recipient     = recipients.getPrimaryRecipient();
    SessionStore  sessionStore  = new SilenceSessionStore(context, masterSecret);
    SessionRecord sessionRecord = sessionStore.loadSession(new AxolotlAddress(recipient.getNumber(), 1));

    return sessionRecord.getSessionState().hasPendingKeyExchange();
  }
}
