package org.smssecure.smssecure.mms;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import org.smssecure.smssecure.mms.ContactPhotoUriLoader.ContactPhotoUri;

import java.io.InputStream;

public class ContactPhotoUriLoader implements StreamModelLoader<ContactPhotoUri> {
  private final Context context;

  /**
   * THe default factory for {@link com.bumptech.glide.load.model.stream.StreamUriLoader}s.
   */
  public static class Factory implements ModelLoaderFactory<ContactPhotoUri, InputStream> {

    @Override
    public StreamModelLoader<ContactPhotoUri> build(Context context, GenericLoaderFactory factories) {
      return new ContactPhotoUriLoader(context);
    }

    @Override
    public void teardown() {
      // Do nothing.
    }
  }

  public ContactPhotoUriLoader(Context context) {
    this.context = context;
  }

  @Override
  public DataFetcher<InputStream> getResourceFetcher(ContactPhotoUri model, int width, int height) {
    return new ContactPhotoLocalUriFetcher(context, model.uri);
  }

  public static class ContactPhotoUri {
    public @NonNull Uri uri;

    public ContactPhotoUri(@NonNull Uri uri) {
      this.uri = uri;
    }
  }
}

