package org.smssecure.smssecure;

import android.support.annotation.NonNull;

import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.model.MessageRecord;
import org.smssecure.smssecure.recipients.Recipients;

import java.util.Locale;
import java.util.Set;

public interface BindableConversationItem extends Unbindable {
  void bind(@NonNull MasterSecret masterSecret,
            @NonNull MessageRecord messageRecord,
            @NonNull Locale locale,
            @NonNull Set<MessageRecord> batchSelected,
            @NonNull Recipients recipients);
}
