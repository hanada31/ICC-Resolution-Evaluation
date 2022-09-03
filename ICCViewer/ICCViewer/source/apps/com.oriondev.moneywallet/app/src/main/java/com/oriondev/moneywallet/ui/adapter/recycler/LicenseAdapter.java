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

package com.oriondev.moneywallet.ui.adapter.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.model.License;

import java.util.List;

/**
 * Created by andrea on 31/03/18.
 */
public class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.ViewHolder> {

    private final Controller mController;
    private List<License> mLicenses;

    public LicenseAdapter(Controller controller) {
        mController = controller;
    }

    public void setLicenses(List<License> licenses) {
        mLicenses = licenses;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.adapter_license_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        License license = mLicenses.get(position);
        holder.mPrimaryTextView.setText(license.getName());
        holder.mSecondaryTextView.setText(license.getTypeName());
    }

    @Override
    public int getItemCount() {
        return mLicenses!= null ? mLicenses.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mPrimaryTextView;
        private TextView mSecondaryTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mPrimaryTextView = itemView.findViewById(R.id.primary_text_view);
            mSecondaryTextView = itemView.findViewById(R.id.secondary_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mController != null) {
                mController.onLicenseClick(mLicenses.get(getAdapterPosition()));
            }
        }
    }

    public interface Controller {

        void onLicenseClick(License license);
    }
}