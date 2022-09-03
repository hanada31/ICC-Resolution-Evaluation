/*
 * Copyright (c) 2018.
 *
 * This file is part of MoneyWallet.
 *
 * MoneyWallet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MoneyWallet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MoneyWallet.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.oriondev.moneywallet.ui.fragment.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.ui.activity.NewEditPlaceActivity;
import com.oriondev.moneywallet.ui.adapter.recycler.PlaceSelectorCursorAdapter;
import com.oriondev.moneywallet.ui.view.theme.ThemedDialog;

/**
 * Created by andre on 20/03/2018.
 */
public class PlacePickerDialog extends DialogFragment implements PlaceSelectorCursorAdapter.Controller, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SS_SELECTED_PLACE = "PlacePickerDialog::SavedState::SelectedPlace";

    private static final int DEFAULT_LOADER_ID = 1;

    public static PlacePickerDialog newInstance() {
        return new PlacePickerDialog();
    }

    private Place mPlace;

    private Callback mCallback;

    private RecyclerView mRecyclerView;
    private TextView mMessageTextView;

    private PlaceSelectorCursorAdapter mCursorAdapter;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        if (savedInstanceState != null) {
            mPlace = savedInstanceState.getParcelable(SS_SELECTED_PLACE);
        }
        MaterialDialog dialog = ThemedDialog.buildMaterialDialog(activity)
                .title(R.string.dialog_place_picker_title)
                .positiveText(R.string.action_new)
                .negativeText(android.R.string.cancel)
                .customView(R.layout.dialog_advanced_list, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        startActivity(new Intent(getActivity(), NewEditPlaceActivity.class));
                    }

                })
                .build();
        mCursorAdapter = new PlaceSelectorCursorAdapter(this);
        View view = dialog.getCustomView();
        if (view != null) {
            mRecyclerView = view.findViewById(R.id.recycler_view);
            mMessageTextView = view.findViewById(R.id.message_text_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            mRecyclerView.setAdapter(mCursorAdapter);
            mMessageTextView.setText(R.string.message_no_place_found);
        }
        mRecyclerView.setVisibility(View.GONE);
        mMessageTextView.setVisibility(View.GONE);
        getLoaderManager().restartLoader(DEFAULT_LOADER_ID, null, this);
        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_SELECTED_PLACE, mPlace);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void showPicker(FragmentManager fragmentManager, String tag, Place place) {
        mPlace = place;
        show(fragmentManager, tag);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Activity activity = getActivity();
        if (activity != null) {
            Uri uri = DataContentProvider.CONTENT_PLACES;
            String[] projection = new String[]{
                    Contract.Place.ID,
                    Contract.Place.NAME,
                    Contract.Place.ICON,
                    Contract.Place.ADDRESS,
                    Contract.Place.LATITUDE,
                    Contract.Place.LONGITUDE
            };
            String sortOrder = Contract.Place.NAME;
            return new CursorLoader(activity, uri, projection, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        if (data != null && data.getCount() > 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mMessageTextView.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onPlaceSelected(Place place) {
        mCallback.onPlaceSelected(place);
        dismiss();
    }

    @Override
    public boolean isPlaceSelected(long id) {
        return mPlace != null && mPlace.getId() == id;
    }

    public interface Callback {

        void onPlaceSelected(Place place);
    }
}