/**
 * Copyright (C) 2015 Open Whisper Systems
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
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.smssecure.smssecure.ImageMediaAdapter.ViewHolder;
import org.smssecure.smssecure.components.ThumbnailView;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.CursorRecyclerViewAdapter;
import org.smssecure.smssecure.database.ImageDatabase.ImageRecord;
import org.smssecure.smssecure.mms.Slide;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.util.MediaUtil;

public class ImageMediaAdapter extends CursorRecyclerViewAdapter<ViewHolder> {
  private static final String TAG = ImageMediaAdapter.class.getSimpleName();

  private final MasterSecret masterSecret;
  private final long         threadId;

  public static class ViewHolder extends RecyclerView.ViewHolder {
    public ThumbnailView imageView;

    public ViewHolder(View v) {
      super(v);
      imageView = (ThumbnailView) v.findViewById(R.id.image);
    }
  }

  public ImageMediaAdapter(Context context, MasterSecret masterSecret, Cursor c, long threadId) {
    super(context, c);
    this.masterSecret = masterSecret;
    this.threadId     = threadId;
  }

  @Override
  public ViewHolder onCreateItemViewHolder(final ViewGroup viewGroup, final int i) {
    final View view = LayoutInflater.from(getContext()).inflate(R.layout.media_overview_item, viewGroup, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindItemViewHolder(final ViewHolder viewHolder, final @NonNull Cursor cursor) {
    final ThumbnailView imageView   = viewHolder.imageView;
    final ImageRecord imageRecord = ImageRecord.from(cursor);

    Slide slide = MediaUtil.getSlideForAttachment(getContext(), imageRecord.getAttachment());

    if (slide != null) {
      imageView.setImageResource(masterSecret, slide, false);
    }

    imageView.setOnClickListener(new OnMediaClickListener(imageRecord));
  }

  private class OnMediaClickListener implements OnClickListener {
    private final ImageRecord imageRecord;

    private OnMediaClickListener(ImageRecord imageRecord) {
      this.imageRecord = imageRecord;
    }

    @Override
    public void onClick(View v) {
      Intent intent = new Intent(getContext(), MediaPreviewActivity.class);
      intent.putExtra(MediaPreviewActivity.DATE_EXTRA, imageRecord.getDate());
      intent.putExtra(MediaPreviewActivity.THREAD_ID_EXTRA, threadId);

      if (!TextUtils.isEmpty(imageRecord.getAddress())) {
        Recipients recipients = RecipientFactory.getRecipientsFromString(getContext(),
                                                                         imageRecord.getAddress(),
                                                                         true);
        if (recipients != null && recipients.getPrimaryRecipient() != null) {
          intent.putExtra(MediaPreviewActivity.RECIPIENT_EXTRA, recipients.getPrimaryRecipient().getRecipientId());
        }
      }
      intent.setDataAndType(imageRecord.getAttachment().getDataUri(), imageRecord.getContentType());
      getContext().startActivity(intent);

    }
  }
}
