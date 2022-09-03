package org.smssecure.smssecure.components.reminder;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

import org.smssecure.smssecure.R;
import org.smssecure.smssecure.util.SilencePreferences;

public class DeliveryReportsReminder extends Reminder {

  public DeliveryReportsReminder(final Context context) {
    super(context.getString(R.string.reminder_header_delivery_reports_title),
          context.getString(R.string.reminder_header_delivery_reports_text),
          context.getString(R.string.reminder_header_delivery_reports_button));

    final OnClickListener okListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        SilencePreferences.setSmsDeliveryReportsEnabled(context);
        SilencePreferences.setPromptedDeliveryReportsReminder(context);
      }
    };
    final OnClickListener dismissListener = new OnClickListener() {
      @Override
      public void onClick(View v) {
        SilencePreferences.setPromptedDeliveryReportsReminder(context);
      }
    };
    setOkListener(okListener);
    setDismissListener(dismissListener);
  }

  public static boolean isEligible(Context context) {
    return !SilencePreferences.isSmsDeliveryReportsEnabled(context) && !SilencePreferences.hasPromptedDeliveryReportsReminder(context);
  }
}
