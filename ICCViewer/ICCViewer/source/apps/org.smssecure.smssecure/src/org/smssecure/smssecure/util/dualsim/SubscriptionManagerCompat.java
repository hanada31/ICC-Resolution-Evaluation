package org.smssecure.smssecure.util.dualsim;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import org.whispersystems.libaxolotl.util.guava.Optional;

import java.util.LinkedList;
import java.util.List;

public class SubscriptionManagerCompat {

  private final Context context;

  public SubscriptionManagerCompat(Context context) {
    this.context = context.getApplicationContext();
  }

  public Optional<SubscriptionInfoCompat> getActiveSubscriptionInfo(int subscriptionId) {
    if (Build.VERSION.SDK_INT < 22) {
      return Optional.absent();
    }

    SubscriptionInfo subscriptionInfo = SubscriptionManager.from(context).getActiveSubscriptionInfo(subscriptionId);

    if (subscriptionInfo != null) {
      return Optional.of(new SubscriptionInfoCompat(subscriptionId, subscriptionInfo.getDisplayName()));
    } else {
      return Optional.absent();
    }
  }

  public @NonNull List<SubscriptionInfoCompat> getActiveSubscriptionInfoList() {
    if (Build.VERSION.SDK_INT < 22) {
      return new LinkedList<>();
    }

    List<SubscriptionInfo> subscriptionInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();

    if (subscriptionInfos == null || subscriptionInfos.isEmpty()) {
      return new LinkedList<>();
    }

    List<SubscriptionInfoCompat> compatList = new LinkedList<>();

    for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
      compatList.add(new SubscriptionInfoCompat(subscriptionInfo.getSubscriptionId(),
                                                subscriptionInfo.getDisplayName()));
    }

    return compatList;
  }

  public static Optional<Integer> getDefaultMessagingSubscriptionId() {
    if (Build.VERSION.SDK_INT < 22) {
      return Optional.absent();
    }
    if(SmsManager.getDefaultSmsSubscriptionId() < 0) {
      return Optional.absent();
    }

    return Optional.of(SmsManager.getDefaultSmsSubscriptionId());
  }

}
