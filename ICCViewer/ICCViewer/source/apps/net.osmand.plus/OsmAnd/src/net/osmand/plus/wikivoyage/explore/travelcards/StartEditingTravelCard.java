package net.osmand.plus.wikivoyage.explore.travelcards;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.wikipedia.WikipediaDialogFragment;

public class StartEditingTravelCard extends BaseTravelCard {

	public static final int TYPE = 1;

	private Context context;

	public StartEditingTravelCard(OsmandApplication app, Activity context, boolean nightMode) {
		super(app, nightMode);
		this.context = context;
	}

	@Override
	public void bindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
		if (viewHolder instanceof StartEditingTravelVH) {
			final StartEditingTravelVH holder = (StartEditingTravelVH) viewHolder;
			holder.title.setText(R.string.start_editing_card_image_text);
			holder.description.setText(R.string.start_editing_card_description);
			holder.backgroundImage.setImageResource(R.drawable.img_help_wikivoyage_contribute);
			holder.button.setText(R.string.start_editing);
			holder.button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WikipediaDialogFragment.showFullArticle(context,
							Uri.parse("https://" + app.getLanguage().toLowerCase() + ".m.wikivoyage.org"), nightMode);
				}
			});
		}
	}

	public static class StartEditingTravelVH extends RecyclerView.ViewHolder {

		final TextView title;
		final TextView description;
		final TextView button;
		final ImageView backgroundImage;

		public StartEditingTravelVH(final View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);
			description = (TextView) itemView.findViewById(R.id.description);
			button = (TextView) itemView.findViewById(R.id.bottom_button_text);
			backgroundImage = (ImageView) itemView.findViewById(R.id.background_image);
		}
	}

	@Override
	public int getCardType() {
		return TYPE;
	}
}
