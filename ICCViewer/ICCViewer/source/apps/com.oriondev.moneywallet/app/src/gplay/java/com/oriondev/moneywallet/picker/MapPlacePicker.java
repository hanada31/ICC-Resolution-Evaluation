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

package com.oriondev.moneywallet.picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.oriondev.moneywallet.model.Place;

/**
 * Created by andrea on 16/10/18.
 */

public class MapPlacePicker extends Fragment {

    private static final String SS_CURRENT_PLACE = "MapPlacePicker::SavedState::CurrentPlace";

    private static final String ARG_DEFAULT_PLACE = "MapPlacePicker::Arguments::DefaultPlace";

    private static final int REQUEST_PLACE_PICKER = 132;

    private Controller mController;

    private Place mCurrentPlace;

    public static MapPlacePicker createPicker(FragmentManager fragmentManager, String tag, Place defaultPlace) {
        MapPlacePicker mapPlacePicker = (MapPlacePicker) fragmentManager.findFragmentByTag(tag);
        if (mapPlacePicker == null) {
            mapPlacePicker = new MapPlacePicker();
            Bundle arguments = new Bundle();
            arguments.putParcelable(ARG_DEFAULT_PLACE, defaultPlace);
            mapPlacePicker.setArguments(arguments);
            fragmentManager.beginTransaction().add(mapPlacePicker, tag).commit();
        }
        return mapPlacePicker;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof MapPlacePicker.Controller) {
            mController = (MapPlacePicker.Controller) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPlace = savedInstanceState.getParcelable(SS_CURRENT_PLACE);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mCurrentPlace = arguments.getParcelable(ARG_DEFAULT_PLACE);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireCallbackSafely();
    }

    public void fireCallbackSafely() {
        if (mController != null) {
            mController.onMapPlaceChanged(getTag(), mCurrentPlace);
        }
    }

    private String getDialogTag() {
        return getTag() + "::DialogFragment";
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SS_CURRENT_PLACE, mCurrentPlace);
    }

    public boolean isSelected() {
        return mCurrentPlace != null;
    }

    public void setCurrentPlace(Place place) {
        mCurrentPlace = place;
        fireCallbackSafely();
    }

    public Place getCurrentPlace() {
        return mCurrentPlace;
    }

    public void showPicker() {
        Activity activity = getActivity();
        if (activity != null) {
            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent = builder.build(getActivity());
                startActivityForResult(intent, REQUEST_PLACE_PICKER);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            Activity activity = getActivity();
            if (activity != null && resultCode == Activity.RESULT_OK) {
                com.google.android.gms.location.places.Place place = com.google.android.gms.location.places.ui.PlacePicker.getPlace(activity, data);
                String name = place.getName().toString();
                String address = place.getAddress() != null ? place.getAddress().toString() : null;
                LatLng coordinates = place.getLatLng();
                mCurrentPlace = new Place(0, name, null, address, coordinates.latitude, coordinates.longitude);
                fireCallbackSafely();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface Controller {

        void onMapPlaceChanged(String tag, Place place);
    }
}