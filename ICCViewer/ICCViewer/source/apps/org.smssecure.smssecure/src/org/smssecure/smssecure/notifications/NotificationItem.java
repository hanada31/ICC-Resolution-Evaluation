package org.smssecure.smssecure.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;

import org.smssecure.smssecure.ConversationActivity;
import org.smssecure.smssecure.mms.SlideDeck;
import org.smssecure.smssecure.recipients.Recipient;
import org.smssecure.smssecure.recipients.Recipients;

public class NotificationItem {

  private final @NonNull  Recipients        recipients;
  private final @NonNull  Recipient         individualRecipient;
  private final @Nullable Recipients        threadRecipients;
  private final long                        threadId;
  private final @Nullable CharSequence      text;
  private final long                        timestamp;
  private final @Nullable SlideDeck         slideDeck;

  public NotificationItem(@NonNull   Recipient individualRecipient,
                          @NonNull   Recipients recipients,
                          @Nullable  Recipients threadRecipients,
                          long threadId, @Nullable CharSequence text, long timestamp,
                          @Nullable SlideDeck slideDeck)
  {
    this.individualRecipient = individualRecipient;
    this.recipients          = recipients;
    this.threadRecipients    = threadRecipients;
    this.text                = text;
    this.threadId            = threadId;
    this.timestamp           = timestamp;
    this.slideDeck           = slideDeck;
  }

  public @NonNull  Recipients getRecipients() {
    return threadRecipients == null ? recipients : threadRecipients;
  }

  public @NonNull  Recipient getIndividualRecipient() {
    return individualRecipient;
  }

  public CharSequence getText() {
    return text;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getThreadId() {
    return threadId;
  }

  public @Nullable SlideDeck getSlideDeck() {
    return slideDeck;
  }

  public PendingIntent getPendingIntent(Context context) {
    Intent     intent           = new Intent(context, ConversationActivity.class);
    Recipients notifyRecipients = threadRecipients != null ? threadRecipients : recipients;
    if (notifyRecipients != null) intent.putExtra("recipients", notifyRecipients.getIds());

    intent.putExtra("thread_id", threadId);
    intent.setData((Uri.parse("custom://"+System.currentTimeMillis())));

    return TaskStackBuilder.create(context)
                           .addNextIntentWithParentStack(intent)
                           .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
  }


}
