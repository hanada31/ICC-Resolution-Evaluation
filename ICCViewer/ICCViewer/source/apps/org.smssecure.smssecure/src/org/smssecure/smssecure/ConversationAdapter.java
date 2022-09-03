/**
 * Copyright (C) 2011 Whisper Systems
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
package org.smssecure.smssecure;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import org.smssecure.smssecure.util.Conversions;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.CursorRecyclerViewAdapter;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.MmsSmsColumns;
import org.smssecure.smssecure.database.MmsSmsDatabase;
import org.smssecure.smssecure.database.SmsDatabase;
import org.smssecure.smssecure.database.model.MediaMmsMessageRecord;
import org.smssecure.smssecure.database.model.MessageRecord;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.util.DateUtils;
import org.smssecure.smssecure.util.LRUCache;
import org.smssecure.smssecure.util.StickyHeaderDecoration;
import org.smssecure.smssecure.util.Util;
import org.smssecure.smssecure.ConversationAdapter.HeaderViewHolder;

import java.lang.ref.SoftReference;
import java.util.Calendar;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.smssecure.smssecure.util.ViewUtil;
import org.smssecure.smssecure.util.VisibleForTesting;

/**
 * A cursor adapter for a conversation thread.  Ultimately
 * used by ComposeMessageActivity to display a conversation
 * thread in a ListActivity.
 *
 * @author Moxie Marlinspike
 *
 */
public class ConversationAdapter <V extends View & BindableConversationItem>
    extends CursorRecyclerViewAdapter<ConversationAdapter.ViewHolder>
  implements StickyHeaderDecoration.StickyHeaderAdapter<HeaderViewHolder>
{

  private static final int MAX_CACHE_SIZE = 40;
  private final Map<String,SoftReference<MessageRecord>> messageRecordCache =
      Collections.synchronizedMap(new LRUCache<String, SoftReference<MessageRecord>>(MAX_CACHE_SIZE));

  private static final int MESSAGE_TYPE_OUTGOING       = 0;
  private static final int MESSAGE_TYPE_INCOMING       = 1;
  private static final int MESSAGE_TYPE_UPDATE         = 2;
  private static final int MESSAGE_TYPE_AUDIO_OUTGOING = 3;
  private static final int MESSAGE_TYPE_AUDIO_INCOMING = 4;

  private final Set<MessageRecord> batchSelected = Collections.synchronizedSet(new HashSet<MessageRecord>());

  private final @Nullable ItemClickListener clickListener;
  private final @NonNull  MasterSecret      masterSecret;
  private final @NonNull  Locale            locale;
  private final @NonNull  Recipients        recipients;
  private final @NonNull  MmsSmsDatabase    db;
  private final @NonNull  LayoutInflater    inflater;
  private final @NonNull  Calendar          calendar;
  private final @NonNull  MessageDigest     digest;

  protected static class ViewHolder extends RecyclerView.ViewHolder {
    public <V extends View & BindableConversationItem> ViewHolder(final @NonNull V itemView) {
      super(itemView);
    }

    @SuppressWarnings("unchecked")
    public <V extends View & BindableConversationItem> V getView() {
      return (V)itemView;
    }
  }

  protected static class HeaderViewHolder extends RecyclerView.ViewHolder {
    protected TextView textView;

    public HeaderViewHolder(View itemView) {
      super(itemView);
      textView = ViewUtil.findById(itemView, R.id.text);
    }

    public HeaderViewHolder(TextView textView) {
      super(textView);
      this.textView = textView;
    }

    public void setText(CharSequence text) {
      textView.setText(text);
    }
  }

  public interface ItemClickListener {
    void onItemClick(ConversationItem item);
    void onItemLongClick(ConversationItem item);
  }

  @SuppressWarnings("ConstantConditions")
  @VisibleForTesting
  ConversationAdapter(Context context, Cursor cursor) {
    super(context, cursor);
    try {
      this.masterSecret  = null;
      this.locale        = null;
      this.clickListener = null;
      this.recipients    = null;
      this.inflater      = null;
      this.db            = null;
      this.calendar      = Calendar.getInstance();
      this.digest        = MessageDigest.getInstance("SHA1");
    } catch (NoSuchAlgorithmException nsae) {
      throw new AssertionError("SHA1 isn't supported!");
    }
  }

  public ConversationAdapter(@NonNull Context context,
                             @NonNull MasterSecret masterSecret,
                             @NonNull Locale locale,
                             @Nullable ItemClickListener clickListener,
                             @Nullable Cursor cursor,
                             @NonNull Recipients recipients)
  {
    super(context, cursor);
    try {
      this.masterSecret  = masterSecret;
      this.locale        = locale;
      this.clickListener = clickListener;
      this.recipients    = recipients;
      this.inflater      = LayoutInflater.from(context);
      this.db            = DatabaseFactory.getMmsSmsDatabase(context);
      this.calendar      = Calendar.getInstance();
      this.digest        = MessageDigest.getInstance("SHA1");

      setHasStableIds(true);
    } catch (NoSuchAlgorithmException nsae) {
      throw new AssertionError("SHA1 isn't supported!");
    }
  }

  @Override
  public void changeCursor(Cursor cursor) {
    messageRecordCache.clear();
    super.changeCursor(cursor);
  }

  @Override
  public void onBindItemViewHolder(ViewHolder viewHolder, @NonNull Cursor cursor) {
    long          start         = System.currentTimeMillis();
    MessageRecord messageRecord = getMessageRecord(cursor);

    viewHolder.getView().bind(masterSecret, messageRecord, locale, batchSelected, recipients);
  }

  @Override
  public ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
    final V itemView = ViewUtil.inflate(inflater, parent, getLayoutForViewType(viewType));
    if (viewType == MESSAGE_TYPE_INCOMING || viewType == MESSAGE_TYPE_OUTGOING) {
      itemView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View view) {
          if (clickListener != null) clickListener.onItemClick((ConversationItem)itemView);
        }
      });
      itemView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          if (clickListener != null) clickListener.onItemLongClick((ConversationItem)itemView);
          return true;
        }
      });
    }

    return new ViewHolder(itemView);
  }

  @Override
  public void onItemViewRecycled(ViewHolder holder) {
    holder.getView().unbind();
  }

  private @LayoutRes int getLayoutForViewType(int viewType) {
    switch (viewType) {
      case MESSAGE_TYPE_AUDIO_OUTGOING:
      case MESSAGE_TYPE_OUTGOING:        return R.layout.conversation_item_sent;
      case MESSAGE_TYPE_AUDIO_INCOMING:
      case MESSAGE_TYPE_INCOMING:        return R.layout.conversation_item_received;
      case MESSAGE_TYPE_UPDATE:          return R.layout.conversation_item_update;
      default: throw new IllegalArgumentException("unsupported item view type given to ConversationAdapter");
    }
  }

  @Override
  public int getItemViewType(@NonNull Cursor cursor) {
    MessageRecord messageRecord = getMessageRecord(cursor);

    if (messageRecord.isGroupAction() || messageRecord.isEndSession()) {
      return MESSAGE_TYPE_UPDATE;
    } else if (hasAudio(messageRecord)) {
      if (messageRecord.isOutgoing()) return MESSAGE_TYPE_AUDIO_OUTGOING;
      else                            return MESSAGE_TYPE_AUDIO_INCOMING;
    } else if (messageRecord.isOutgoing()) {
      return MESSAGE_TYPE_OUTGOING;
    } else {
      return MESSAGE_TYPE_INCOMING;
    }
}

  @Override
  public long getItemId(@NonNull Cursor cursor) {
    final String unique = cursor.getString(cursor.getColumnIndexOrThrow(MmsSmsColumns.UNIQUE_ROW_ID));
    final byte[] bytes  = digest.digest(unique.getBytes());
    return Conversions.byteArrayToLong(bytes);
  }

  private MessageRecord getMessageRecord(Cursor cursor) {
    long   messageId = cursor.getLong(cursor.getColumnIndexOrThrow(MmsSmsColumns.ID));
    String type      = cursor.getString(cursor.getColumnIndexOrThrow(MmsSmsDatabase.TRANSPORT));

    final SoftReference<MessageRecord> reference = messageRecordCache.get(type + messageId);
    if (reference != null) {
      final MessageRecord record = reference.get();
      if (record != null) return record;
    }

    final MessageRecord messageRecord = db.readerFor(cursor, masterSecret).getCurrent();
    messageRecordCache.put(type + messageId, new SoftReference<>(messageRecord));

    return messageRecord;
  }

  public void close() {
    getCursor().close();
  }

  public void toggleSelection(MessageRecord messageRecord) {
    if (!batchSelected.remove(messageRecord)) {
      batchSelected.add(messageRecord);
    }
  }

  public void clearSelection() {
    batchSelected.clear();
  }

  public Set<MessageRecord> getSelectedItems() {
    return Collections.unmodifiableSet(new HashSet<>(batchSelected));
  }

  private boolean hasAudio(MessageRecord messageRecord) {
    return messageRecord.isMms() &&
        !messageRecord.isMmsNotification() &&
        ((MediaMmsMessageRecord)messageRecord).getSlideDeck().getAudioSlide() != null;
  }

  @Override
  public long getHeaderId(int position) {
    if (!isActiveCursor())          return -1;
    if (isHeaderPosition(position)) return -1;
    if (isFooterPosition(position)) return -1;
    if (position >= getItemCount()) return -1;
    if (position < 0)               return -1;

    Cursor        cursor = getCursorAtPositionOrThrow(position);
    MessageRecord record = getMessageRecord(cursor);

    calendar.setTime(new Date(record.getDateSent()));
    return Util.hashCode(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR));
  }

  @Override
  public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
    return new HeaderViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.conversation_item_header, parent, false));
  }

  @Override
  public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
    Cursor cursor = getCursorAtPositionOrThrow(position);
    viewHolder.setText(DateUtils.getRelativeDate(getContext(), locale, getMessageRecord(cursor).getDateReceived()));
  }
}
