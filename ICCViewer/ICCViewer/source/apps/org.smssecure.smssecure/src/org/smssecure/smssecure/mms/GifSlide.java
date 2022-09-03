package org.smssecure.smssecure.mms;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

import org.smssecure.smssecure.attachments.Attachment;
import org.smssecure.smssecure.crypto.MasterSecret;

import java.io.IOException;
import java.io.InputStream;

import ws.com.google.android.mms.ContentType;
import ws.com.google.android.mms.pdu.PduPart;

public class GifSlide extends ImageSlide {

  public GifSlide(Context context, Attachment attachment) {
    super(context, attachment);
  }

  public GifSlide(Context context, Uri uri, long size) throws IOException {
    super(context, constructAttachmentFromUri(context, uri, ContentType.IMAGE_GIF, size));
  }

  @Override
  @Nullable
  public Uri getThumbnailUri() {
    return getUri();
  }
}
