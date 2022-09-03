package net.osmand.plus.settings;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.map.ITileSource;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.ApplicationMode.ApplicationModeBean;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities;
import net.osmand.plus.helpers.AvoidSpecificRoads.AvoidRoadInfo;
import net.osmand.plus.poi.PoiUIFilter;
import net.osmand.plus.profiles.ProfileIconColors;
import net.osmand.plus.quickaction.QuickAction;
import net.osmand.plus.render.RenderingIcons;
import net.osmand.util.Algorithms;


import java.io.File;
import java.util.List;

public class DuplicatesSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int HEADER_TYPE = 0;
	private static final int ITEM_TYPE = 1;

	private boolean nightMode;
	private OsmandApplication app;
	private UiUtilities uiUtilities;
	private List<? super Object> items;

	DuplicatesSettingsAdapter(OsmandApplication app, List<? super Object> items, boolean nightMode) {
		this.app = app;
		this.items = items;
		this.nightMode = nightMode;
		this.uiUtilities = app.getUIUtilities();
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(app);
		if (viewType == HEADER_TYPE) {
			View view = inflater.inflate(R.layout.list_item_header_import, parent, false);
			return new HeaderViewHolder(view);
		} else {
			View view = inflater.inflate(R.layout.list_item_import, parent, false);
			return new ItemViewHolder(view);
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		Object currentItem = items.get(position);
		if (holder instanceof HeaderViewHolder) {
			HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
			headerHolder.title.setText((String) currentItem);
			headerHolder.subTitle.setText(String.format(
					app.getString(R.string.listed_exist),
					(String) currentItem));
			headerHolder.divider.setVisibility(View.VISIBLE);
		} else if (holder instanceof ItemViewHolder) {
			ItemViewHolder itemHolder = (ItemViewHolder) holder;
			if (currentItem instanceof ApplicationModeBean) {
				ApplicationModeBean modeBean = (ApplicationModeBean) currentItem;
				String profileName = modeBean.userProfileName;
				if (Algorithms.isEmpty(profileName)) {
					ApplicationMode appMode = ApplicationMode.valueOfStringKey(modeBean.stringKey, null);
					profileName = app.getString(appMode.getNameKeyResource());
				}
				itemHolder.title.setText(profileName);
				String routingProfile = modeBean.routingProfile;
				if (Algorithms.isEmpty(routingProfile)) {
					itemHolder.subTitle.setVisibility(View.GONE);
				} else {
					itemHolder.subTitle.setText(String.format(
							app.getString(R.string.ltr_or_rtl_combine_via_colon),
							app.getString(R.string.nav_type_hint),
							routingProfile));
					itemHolder.subTitle.setVisibility(View.VISIBLE);
				}
				int profileIconRes = AndroidUtils.getDrawableId(app, modeBean.iconName);
				ProfileIconColors iconColor = modeBean.iconColor;
				itemHolder.icon.setImageDrawable(uiUtilities.getIcon(profileIconRes, iconColor.getColor(nightMode)));
				itemHolder.icon.setVisibility(View.VISIBLE);
			} else if (currentItem instanceof QuickAction) {
				QuickAction action = (QuickAction) currentItem;
				itemHolder.title.setText(action.getName(app));
				itemHolder.icon.setImageDrawable(uiUtilities.getIcon(action.getIconRes(), nightMode));
				itemHolder.subTitle.setVisibility(View.GONE);
				itemHolder.icon.setVisibility(View.VISIBLE);
			} else if (currentItem instanceof PoiUIFilter) {
				PoiUIFilter filter = (PoiUIFilter) currentItem;
				itemHolder.title.setText(filter.getName());
				int iconRes = RenderingIcons.getBigIconResourceId(filter.getIconId());
				itemHolder.icon.setImageDrawable(uiUtilities.getIcon(iconRes != 0 ? iconRes : R.drawable.ic_person, nightMode));
				itemHolder.subTitle.setVisibility(View.GONE);
				itemHolder.icon.setVisibility(View.VISIBLE);
			} else if (currentItem instanceof ITileSource) {
				itemHolder.title.setText(((ITileSource) currentItem).getName());
				itemHolder.icon.setImageResource(R.drawable.ic_action_info_dark);
				itemHolder.subTitle.setVisibility(View.GONE);
				itemHolder.icon.setVisibility(View.INVISIBLE);
			} else if (currentItem instanceof File) {
				File file = (File) currentItem;
				itemHolder.title.setText(file.getName());
				if (file.getName().contains("/rendering/")) {
					itemHolder.icon.setImageDrawable(uiUtilities.getIcon(R.drawable.ic_action_map_style, nightMode));
					itemHolder.icon.setVisibility(View.VISIBLE);
				} else {
					itemHolder.icon.setImageResource(R.drawable.ic_action_info_dark);
					itemHolder.icon.setVisibility(View.INVISIBLE);
				}
				itemHolder.subTitle.setVisibility(View.GONE);
			} else if (currentItem instanceof AvoidRoadInfo) {
				itemHolder.title.setText(((AvoidRoadInfo) currentItem).name);
				itemHolder.icon.setImageDrawable(app.getUIUtilities().getIcon(R.drawable.ic_action_alert, nightMode));
				itemHolder.subTitle.setVisibility(View.GONE);
				itemHolder.icon.setVisibility(View.VISIBLE);
			}
			itemHolder.divider.setVisibility(shouldShowDivider(position) ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	@Override
	public int getItemViewType(int position) {
		if (items.get(position) instanceof String) {
			return HEADER_TYPE;
		} else {
			return ITEM_TYPE;
		}
	}

	private class HeaderViewHolder extends RecyclerView.ViewHolder {
		TextView title;
		TextView subTitle;
		View divider;

		HeaderViewHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			subTitle = itemView.findViewById(R.id.sub_title);
			divider = itemView.findViewById(R.id.top_divider);
		}
	}

	private class ItemViewHolder extends RecyclerView.ViewHolder {
		TextView title;
		TextView subTitle;
		ImageView icon;
		View divider;

		ItemViewHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.title);
			subTitle = itemView.findViewById(R.id.sub_title);
			icon = itemView.findViewById(R.id.icon);
			divider = itemView.findViewById(R.id.bottom_divider);
		}
	}

	private boolean shouldShowDivider(int position) {
		boolean isLast = position == items.size() - 1;
		if (isLast) {
			return true;
		} else {
			Object next = items.get(position + 1);
			return next instanceof String;
		}
	}
}
