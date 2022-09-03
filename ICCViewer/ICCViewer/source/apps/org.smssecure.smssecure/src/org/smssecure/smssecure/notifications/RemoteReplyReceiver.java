/**
 * Copyright (C) 2016 Open Whisper Systems
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

package org.smssecure.smssecure.notifications;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.RemoteInput;

import org.smssecure.smssecure.attachments.Attachment;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.crypto.SessionUtil;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.RecipientPreferenceDatabase.RecipientsPreferences;
import org.smssecure.smssecure.mms.OutgoingMediaMessage;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.sms.MessageSender;
import org.smssecure.smssecure.sms.OutgoingEncryptedMessage;
import org.smssecure.smssecure.sms.OutgoingTextMessage;
import org.whispersystems.libaxolotl.util.guava.Optional;

import java.util.LinkedList;

/**
 * Get the response text from the Wearable Device and sends an message as a reply
 */
public class RemoteReplyReceiver extends MasterSecretBroadcastReceiver {

  public static final String TAG                 = RemoteReplyReceiver.class.getSimpleName();
  public static final String REPLY_ACTION        = "org.smssecure.smssecure.notifications.WEAR_REPLY";
  public static final String RECIPIENT_IDS_EXTRA = "recipient_ids";

  @Override
  protected void onReceive(final Context context, Intent intent,
                           final @Nullable MasterSecret masterSecret)
  {
    if (!REPLY_ACTION.equals(intent.getAction())) return;

    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

    if (remoteInput == null) return;

    final long[]       recipientIds = intent.getLongArrayExtra(RECIPIENT_IDS_EXTRA);
    final CharSequence responseText = remoteInput.getCharSequence(MessageNotifier.EXTRA_REMOTE_REPLY);
    final Recipients   recipients   = RecipientFactory.getRecipientsForIds(context, recipientIds, false);

    if (masterSecret != null && responseText != null) {
      new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... params) {
          long threadId;

          Optional<RecipientsPreferences> preferences = DatabaseFactory.getRecipientPreferenceDatabase(context).getRecipientsPreferences(recipientIds);
          int subscriptionId = preferences.isPresent() ? preferences.get().getDefaultSubscriptionId().or(-1) : -1;

          if (recipients.isGroupRecipient()) {
            OutgoingMediaMessage reply = new OutgoingMediaMessage(recipients, responseText.toString(), new LinkedList<Attachment>(), System.currentTimeMillis(), subscriptionId, 0);
            threadId = MessageSender.send(context, masterSecret, reply, -1, false);
          } else {
            boolean secure = SessionUtil.hasSession(context, masterSecret, recipients.getPrimaryRecipient());

            OutgoingTextMessage reply;
            if (!secure) {
              reply = new OutgoingTextMessage(recipients, responseText.toString(), subscriptionId);
            } else {
              reply = new OutgoingEncryptedMessage(recipients, responseText.toString(), subscriptionId);
            }

            threadId = MessageSender.send(context, masterSecret, reply, -1, false);
          }

          DatabaseFactory.getThreadDatabase(context).setRead(threadId);
          MessageNotifier.updateNotification(context, masterSecret);

          return null;
        }
      }.execute();
    }

  }
}
