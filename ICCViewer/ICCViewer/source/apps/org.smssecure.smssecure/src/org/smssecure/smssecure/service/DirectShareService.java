package org.smssecure.smssecure.service;


import android.content.ComponentName;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.chooser.ChooserTarget;
import android.service.chooser.ChooserTargetService;
import android.support.annotation.RequiresApi;

import org.smssecure.smssecure.ShareActivity;
import org.smssecure.smssecure.crypto.MasterCipher;
import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.database.ThreadDatabase;
import org.smssecure.smssecure.database.model.ThreadRecord;
import org.smssecure.smssecure.recipients.RecipientFactory;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.util.BitmapUtil;

import java.util.LinkedList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.M)
public class DirectShareService extends ChooserTargetService {
  @Override
  public List<ChooserTarget> onGetChooserTargets(ComponentName targetActivityName,
                                                 IntentFilter matchedFilter)
  {
    List<ChooserTarget> results        = new LinkedList<>();
    MasterSecret        masterSecret   = KeyCachingService.getMasterSecret(this);

    if (masterSecret == null) {
      return results;
    }

    ComponentName  componentName  = new ComponentName(this, ShareActivity.class);
    ThreadDatabase threadDatabase = DatabaseFactory.getThreadDatabase(this);
    Cursor         cursor         = threadDatabase.getDirectShareList();

    try {
      ThreadDatabase.Reader reader = threadDatabase.readerFor(cursor, new MasterCipher(masterSecret));
      ThreadRecord record;

      while ((record = reader.getNext()) != null && results.size() < 10) {
        Recipients recipients = RecipientFactory.getRecipientsForIds(this, record.getRecipients().getIds(), false);
        String     name       = recipients.toShortString();
        Drawable   drawable   = recipients.getContactPhoto().asDrawable(this, recipients.getColor().toConversationColor(this));
        Bitmap     avatar     = BitmapUtil.createFromDrawable(drawable, 500, 500);

        Bundle bundle = new Bundle();
        bundle.putLong(ShareActivity.EXTRA_THREAD_ID, record.getThreadId());
        bundle.putLongArray(ShareActivity.EXTRA_RECIPIENT_IDS, recipients.getIds());
        bundle.putInt(ShareActivity.EXTRA_DISTRIBUTION_TYPE, record.getDistributionType());

        results.add(new ChooserTarget(name, Icon.createWithBitmap(avatar), 1.0f, componentName, bundle));
      }

      return results;
    } finally {
      if (cursor != null) cursor.close();
    }
  }
}
