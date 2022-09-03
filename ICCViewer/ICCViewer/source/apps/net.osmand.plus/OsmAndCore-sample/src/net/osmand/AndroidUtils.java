package net.osmand;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import net.osmand.core.samples.android.sample1.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static android.util.TypedValue.COMPLEX_UNIT_DIP;

public class AndroidUtils {

	/**
	 * @param context
	 * @return true if Hardware keyboard is available
	 */
	public static boolean isHardwareKeyboardAvailable(Context context) {
		return context.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
	}
	
	public static void softKeyboardDelayed(final View view) {
		view.post(new Runnable() {
			@Override
			public void run() {
				if (!isHardwareKeyboardAvailable(view.getContext())) {
					InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm != null) {
						imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
					}
				}
			}
		});
	}

	public static void hideSoftKeyboard(final Activity activity, final View input) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (inputMethodManager != null) {
			if (input != null) {
				IBinder windowToken = input.getWindowToken();
				if (windowToken != null) {
					inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
				}
			}
		}

	}

	public static String formatDate(Context ctx, long time) {
		return DateFormat.getDateFormat(ctx).format(new Date(time));
	}
	
	public static String formatDateTime(Context ctx, long time) {
		Date d = new Date(time);
		return DateFormat.getDateFormat(ctx).format(d) +
				" " + DateFormat.getTimeFormat(ctx).format(d);
	}
	
	public static String formatTime(Context ctx, long time) {
		return DateFormat.getTimeFormat(ctx).format(new Date(time));
	}

	public static View findParentViewById(View view, int id) {
		ViewParent viewParent = view.getParent();

		while (viewParent != null && viewParent instanceof View) {
			View parentView = (View)viewParent;
			if (parentView.getId() == id)
				return parentView;

			viewParent = parentView.getParent();
		}

		return null;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static void setBackground(Context ctx, View view, boolean night, int lightResId, int darkResId) {
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
			view.setBackground(ctx.getResources().getDrawable(night ? darkResId : lightResId,
					ctx.getTheme()));
		} else {
			view.setBackgroundDrawable(ctx.getResources().getDrawable(night ? darkResId : lightResId));
		}
	}

	public static void setBackgroundColor(Context ctx, View view, boolean night, int lightResId, int darkResId) {
		view.setBackgroundColor(ctx.getResources().getColor(night ? darkResId : lightResId));
	}

	public static void setListItemBackground(Context ctx, View view, boolean night) {
		setBackgroundColor(ctx, view, night, R.color.bg_color_light, R.color.bg_color_dark);
	}

	public static void setListBackground(Context ctx, View view, boolean night) {
		setBackgroundColor(ctx, view, night, R.color.ctx_menu_info_view_bg_light, R.color.ctx_menu_info_view_bg_dark);
	}

	public static void setTextPrimaryColor(Context ctx, TextView textView, boolean night) {
		textView.setTextColor(night ?
				ctx.getResources().getColor(R.color.primary_text_dark)
				: ctx.getResources().getColor(R.color.primary_text_light));
	}

	public static void setTextSecondaryColor(Context ctx, TextView textView, boolean night) {
		textView.setTextColor(night ?
				ctx.getResources().getColor(R.color.secondary_text_dark)
				: ctx.getResources().getColor(R.color.secondary_text_light));
	}

	public static void setHintTextSecondaryColor(Context ctx, TextView textView, boolean night) {
		textView.setHintTextColor(night ?
				ctx.getResources().getColor(R.color.secondary_text_dark)
				: ctx.getResources().getColor(R.color.secondary_text_light));
	}

	public static int dpToPx(Context ctx, float dp) {
		Resources r = ctx.getResources();
		return (int) TypedValue.applyDimension(
				COMPLEX_UNIT_DIP,
				dp,
				r.getDisplayMetrics()
		);
	}

	public static int getStatusBarHeight(Context ctx) {
		int result = 0;
		int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = ctx.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public static int getScreenHeight(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	public static int getScreenWidth(Activity activity) {
		DisplayMetrics dm = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

	public static boolean isValidEmail(CharSequence target) {
		return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
	}

	public static String getFileAsString(File file) {
		try {
			FileInputStream fin = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(line);
			}
			reader.close();
			fin.close();
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	public static PointF centroidForPoly(PointF[] points) {
		float centroidX = 0, centroidY = 0;

		for (PointF point : points) {
			centroidX += point.x / points.length;
			centroidY += point.y / points.length;
		}
		return new PointF(centroidX, centroidY);
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}
