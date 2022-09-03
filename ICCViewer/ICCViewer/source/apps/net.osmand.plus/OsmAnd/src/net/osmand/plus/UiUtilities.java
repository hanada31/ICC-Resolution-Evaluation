package net.osmand.plus;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TintableCompoundButton;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.osmand.AndroidUtils;
import net.osmand.Location;
import net.osmand.data.LatLon;
import net.osmand.plus.views.DirectionDrawable;
import net.osmand.plus.widgets.TextViewEx;

import java.util.ArrayList;
import java.util.Locale;

import gnu.trove.map.hash.TLongObjectHashMap;

public class UiUtilities {

	private TLongObjectHashMap<Drawable> drawableCache = new TLongObjectHashMap<>();
	private OsmandApplication app;
	private static final int ORIENTATION_0 = 0;
	private static final int ORIENTATION_90 = 3;
	private static final int ORIENTATION_270 = 1;
	private static final int ORIENTATION_180 = 2;
	private static final int INVALID_ID = -1;

	public enum DialogButtonType {
		PRIMARY,
		SECONDARY,
		STROKED
	}

	public enum CompoundButtonType {
		GLOBAL,
		PROFILE_DEPENDENT,
		TOOLBAR
	}

	public UiUtilities(OsmandApplication app) {
		this.app = app;
	}

	private Drawable getDrawable(@DrawableRes int resId, @ColorRes int clrId) {
		long hash = ((long) resId << 31l) + clrId;
		Drawable d = drawableCache.get(hash);
		if (d == null) {
			d = ContextCompat.getDrawable(app, resId);
			d = DrawableCompat.wrap(d);
			d.mutate();
			if (clrId != 0) {
				DrawableCompat.setTint(d, ContextCompat.getColor(app, clrId));
			}
			drawableCache.put(hash, d);
		}
		return d;
	}

	private Drawable getPaintedDrawable(@DrawableRes int resId, @ColorInt int color) {
		long hash = ((long) resId << 31l) + color;
		Drawable d = drawableCache.get(hash);
		if (d == null) {
			d = ContextCompat.getDrawable(app, resId);
			d = tintDrawable(d, color);

			drawableCache.put(hash, d);
		}
		return d;
	}

	public Drawable getPaintedIcon(@DrawableRes int id, @ColorInt int color) {
		return getPaintedDrawable(id, color);
	}

	public Drawable getIcon(@DrawableRes int id, @ColorRes int colorId) {
		return getDrawable(id, colorId);
	}

	public Drawable getLayeredIcon(@DrawableRes int bgIconId, @DrawableRes int foregroundIconId) {
		return getLayeredIcon(bgIconId, foregroundIconId, 0, 0);
	}

	public Drawable getLayeredIcon(@DrawableRes int bgIconId, @DrawableRes int foregroundIconId,
	                               @ColorRes int bgColorId, @ColorRes int foregroundColorId) {
		Drawable b = getDrawable(bgIconId, bgColorId);
		Drawable f = getDrawable(foregroundIconId, foregroundColorId);
		Drawable[] layers = new Drawable[2];
		layers[0] = b;
		layers[1] = f;
		return new LayerDrawable(layers);
	}

	public Drawable getThemedIcon(@DrawableRes int id) {
		return getDrawable(id, R.color.icon_color_default_light);
	}

	public Drawable getIcon(@DrawableRes int id) {
		return getDrawable(id, 0);
	}

	public Drawable getIcon(@DrawableRes int id, boolean light) {
		return getDrawable(id, light ? R.color.icon_color_default_light : R.color.icon_color_default_dark);
	}

	public Drawable getMapIcon(@DrawableRes int id, boolean light) {
		return getDrawable(id, light ? R.color.icon_color_default_light : 0);
	}

	public static Drawable getSelectableDrawable(Context ctx) {
		int bgResId = AndroidUtils.resolveAttribute(ctx, R.attr.selectableItemBackground);
		if (bgResId != 0) {
			return ContextCompat.getDrawable(ctx, bgResId);
		}
		return null;
	}

	public static Drawable getColoredSelectableDrawable(Context ctx, int color, float alpha) {
		Drawable drawable = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			Drawable bg = getSelectableDrawable(ctx);
			if (bg != null) {
				drawable = tintDrawable(bg, getColorWithAlpha(color, alpha));
			}
		} else {
			drawable = AndroidUtils.createPressedStateListDrawable(new ColorDrawable(Color.TRANSPARENT), new ColorDrawable(getColorWithAlpha(color, alpha)));
		}
		return drawable;
	}

	public static Drawable createTintedDrawable(Context context, @DrawableRes int resId, int color) {
		return tintDrawable(ContextCompat.getDrawable(context, resId), color);
	}

	public static Drawable tintDrawable(Drawable drawable, int color) {
		Drawable coloredDrawable = null;
		if (drawable != null) {
			coloredDrawable = DrawableCompat.wrap(drawable);
			coloredDrawable.mutate();
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && coloredDrawable instanceof RippleDrawable) {
				((RippleDrawable) coloredDrawable).setColor(ColorStateList.valueOf(color));
			} else {
				DrawableCompat.setTint(coloredDrawable, color);
			}
		}

		return coloredDrawable;
	}

	@ColorRes
	public static int getDefaultColorRes(Context context) {
		final OsmandApplication app = (OsmandApplication) context.getApplicationContext();
		boolean light = app.getSettings().isLightContent();
		return light ? R.color.icon_color_default_light : R.color.icon_color_default_dark;
	}

	@ColorInt
	public static int getContrastColor(Context context, @ColorInt int color, boolean transparent) {
		// Counting the perceptive luminance - human eye favors green color...
		double luminance = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
		return luminance < 0.5 ? transparent ? ContextCompat.getColor(context, R.color.color_black_transparent) : Color.BLACK : Color.WHITE;
	}

	@ColorInt
	public static int getColorWithAlpha(@ColorInt int color, float ratio) {
		int newColor = 0;
		int alpha = Math.round(Color.alpha(color) * ratio);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		newColor = Color.argb(alpha, r, g, b);
		return newColor;
	}

	@ColorInt
	public static int mixTwoColors(@ColorInt int color1, @ColorInt int color2, float amount )
	{
		final byte ALPHA_CHANNEL = 24;
		final byte RED_CHANNEL   = 16;
		final byte GREEN_CHANNEL =  8;
		final byte BLUE_CHANNEL  =  0;

		final float inverseAmount = 1.0f - amount;

		int a = ((int)(((float)(color1 >> ALPHA_CHANNEL & 0xff )*amount) +
				((float)(color2 >> ALPHA_CHANNEL & 0xff )*inverseAmount))) & 0xff;
		int r = ((int)(((float)(color1 >> RED_CHANNEL & 0xff )*amount) +
				((float)(color2 >> RED_CHANNEL & 0xff )*inverseAmount))) & 0xff;
		int g = ((int)(((float)(color1 >> GREEN_CHANNEL & 0xff )*amount) +
				((float)(color2 >> GREEN_CHANNEL & 0xff )*inverseAmount))) & 0xff;
		int b = ((int)(((float)(color1 & 0xff )*amount) +
				((float)(color2 & 0xff )*inverseAmount))) & 0xff;

		return a << ALPHA_CHANNEL | r << RED_CHANNEL | g << GREEN_CHANNEL | b << BLUE_CHANNEL;
	}

	public UpdateLocationViewCache getUpdateLocationViewCache(){
		UpdateLocationViewCache uvc = new UpdateLocationViewCache();
		uvc.screenOrientation = getScreenOrientation();
		return uvc;
	}

	public static class UpdateLocationViewCache {
		int screenOrientation;
		public boolean paintTxt = true;
		public int arrowResId;
		public int arrowColor;
		public int textColor;
		public LatLon specialFrom;
	}

	public void updateLocationView(UpdateLocationViewCache cache, ImageView arrow, TextView txt,
			double toLat, double toLon) {
		updateLocationView(cache, arrow, txt, new LatLon(toLat, toLon));
	}
	public void updateLocationView(UpdateLocationViewCache cache, ImageView arrow, TextView txt,
			LatLon toLoc) {
		float[] mes = new float[2];
		boolean stale = false;
		LatLon fromLoc = cache == null ? null : cache.specialFrom;
		boolean useCenter = fromLoc != null;
		Float h = null;
		if (fromLoc == null) {
			Location loc = app.getLocationProvider().getLastKnownLocation();
			h = app.getLocationProvider().getHeading();
			if (loc == null) {
				loc = app.getLocationProvider().getLastStaleKnownLocation();
				stale = true;
			}
			if (loc != null) {
				fromLoc = new LatLon(loc.getLatitude(), loc.getLongitude());
			} else {
				useCenter = true;
				stale = false;
				fromLoc = app.getMapViewTrackingUtilities().getMapLocation();
				h = app.getMapViewTrackingUtilities().getMapRotate();
				if(h != null) {
					h = -h;
				}
			}
		}
		if (fromLoc != null && toLoc != null) {
			Location.distanceBetween(toLoc.getLatitude(), toLoc.getLongitude(), fromLoc.getLatitude(),
					fromLoc.getLongitude(), mes);
		}

		if (arrow != null) {
			boolean newImage = false;
			int arrowResId = cache == null ? 0 : cache.arrowResId;
			if (arrowResId == 0) {
				arrowResId = R.drawable.ic_direction_arrow;
			}
			DirectionDrawable dd;
			if (!(arrow.getDrawable() instanceof DirectionDrawable)) {
				newImage = true;
				dd = new DirectionDrawable(app, arrow.getWidth(), arrow.getHeight());
			} else {
				dd = (DirectionDrawable) arrow.getDrawable();
			}
			int imgColorSet = cache == null ? 0 : cache.arrowColor;
			if (imgColorSet == 0) {
				imgColorSet = useCenter ? R.color.color_distance : R.color.color_myloc_distance;
				if (stale) {
					imgColorSet = R.color.icon_color_default_light;
				}
			}
			dd.setImage(arrowResId, imgColorSet);
			if (fromLoc == null || h == null || toLoc == null) {
				dd.setAngle(0);
			} else {
				float orientation = (cache == null ? 0 : cache.screenOrientation) ;
				dd.setAngle(mes[1] - h + 180 + orientation);
			}
			if (newImage) {
				arrow.setImageDrawable(dd);
			}
			arrow.invalidate();
		}
		if (txt != null) {
			if (fromLoc != null && toLoc != null) {
				if (cache.paintTxt) {
					int textColorSet = cache.textColor;
					if (textColorSet == 0) {
						textColorSet = useCenter ? R.color.color_distance : R.color.color_myloc_distance;
						if (stale) {
							textColorSet = R.color.icon_color_default_light;
						}
					}
					txt.setTextColor(app.getResources().getColor(textColorSet));
				}
				txt.setText(OsmAndFormatter.getFormattedDistance(mes[0], app));
			} else {
				txt.setText("");
			}
		}
	}

	public int getScreenOrientation() {
		int screenOrientation = ((WindowManager) app.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		switch (screenOrientation) {
			case ORIENTATION_0:   // Device default (normally portrait)
				screenOrientation = 0;
				break;
			case ORIENTATION_90:  // Landscape right
				screenOrientation = 90;
				break;
			case ORIENTATION_270: // Landscape left
				screenOrientation = 270;
				break;
			case ORIENTATION_180: // Upside down
				screenOrientation = 180;
				break;
		}
		//Looks like screenOrientation correction must not be applied for devices without compass?
		Sensor compass = ((SensorManager) app.getSystemService(Context.SENSOR_SERVICE)).getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if (compass == null) {
			screenOrientation = 0;
		}
		return screenOrientation;
	}
	
	public static void setupSnackbar(Snackbar snackbar, boolean nightMode) {
		setupSnackbar(snackbar, nightMode, null, null, null, null);
	}
	
	public static void setupSnackbar(Snackbar snackbar, boolean nightMode, Integer maxLines) {
		setupSnackbar(snackbar, nightMode, null, null, null, maxLines);
	}
	
	public static void setupSnackbar(Snackbar snackbar, boolean nightMode, @ColorRes Integer backgroundColor,
	                                 @ColorRes Integer messageColor, @ColorRes Integer actionColor, Integer maxLines) {
		if (snackbar == null) {
			return;
		}
		View view = snackbar.getView();
		Context ctx = view.getContext();
		TextView tvMessage = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
		TextView tvAction = (TextView) view.findViewById(android.support.design.R.id.snackbar_action);
		if (messageColor == null) {
			messageColor = nightMode ? R.color.text_color_primary_dark : R.color.text_color_primary_light;
		}
		tvMessage.setTextColor(ContextCompat.getColor(ctx, messageColor));
		if (actionColor == null) {
			actionColor = nightMode ? R.color.active_color_primary_dark : R.color.active_color_primary_light;
		}
		tvAction.setTextColor(ContextCompat.getColor(ctx, actionColor));
		if (maxLines != null) {
			tvMessage.setMaxLines(maxLines);
		}
		if (backgroundColor == null) {
			backgroundColor = nightMode ? R.color.list_background_color_dark : R.color.list_background_color_light;
		}
		view.setBackgroundColor(ContextCompat.getColor(ctx, backgroundColor));
	}

	public static void rotateImageByLayoutDirection(ImageView image, int layoutDirection) {
		if (image == null) {
			return;
		}
		int rotation = layoutDirection == View.LAYOUT_DIRECTION_LTR ? 0 : 180;
		image.setRotationY(rotation);
	}

	public static void setupCompoundButtonDrawable(Context ctx, boolean nightMode, @ColorInt int activeColor, Drawable drawable) {
		int inactiveColor = ContextCompat.getColor(ctx, nightMode ? R.color.icon_color_default_dark : R.color.icon_color_default_light);
		int[][] states = new int[][] {
				new int[] {-android.R.attr.state_checked},
				new int[] {android.R.attr.state_checked}
		};
		ColorStateList csl = new ColorStateList(states, new int[]{inactiveColor, activeColor});
		DrawableCompat.setTintList(DrawableCompat.wrap(drawable), csl);
	}

	public static void setupCompoundButton(boolean nightMode, @ColorInt int activeColor, CompoundButton compoundButton) {
	    if (compoundButton == null) {
	        return;
        }
	    Context ctx = compoundButton.getContext();
		int inactiveColorPrimary = ContextCompat.getColor(ctx, nightMode ? R.color.icon_color_default_dark : R.color.icon_color_secondary_light);
		int inactiveColorSecondary = getColorWithAlpha(inactiveColorPrimary, 0.45f);
		setupCompoundButton(compoundButton, activeColor, inactiveColorPrimary, inactiveColorSecondary);
	}

	public static void setupCompoundButton(CompoundButton compoundButton, boolean nightMode, CompoundButtonType type) {
		if (compoundButton == null) {
			return;
		}
		OsmandApplication app = (OsmandApplication) compoundButton.getContext().getApplicationContext();
		@ColorInt int activeColor = ContextCompat.getColor(app, nightMode ? R.color.active_color_primary_dark : R.color.active_color_primary_light);
		@ColorInt int inactiveColorPrimary = ContextCompat.getColor(app, nightMode ? R.color.icon_color_default_dark : R.color.icon_color_secondary_light);
		@ColorInt int inactiveColorSecondary = getColorWithAlpha(inactiveColorPrimary, 0.45f);
		switch (type) {
			case PROFILE_DEPENDENT:
				ApplicationMode appMode = app.getSettings().getApplicationMode();
				activeColor = ContextCompat.getColor(app, appMode.getIconColorInfo().getColor(nightMode));
				break;
			case TOOLBAR:
				activeColor = Color.WHITE;
				inactiveColorPrimary = activeColor;
				inactiveColorSecondary = UiUtilities.getColorWithAlpha(Color.BLACK, 0.25f);
				break;
		}
		setupCompoundButton(compoundButton, activeColor, inactiveColorPrimary, inactiveColorSecondary);
	}

	public static void setupCompoundButton(CompoundButton compoundButton,
										   @ColorInt int activeColor,
										   @ColorInt int inactiveColorPrimary,
										   @ColorInt int inactiveColorSecondary) {
		if (compoundButton == null) {
			return;
		}
		int[][] states = new int[][] {
				new int[] {-android.R.attr.state_checked},
				new int[] {android.R.attr.state_checked}
		};
		if (compoundButton instanceof SwitchCompat) {
			SwitchCompat sc = (SwitchCompat) compoundButton;
			int[] thumbColors = new int[] {
					inactiveColorPrimary, activeColor
			};

			int[] trackColors = new int[] {
					inactiveColorSecondary, inactiveColorSecondary
			};
			DrawableCompat.setTintList(DrawableCompat.wrap(sc.getThumbDrawable()), new ColorStateList(states, thumbColors));
			DrawableCompat.setTintList(DrawableCompat.wrap(sc.getTrackDrawable()), new ColorStateList(states, trackColors));
		} else if (compoundButton instanceof TintableCompoundButton) {
			ColorStateList csl = new ColorStateList(states, new int[]{inactiveColorPrimary, activeColor});
			((TintableCompoundButton) compoundButton).setSupportButtonTintList(csl);
		}
		compoundButton.setBackgroundColor(Color.TRANSPARENT);
	}
	
	public static void setupSeekBar(@NonNull OsmandApplication app, @NonNull SeekBar seekBar, 
	                                boolean nightMode, boolean profileDependent) {
		int activeColor = ContextCompat.getColor(app, profileDependent ?
				app.getSettings().APPLICATION_MODE.get().getIconColorInfo().getColor(nightMode) :
				nightMode ? R.color.active_color_primary_dark : R.color.active_color_primary_light);
		setupSeekBar(seekBar, activeColor, nightMode);
	}

	public static void setupSeekBar(@NonNull SeekBar seekBar, @ColorInt int activeColor, boolean nightMode) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			int backgroundColor = ContextCompat.getColor(seekBar.getContext(), nightMode
					? R.color.icon_color_secondary_dark : R.color.icon_color_default_light);
			if (seekBar.getProgressDrawable() instanceof LayerDrawable) {
				LayerDrawable progressDrawable = (LayerDrawable) seekBar.getProgressDrawable();
				Drawable background = progressDrawable.findDrawableByLayerId(android.R.id.background);
				if (background != null) {
					background.setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN);
				}
				Drawable progress = progressDrawable.findDrawableByLayerId(android.R.id.progress);
				if (progress != null) {
					progress.setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
				}
			}
			seekBar.getThumb().setColorFilter(activeColor, PorterDuff.Mode.SRC_IN);
		}
	}
	
	public static void setupDialogButton(boolean nightMode, View buttonView, DialogButtonType buttonType, @StringRes int buttonTextId) {
		setupDialogButton(nightMode, buttonView, buttonType, buttonView.getContext().getString(buttonTextId));
	}

	public static void setupDialogButton(boolean nightMode, View buttonView, DialogButtonType buttonType, CharSequence buttonText) {
		setupDialogButton(nightMode, buttonView, buttonType, buttonText, INVALID_ID);
	}

	public static void setupDialogButton(boolean nightMode, View buttonView, DialogButtonType buttonType, CharSequence buttonText, int iconResId) {
		Context ctx = buttonView.getContext();
		TextViewEx buttonTextView = (TextViewEx) buttonView.findViewById(R.id.button_text);
		boolean v21 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
		View buttonContainer = buttonView.findViewById(R.id.button_container);
		int textAndIconColorResId = INVALID_ID;
		switch (buttonType) {
			case PRIMARY:
				if (v21) {
					AndroidUtils.setBackground(ctx, buttonContainer, nightMode, R.drawable.ripple_solid_light, R.drawable.ripple_solid_dark);
				}
				AndroidUtils.setBackground(ctx, buttonView, nightMode, R.drawable.dlg_btn_primary_light, R.drawable.dlg_btn_primary_dark);
				textAndIconColorResId = nightMode ? R.color.dlg_btn_primary_text_dark : R.color.dlg_btn_primary_text_light;
				break;
			case SECONDARY:
				if (v21) {
					AndroidUtils.setBackground(ctx, buttonContainer, nightMode, R.drawable.ripple_solid_light, R.drawable.ripple_solid_dark);
				}
				AndroidUtils.setBackground(ctx, buttonView, nightMode, R.drawable.dlg_btn_secondary_light, R.drawable.dlg_btn_secondary_dark);
				textAndIconColorResId = nightMode ? R.color.dlg_btn_secondary_text_dark : R.color.dlg_btn_secondary_text_light;
				break;
			case STROKED:
				if (v21) {
					AndroidUtils.setBackground(ctx, buttonContainer, nightMode, R.drawable.ripple_light, R.drawable.ripple_dark);
				}
				AndroidUtils.setBackground(ctx, buttonView, nightMode, R.drawable.dlg_btn_stroked_light, R.drawable.dlg_btn_stroked_dark);
				textAndIconColorResId = nightMode ? R.color.dlg_btn_secondary_text_dark : R.color.dlg_btn_secondary_text_light;
				break;
		}
		if (textAndIconColorResId != INVALID_ID) {
			ColorStateList colorStateList = ContextCompat.getColorStateList(ctx, textAndIconColorResId);
			buttonTextView.setText(buttonText);
			buttonTextView.setTextColor(colorStateList);
			buttonTextView.setEnabled(buttonView.isEnabled());
			if (iconResId != INVALID_ID) {
				Drawable icon = tintDrawable(ContextCompat.getDrawable(ctx, iconResId), ContextCompat.getColor(ctx, textAndIconColorResId));
				buttonTextView.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
				buttonTextView.setCompoundDrawablePadding(AndroidUtils.dpToPx(ctx, ctx.getResources().getDimension(R.dimen.content_padding_half)));
			}
		}
	}

	public static LayoutInflater getInflater(Context ctx, boolean nightMode) {
		return LayoutInflater.from(getThemedContext(ctx, nightMode));
	}

	public static Context getThemedContext(Context context, boolean nightMode) {
		return getThemedContext(context, nightMode, R.style.OsmandLightTheme, R.style.OsmandDarkTheme);
	}

	public static Context getThemedContext(Context context, boolean nightMode, int lightStyle, int darkStyle) {
		return new ContextThemeWrapper(context, nightMode ? darkStyle : lightStyle);
	}

	public static void setMargins(View v, int l, int t, int r, int b) {
		if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
			ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
			p.setMargins(l, t, r, b);
			v.requestLayout();
		}
	}
}