package org.smssecure.smssecure.database.loaders;

import android.content.Context;
import android.database.Cursor;

import org.smssecure.smssecure.database.DatabaseFactory;
import org.smssecure.smssecure.util.AbstractCursorLoader;

public class BlockedContactsLoader extends AbstractCursorLoader {

  public BlockedContactsLoader(Context context) {
    super(context);
  }

  @Override
  public Cursor getCursor() {
    return DatabaseFactory.getRecipientPreferenceDatabase(getContext())
                          .getBlocked();
  }

}
