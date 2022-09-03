package com.fsck.k9.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import timber.log.Timber;

import com.fsck.k9.mailstore.StorageManager;

/**
 * That BroadcastReceiver is only interested in MOUNT events.
 */
public class StorageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        final Uri uri = intent.getData();

        if (uri == null || uri.getPath() == null) {
            return;
        }

        Timber.v("StorageReceiver: %s", intent);

        final String path = uri.getPath();

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            StorageManager.getInstance(context).onMount(path,
                    intent.getBooleanExtra("read-only", true));
        }
    }

}
