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

package com.oriondev.moneywallet.ui.activity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.oriondev.moneywallet.R;
import com.oriondev.moneywallet.broadcast.RecurrenceBroadcastReceiver;
import com.oriondev.moneywallet.model.Category;
import com.oriondev.moneywallet.model.CurrencyUnit;
import com.oriondev.moneywallet.model.Event;
import com.oriondev.moneywallet.model.Place;
import com.oriondev.moneywallet.model.RecurrenceSetting;
import com.oriondev.moneywallet.model.Wallet;
import com.oriondev.moneywallet.picker.CategoryPicker;
import com.oriondev.moneywallet.picker.EventPicker;
import com.oriondev.moneywallet.picker.MoneyPicker;
import com.oriondev.moneywallet.picker.PlacePicker;
import com.oriondev.moneywallet.picker.RecurrencePicker;
import com.oriondev.moneywallet.picker.WalletPicker;
import com.oriondev.moneywallet.storage.database.Contract;
import com.oriondev.moneywallet.storage.database.DataContentProvider;
import com.oriondev.moneywallet.storage.preference.PreferenceManager;
import com.oriondev.moneywallet.ui.view.text.MaterialEditText;
import com.oriondev.moneywallet.ui.view.text.Validator;
import com.oriondev.moneywallet.utils.CurrencyManager;
import com.oriondev.moneywallet.utils.DateUtils;
import com.oriondev.moneywallet.utils.IconLoader;
import com.oriondev.moneywallet.utils.MoneyFormatter;

import java.util.Date;

/**
 * Created by andrea on 06/11/18.
 */
public class NewEditRecurrentTransactionActivity extends NewEditItemActivity implements MoneyPicker.Controller,
                                                                                        CategoryPicker.Controller,
                                                                                        WalletPicker.SingleWalletController,
                                                                                        RecurrencePicker.Controller,
                                                                                        EventPicker.Controller,
                                                                                        PlacePicker.Controller {

    private static final String TAG_MONEY_PICKER = "NewEditRecurrentTransactionActivity::Tag::MoneyPicker";
    private static final String TAG_CATEGORY_PICKER = "NewEditRecurrentTransactionActivity::Tag::CategoryPicker";
    private static final String TAG_WALLET_PICKER = "NewEditRecurrentTransactionActivity::Tag::WalletPicker";
    private static final String TAG_RECURRENCE_PICKER = "NewEditRecurrentTransactionActivity::Tag::RecurrencePicker";
    private static final String TAG_EVENT_PICKER = "NewEditRecurrentTransactionActivity::Tag::EventPicker";
    private static final String TAG_PLACE_PICKER = "NewEditRecurrentTransactionActivity::Tag::PlacePicker";

    private TextView mCurrencyTextView;
    private TextView mMoneyTextView;
    private MaterialEditText mDescriptionEditText;
    private MaterialEditText mCategoryEditText;
    private MaterialEditText mWalletEditText;
    private MaterialEditText mRecurrenceEditText;
    private MaterialEditText mEventEditText;
    private MaterialEditText mPlaceEditText;
    private MaterialEditText mNoteEditText;
    private CheckBox mConfirmedCheckBox;
    private CheckBox mCountInTotalCheckBox;

    private MoneyPicker mMoneyPicker;
    private CategoryPicker mCategoryPicker;
    private WalletPicker mWalletPicker;
    private RecurrencePicker mRecurrencePicker;
    private EventPicker mEventPicker;
    private PlacePicker mPlacePicker;

    private MoneyFormatter mMoneyFormatter = MoneyFormatter.getInstance();
    
    @Override
    protected void onCreateHeaderView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_header_new_edit_money_item, parent, true);
        mCurrencyTextView = view.findViewById(R.id.currency_text_view);
        mMoneyTextView = view.findViewById(R.id.money_text_view);
        // attach a listener to the views
        mMoneyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mMoneyPicker.showPicker();
            }

        });
    }

    @Override
    protected void onCreatePanelView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_panel_new_edit_recurrent_transaction, parent, true);
        mDescriptionEditText = view.findViewById(R.id.description_edit_text);
        mCategoryEditText = view.findViewById(R.id.category_edit_text);
        mWalletEditText = view.findViewById(R.id.wallet_edit_text);
        mRecurrenceEditText = view.findViewById(R.id.recurrence_edit_text);
        mEventEditText = view.findViewById(R.id.event_edit_text);
        mPlaceEditText = view.findViewById(R.id.place_edit_text);
        mNoteEditText = view.findViewById(R.id.note_edit_text);
        mConfirmedCheckBox = view.findViewById(R.id.confirmed_checkbox);
        mCountInTotalCheckBox = view.findViewById(R.id.count_in_total_checkbox);
        // disable unused edit texts
        mCategoryEditText.setTextViewMode(true);
        mWalletEditText.setTextViewMode(true);
        mRecurrenceEditText.setTextViewMode(true);
        mEventEditText.setTextViewMode(true);
        mPlaceEditText.setTextViewMode(true);
        // add validators
        mCategoryEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_category);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mCategoryPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        mWalletEditText.addValidator(new Validator() {

            @NonNull
            @Override
            public String getErrorMessage() {
                return getString(R.string.error_input_missing_wallet);
            }

            @Override
            public boolean isValid(@NonNull CharSequence charSequence) {
                return mWalletPicker.isSelected();
            }

            @Override
            public boolean autoValidate() {
                return false;
            }

        });
        // attach listeners
        mCategoryEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCategoryPicker.showPicker();
            }

        });
        mWalletEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mWalletPicker.showSingleWalletPicker();
            }

        });
        mRecurrenceEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRecurrencePicker.showPicker();
            }

        });
        mEventEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEventPicker.showPicker(null);
            }

        });
        mEventEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mEventPicker.setCurrentEvent(null);
                return false;
            }

        });
        mPlaceEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mPlacePicker.showPicker();
            }

        });
        mPlaceEditText.setOnCancelButtonClickListener(new MaterialEditText.CancelButtonListener() {

            @Override
            public boolean onCancelButtonClick(@NonNull MaterialEditText materialEditText) {
                mPlacePicker.setCurrentPlace(null);
                return false;
            }

        });
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState) {
        super.onViewCreated(savedInstanceState);
        long money = 0L;
        Category category = null;
        Wallet wallet = null;
        RecurrenceSetting recurrenceSetting = null;
        Event event = null;
        Place place = null;
        if (savedInstanceState == null) {
            ContentResolver contentResolver = getContentResolver();
            if (getMode() == Mode.EDIT_ITEM) {
                Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_RECURRENT_TRANSACTIONS, getItemId());
                String[] projection = new String[] {
                        Contract.RecurrentTransaction.MONEY,
                        Contract.RecurrentTransaction.DESCRIPTION,
                        Contract.RecurrentTransaction.CATEGORY_ID,
                        Contract.RecurrentTransaction.CATEGORY_NAME,
                        Contract.RecurrentTransaction.CATEGORY_ICON,
                        Contract.RecurrentTransaction.CATEGORY_TYPE,
                        Contract.RecurrentTransaction.CATEGORY_SHOW_REPORT,
                        Contract.RecurrentTransaction.DIRECTION,
                        Contract.RecurrentTransaction.WALLET_ID,
                        Contract.RecurrentTransaction.WALLET_NAME,
                        Contract.RecurrentTransaction.WALLET_ICON,
                        Contract.RecurrentTransaction.WALLET_CURRENCY,
                        Contract.RecurrentTransaction.PLACE_ID,
                        Contract.RecurrentTransaction.PLACE_NAME,
                        Contract.RecurrentTransaction.PLACE_ICON,
                        Contract.RecurrentTransaction.PLACE_ADDRESS,
                        Contract.RecurrentTransaction.PLACE_LATITUDE,
                        Contract.RecurrentTransaction.PLACE_LONGITUDE,
                        Contract.RecurrentTransaction.NOTE,
                        Contract.RecurrentTransaction.EVENT_ID,
                        Contract.RecurrentTransaction.EVENT_NAME,
                        Contract.RecurrentTransaction.EVENT_ICON,
                        Contract.RecurrentTransaction.EVENT_START_DATE,
                        Contract.RecurrentTransaction.EVENT_END_DATE,
                        Contract.RecurrentTransaction.CONFIRMED,
                        Contract.RecurrentTransaction.COUNT_IN_TOTAL,
                        Contract.RecurrentTransaction.START_DATE,
                        Contract.RecurrentTransaction.RULE
                };
                Cursor cursor = contentResolver.query(uri, projection, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        money = cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.MONEY));
                        mDescriptionEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.DESCRIPTION)));
                        category = new Category(
                                cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.CATEGORY_ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.CATEGORY_NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.CATEGORY_ICON))),
                                Contract.CategoryType.fromValue(cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransaction.CATEGORY_TYPE)))
                        );
                        wallet = new Wallet(
                                cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.WALLET_ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.WALLET_NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.WALLET_ICON))),
                                CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.WALLET_CURRENCY))),
                                0L, 0L
                        );
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_ID))) {
                            place = new Place(
                                    cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_ICON))),
                                    cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_ADDRESS)),
                                    cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_LATITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_LATITUDE)),
                                    cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_LONGITUDE)) ? null : cursor.getDouble(cursor.getColumnIndex(Contract.RecurrentTransaction.PLACE_LONGITUDE))
                            );
                        }
                        mNoteEditText.setText(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.NOTE)));
                        if (!cursor.isNull(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_ID))) {
                            event = new Event(
                                    cursor.getLong(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_ID)),
                                    cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_NAME)),
                                    IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_ICON))),
                                    DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_START_DATE))),
                                    DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.EVENT_END_DATE)))
                            );
                        }
                        mConfirmedCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransaction.CONFIRMED)) == 1);
                        mCountInTotalCheckBox.setChecked(cursor.getInt(cursor.getColumnIndex(Contract.RecurrentTransaction.COUNT_IN_TOTAL)) == 1);
                        Date startDate = DateUtils.getDateFromSQLDateString(cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.START_DATE)));
                        String rule = cursor.getString(cursor.getColumnIndex(Contract.RecurrentTransaction.RULE));
                        recurrenceSetting = new RecurrenceSetting(startDate, rule);
                    }
                    cursor.close();
                }
            } else {
                String[] projection = new String[] {
                        Contract.Wallet.ID,
                        Contract.Wallet.NAME,
                        Contract.Wallet.ICON,
                        Contract.Wallet.CURRENCY,
                        Contract.Wallet.START_MONEY,
                        Contract.Wallet.TOTAL_MONEY
                };
                long currentWallet = PreferenceManager.getCurrentWallet();
                Cursor cursor;
                if (currentWallet == PreferenceManager.TOTAL_WALLET_ID) {
                    Uri uri = DataContentProvider.CONTENT_WALLETS;
                    cursor = contentResolver.query(uri, projection, null, null, null);
                } else {
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_WALLETS, currentWallet);
                    cursor = contentResolver.query(uri, projection, null, null, null);
                }
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        wallet = new Wallet(
                                cursor.getLong(cursor.getColumnIndex(Contract.Wallet.ID)),
                                cursor.getString(cursor.getColumnIndex(Contract.Wallet.NAME)),
                                IconLoader.parse(cursor.getString(cursor.getColumnIndex(Contract.Wallet.ICON))),
                                CurrencyManager.getCurrency(cursor.getString(cursor.getColumnIndex(Contract.Wallet.CURRENCY))),
                                cursor.getLong(cursor.getColumnIndex(Contract.Wallet.START_MONEY)),
                                cursor.getLong(cursor.getColumnIndex(Contract.Wallet.TOTAL_MONEY))
                        );
                    }
                    cursor.close();
                }
                recurrenceSetting = new RecurrenceSetting(new Date(), RecurrenceSetting.TYPE_DAILY);
            }
        }
        // now we can create pickers with default values or existing item parameters
        // and update all the views according to the data
        FragmentManager fragmentManager = getSupportFragmentManager();
        mMoneyPicker = MoneyPicker.createPicker(fragmentManager, TAG_MONEY_PICKER, null, money);
        mCategoryPicker = CategoryPicker.createPicker(fragmentManager, TAG_CATEGORY_PICKER, category);
        mWalletPicker = WalletPicker.createPicker(fragmentManager, TAG_WALLET_PICKER, wallet);
        mRecurrencePicker = RecurrencePicker.createPicker(fragmentManager, TAG_RECURRENCE_PICKER, recurrenceSetting);
        mEventPicker = EventPicker.createPicker(fragmentManager, TAG_EVENT_PICKER, event);
        mPlacePicker = PlacePicker.createPicker(fragmentManager, TAG_PLACE_PICKER, place);
    }

    @Override
    protected int getActivityTileRes(Mode mode) {
        switch (mode) {
            case NEW_ITEM:
                return R.string.title_activity_new_recurrence;
            case EDIT_ITEM:
                return R.string.title_activity_edit_recurrence;
            default:
                return -1;
        }
    }

    private boolean validate() {
        return mCategoryEditText.validate() && mWalletEditText.validate();
    }

    @Override
    protected void onSaveChanges(Mode mode) {
        if (validate()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.RecurrentTransaction.MONEY, mMoneyPicker.getCurrentMoney());
            contentValues.put(Contract.RecurrentTransaction.DESCRIPTION, mDescriptionEditText.getTextAsString());
            contentValues.put(Contract.RecurrentTransaction.CATEGORY_ID, mCategoryPicker.getCurrentCategory().getId());
            contentValues.put(Contract.RecurrentTransaction.DIRECTION, mCategoryPicker.getCurrentCategory().getDirection());
            contentValues.put(Contract.RecurrentTransaction.WALLET_ID, mWalletPicker.getCurrentWallet().getId());
            contentValues.put(Contract.RecurrentTransaction.PLACE_ID, mPlacePicker.isSelected() ? mPlacePicker.getCurrentPlace().getId() : null);
            contentValues.put(Contract.RecurrentTransaction.EVENT_ID, mEventPicker.isSelected() ? mEventPicker.getCurrentEvent().getId() : null);
            contentValues.put(Contract.RecurrentTransaction.NOTE, mNoteEditText.getTextAsString());
            contentValues.put(Contract.RecurrentTransaction.CONFIRMED, mConfirmedCheckBox.isChecked());
            contentValues.put(Contract.RecurrentTransaction.COUNT_IN_TOTAL, mCountInTotalCheckBox.isChecked());
            contentValues.put(Contract.RecurrentTransaction.START_DATE, DateUtils.getSQLDateString(mRecurrencePicker.getCurrentSettings().getStartDate()));
            contentValues.put(Contract.RecurrentTransaction.RULE, mRecurrencePicker.getCurrentSettings().getRule());
            ContentResolver contentResolver = getContentResolver();
            switch (mode) {
                case NEW_ITEM:
                    contentResolver.insert(DataContentProvider.CONTENT_RECURRENT_TRANSACTIONS, contentValues);
                    break;
                case EDIT_ITEM:
                    Uri uri = ContentUris.withAppendedId(DataContentProvider.CONTENT_RECURRENT_TRANSACTIONS, getItemId());
                    contentResolver.update(uri, contentValues, null, null);
                    break;
            }
            RecurrenceBroadcastReceiver.scheduleRecurrenceTask(this);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onMoneyChanged(String tag, CurrencyUnit currency, long money) {
        if (currency != null) {
            mCurrencyTextView.setText(currency.getSymbol());
        } else {
            mCurrencyTextView.setText("?");
        }
        mMoneyTextView.setText(mMoneyFormatter.getNotTintedString(currency, money, MoneyFormatter.CurrencyMode.ALWAYS_HIDDEN));
    }

    @Override
    public void onCategoryChanged(String tag, Category category) {
        if (category != null) {
            mCategoryEditText.setText(category.getName());
        } else {
            mCategoryEditText.setText(null);
        }
    }

    @Override
    public void onWalletChanged(String tag, Wallet wallet) {
        if (wallet != null) {
            mWalletEditText.setText(wallet.getName());
            mMoneyPicker.setCurrency(wallet.getCurrency());
        } else {
            mWalletEditText.setText(null);
            mMoneyPicker.setCurrency(null);
        }
    }

    @Override
    public void onRecurrenceSettingChanged(String tag, RecurrenceSetting recurrenceSetting) {
        mRecurrenceEditText.setText(recurrenceSetting.getUserReadableString(this));
    }

    @Override
    public void onEventChanged(String tag, Event event) {
        if (event != null) {
            mEventEditText.setText(event.getName());
        } else {
            mEventEditText.setText(null);
        }
    }

    @Override
    public void onPlaceChanged(String tag, Place place) {
        if (place != null) {
            mPlaceEditText.setText(place.getName());
        } else {
            mPlaceEditText.setText(null);
        }
    }
}