package net.osmand.plus.dialogs;


import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.base.bottomsheetmenu.simpleitems.TitleItem;
import net.osmand.plus.quickaction.CreateEditActionDialog;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.quickaction.QuickActionFactory;
import net.osmand.plus.quickaction.QuickActionRegistry;
import net.osmand.plus.quickaction.SwitchableAction;
import net.osmand.plus.quickaction.actions.MapStyleAction;
import net.osmand.plus.quickaction.actions.MapSourceAction;
import net.osmand.plus.quickaction.actions.MapOverlayAction;
import net.osmand.plus.quickaction.actions.MapUnderlayAction;
import net.osmand.plus.render.RendererRegistry;
import net.osmand.render.RenderingRulesStorage;

import java.util.List;

public class SelectMapViewQuickActionsBottomSheet extends MenuBottomSheetDialogFragment {

	public static final String TAG = SelectMapViewQuickActionsBottomSheet.class.getSimpleName();

	private static final String SELECTED_ITEM_KEY = "selected_item";

	private LinearLayout itemsContainer;
	private View.OnClickListener onClickListener;
	private ColorStateList rbColorList;

	private String selectedItem;
	private QuickAction action;

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		Bundle args = getArguments();
		if (args == null) {
			return;
		}
		MapActivity mapActivity = getMapActivity();
		if (mapActivity == null) {
			return;
		}
		long id = args.getLong(SwitchableAction.KEY_ID);
		OsmandApplication app = mapActivity.getMyApplication();

		QuickActionRegistry quickActionRegistry = app.getQuickActionRegistry();
		action = quickActionRegistry.getQuickAction(id);
		action = QuickActionFactory.produceAction(action);
		if (action == null) {
			return;
		}
		OsmandSettings settings = app.getSettings();
		if (savedInstanceState != null) {
			selectedItem = savedInstanceState.getString(SELECTED_ITEM_KEY);
		} else {
			if (action instanceof MapStyleAction) {
				RenderingRulesStorage current = app.getRendererRegistry().getCurrentSelectedRenderer();
				if (current != null) {
					selectedItem = current.getName();
				} else {
					selectedItem = RendererRegistry.DEFAULT_RENDER;
				}
			} else if (action instanceof MapSourceAction) {
				selectedItem = settings.MAP_ONLINE_DATA.get()
						? settings.MAP_TILE_SOURCES.get()
						: MapSourceAction.LAYER_OSM_VECTOR;
			} else if (action instanceof MapUnderlayAction) {
				selectedItem = settings.MAP_UNDERLAY.get();
			} else if (action instanceof MapOverlayAction) {
				selectedItem = settings.MAP_OVERLAY.get();
			}
		}
		rbColorList = AndroidUtils.createCheckedColorStateList(app, R.color.icon_color_default_light, getActiveColorId());

		items.add(new TitleItem(action.getName(app)));

		NestedScrollView nestedScrollView = new NestedScrollView(app);
		itemsContainer = new LinearLayout(app);
		itemsContainer.setLayoutParams((new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)));
		itemsContainer.setOrientation(LinearLayout.VERTICAL);
		int padding = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_content_padding_small);
		itemsContainer.setPadding(0, padding, 0, padding);

		int itemsSize = 0;
		if (action instanceof SwitchableAction) {
			SwitchableAction switchableAction = (SwitchableAction) action;
			List sources = switchableAction.loadListFromParams();
			itemsSize = sources.size();
		}
		for (int i = 0; i < itemsSize; i++) {
			LayoutInflater.from(new ContextThemeWrapper(app, themeRes))
					.inflate(R.layout.bottom_sheet_item_with_radio_btn, itemsContainer, true);
		}

		nestedScrollView.addView(itemsContainer);
		items.add(new BaseBottomSheetItem.Builder().setCustomView(nestedScrollView).create());

		populateItemsList();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(SELECTED_ITEM_KEY, selectedItem);
	}

	@Override
	protected int getRightBottomButtonTextId() {
		return R.string.shared_string_close;
	}

	@Override
	protected void onRightBottomButtonClick() {
		dismiss();
	}

	@Override
	protected int getDismissButtonTextId() {
		return R.string.quick_action_edit_actions;
	}

	@Override
	protected void onDismissButtonClickAction() {
		FragmentManager fm = getFragmentManager();
		if (fm == null) {
			return;
		}
		CreateEditActionDialog dialog = CreateEditActionDialog.newInstance(action.getId());
		dialog.show(fm, CreateEditActionDialog.TAG);
	}

	@Override
	protected boolean useScrollableItemsContainer() {
		return false;
	}

	@Nullable
	private MapActivity getMapActivity() {
		Activity activity = getActivity();
		if (activity != null && activity instanceof MapActivity) {
			return (MapActivity) activity;
		}
		return null;
	}

	private void populateItemsList() {
		Context context = getContext();
		if (context == null) {
			return;
		}
		int counter = 0;
		if (action instanceof MapStyleAction) {
			MapStyleAction mapStyleAction = (MapStyleAction) action;
			List<String> stylesList = mapStyleAction.getFilteredStyles();
			for (String entry : stylesList) {
				boolean selected = entry.equals(selectedItem);
				createItemRow(selected, counter, mapStyleAction.getTranslatedItemName(context, entry), entry);
				counter++;
			}
		} else if (action instanceof SwitchableAction) {
			SwitchableAction switchableAction = (SwitchableAction) action;
			List<Pair<String, String>> sources = (List<Pair<String, String>>) switchableAction.loadListFromParams();
			for (Pair<String, String> entry : sources) {
				String tag = entry.first;
				boolean selected = tag.equals(selectedItem);
				createItemRow(selected, counter, entry.second, tag);
				counter++;
			}
		}
	}

	private void createItemRow(boolean selected, int counter, String text, String tag) {
		View view = itemsContainer.getChildAt(counter);
		view.setTag(tag);
		view.setOnClickListener(getOnClickListener());

		TextView titleTv = (TextView) view.findViewById(R.id.title);
		titleTv.setText(text);
		titleTv.setTextColor(getStyleTitleColor(selected));

		RadioButton rb = (RadioButton) view.findViewById(R.id.compound_button);
		rb.setChecked(selected);
		CompoundButtonCompat.setButtonTintList(rb, rbColorList);
		ImageView imageView = (ImageView) view.findViewById(R.id.icon);
		imageView.setImageDrawable(getContentIcon(action.getIconRes()));
	}

	@ColorInt
	private int getStyleTitleColor(boolean selected) {
		int colorId = selected
				? getActiveColorId()
				: nightMode ? R.color.text_color_primary_dark : R.color.text_color_primary_light;
		return getResolvedColor(colorId);
	}

	private View.OnClickListener getOnClickListener() {
		if (onClickListener == null) {
			onClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MapActivity mapActivity = getMapActivity();
					if (mapActivity == null) {
						return;
					}
					selectedItem = (String) v.getTag();
					if (action instanceof SwitchableAction) {
						((SwitchableAction) action).executeWithParams(mapActivity, selectedItem);
					}
					dismiss();
				}
			};
		}
		return onClickListener;
	}
}