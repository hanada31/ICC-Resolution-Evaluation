package org.smssecure.smssecure;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.smssecure.smssecure.crypto.MasterSecret;
import org.smssecure.smssecure.database.model.ThreadRecord;
import org.smssecure.smssecure.util.ViewUtil;

import java.util.Locale;
import java.util.Set;

public class ConversationListItemAction extends LinearLayout implements BindableConversationListItem {

  private TextView description;

  public ConversationListItemAction(Context context) {
    super(context);
  }

  public ConversationListItemAction(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public ConversationListItemAction(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();
    this.description = ViewUtil.findById(this, R.id.description);
  }

  @Override
  public void bind(@NonNull MasterSecret masterSecret, @NonNull ThreadRecord thread, @NonNull Locale locale, @NonNull Set<Long> selectedThreads, boolean batchMode) {
    this.description.setText(getContext().getString(R.string.ConversationListItemAction_archived_conversations_d, thread.getCount()));
  }

  @Override
  public void unbind() {

  }
}
