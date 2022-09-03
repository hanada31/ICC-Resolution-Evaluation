package org.smssecure.smssecure.mms;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.smssecure.smssecure.attachments.Attachment;
import org.smssecure.smssecure.database.ThreadDatabase;
import org.smssecure.smssecure.recipients.Recipients;
import org.smssecure.smssecure.util.Base64;
import org.whispersystems.textsecure.internal.push.TextSecureProtos.GroupContext;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class OutgoingGroupMediaMessage extends OutgoingSecureMediaMessage {

  private final GroupContext group;

  public OutgoingGroupMediaMessage(@NonNull Recipients recipients,
                                   @NonNull String encodedGroupContext,
                                   @NonNull List<Attachment> avatar,
                                   long sentTimeMillis)
      throws IOException
  {
    super(recipients, encodedGroupContext, avatar, sentTimeMillis,
          ThreadDatabase.DistributionTypes.CONVERSATION);

    this.group = GroupContext.parseFrom(Base64.decode(encodedGroupContext));
  }

  public OutgoingGroupMediaMessage(@NonNull Recipients recipients,
                                   @NonNull GroupContext group,
                                   @Nullable final Attachment avatar)
  {
    super(recipients, Base64.encodeBytes(group.toByteArray()),
          new LinkedList<Attachment>() {{if (avatar != null) add(avatar);}},
          System.currentTimeMillis(),
          ThreadDatabase.DistributionTypes.CONVERSATION);

    this.group = group;
  }

  @Override
  public boolean isGroup() {
    return true;
  }

  public boolean isGroupUpdate() {
    return group.getType().getNumber() == GroupContext.Type.UPDATE_VALUE;
  }

  public boolean isGroupQuit() {
    return group.getType().getNumber() == GroupContext.Type.QUIT_VALUE;
  }

  public GroupContext getGroupContext() {
    return group;
  }
}
