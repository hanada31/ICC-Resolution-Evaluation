package org.smssecure.smssecure.components.reminder;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import org.smssecure.smssecure.ConversationListActivity;
import org.smssecure.smssecure.DatabaseMigrationActivity;
import org.smssecure.smssecure.R;
import org.smssecure.smssecure.service.ApplicationMigrationService;
import org.smssecure.smssecure.crypto.MasterSecret;

public class SystemSmsImportReminder extends Reminder {

  public SystemSmsImportReminder(final Context context, final MasterSecret masterSecret) {
    super(context.getString(R.string.reminder_header_sms_import_title),
          context.getString(R.string.reminder_header_sms_import_text),
          context.getString(R.string.reminder_header_sms_import_button));

    final OnClickListener okListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(context, ApplicationMigrationService.class);
        intent.setAction(ApplicationMigrationService.MIGRATE_DATABASE);
        intent.putExtra("master_secret", masterSecret);
        context.startService(intent);

        Intent nextIntent = new Intent(context, ConversationListActivity.class);
        intent.putExtra("master_secret", masterSecret);

        Intent activityIntent = new Intent(context, DatabaseMigrationActivity.class);
        activityIntent.putExtra("master_secret", masterSecret);
        activityIntent.putExtra("next_intent", nextIntent);
        context.startActivity(activityIntent);
      }
    };
    final OnClickListener cancelListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        ApplicationMigrationService.setDatabaseImported(context);
      }
    };
    setOkListener(okListener);
    setDismissListener(cancelListener);
  }

  public static boolean isEligible(Context context) {
    return !ApplicationMigrationService.isDatabaseImported(context);
  }
}
