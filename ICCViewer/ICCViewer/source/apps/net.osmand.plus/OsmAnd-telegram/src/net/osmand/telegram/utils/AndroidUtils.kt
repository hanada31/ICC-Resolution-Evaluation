package net.osmand.telegram.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.net.Uri
import android.os.Build
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import net.osmand.telegram.R
import java.io.File

object AndroidUtils {
	
	private const val PERMISSION_REQUEST_LOCATION = 1

	private fun isHardwareKeyboardAvailable(context: Context): Boolean {
		return context.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS
	}

	fun softKeyboardDelayed(view: View) {
		view.post {
			view.requestFocus()
			if (!isHardwareKeyboardAvailable(view.context)) {
				val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
				imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
			}
		}
	}

	fun hideSoftKeyboard(activity: Activity, input: View?) {
		val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
		if (inputMethodManager != null) {
			if (input != null) {
				val windowToken = input.windowToken
				if (windowToken != null) {
					inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
				}
			}
		}
	}

	fun dismissAllDialogs(fm: FragmentManager) {
		for (fragment in fm.fragments) {
			if (fragment is DialogFragment) {
				fragment.dismissAllowingStateLoss()
			}
			dismissAllDialogs(fragment.childFragmentManager)
		}
	}

	fun isLocationPermissionAvailable(context: Context): Boolean {
		return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
	}


	fun requestLocationPermission(activity: Activity) {
		ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
	}
	
	fun dpToPx(ctx: Context, dp: Float): Int {
		val r = ctx.resources
		return TypedValue.applyDimension(
				COMPLEX_UNIT_DIP,
				dp,
				r.displayMetrics
		).toInt()
	}

	fun getStatusBarHeight(ctx: Context): Int {
		var result = 0
		val resourceId = ctx.resources.getIdentifier("status_bar_height", "dimen", "android")
		if (resourceId > 0) {
			result = ctx.resources.getDimensionPixelSize(resourceId)
		}
		return result
	}

	fun addStatusBarPadding19v(ctx: Context, view: View) {
		if (Build.VERSION.SDK_INT >= 19) {
			view.apply {
				setPadding(paddingLeft, paddingTop + getStatusBarHeight(ctx), paddingRight, paddingBottom)
			}
		}
	}

	fun removeStatusBarPadding19v(ctx: Context, view: View) {
		if (Build.VERSION.SDK_INT >= 19) {
			view.apply {
				setPadding(paddingLeft, paddingTop - getStatusBarHeight(ctx), paddingRight, paddingBottom)
			}
		}
	}

	fun getNavBarHeight(ctx: Context): Int {
		if (!hasNavBar(ctx)) {
			return 0
		}
		val landscape = ctx.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
		val isSmartphone = ctx.resources.configuration.smallestScreenWidthDp < 600
		if (isSmartphone && landscape) {
			return 0
		}
		val name = if (landscape) "navigation_bar_height_landscape" else "navigation_bar_height"
		val id = ctx.resources.getIdentifier(name, "dimen", "android")
		return if (id > 0) {
			ctx.resources.getDimensionPixelSize(id)
		} else 0
	}

	fun hasNavBar(ctx: Context): Boolean {
		val id = ctx.resources.getIdentifier("config_showNavigationBar", "bool", "android")
		return id > 0 && ctx.resources.getBoolean(id)
	}

	fun enterToTransparentFullScreen(activity: Activity) {
		if (Build.VERSION.SDK_INT >= 23) {
			val window = activity.window
			window.statusBarColor = Color.TRANSPARENT
			window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
		}
	}

	fun enterToTranslucentFullScreen(activity: Activity) {
		if (Build.VERSION.SDK_INT >= 19) {
			activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
		}
	}

	fun getPopupMenuWidth(ctx: Context, titles: Collection<String>): Int {
		val txtSize = ctx.resources.getDimensionPixelSize(R.dimen.list_item_title_text_size)
		val paint = Paint().apply { textSize = txtSize.toFloat() }
		val maxTextWidth = titles.map { paint.measureText(it) }.max()
		if (maxTextWidth != null) {
			val maxItemWidth = maxTextWidth.toInt() + AndroidUtils.dpToPx(ctx, 34f)
			val minWidth = AndroidUtils.dpToPx(ctx, 100f)
			return maxOf(minWidth, maxItemWidth)
		}
		return 0
	}

	fun getPopupMenuHeight(ctx: Context): Int {
		return ctx.resources.getDimensionPixelSize(R.dimen.list_popup_window_height)
	}

	fun setSnackbarTextColor(snackbar: Snackbar, @ColorRes colorId: Int) {
		val view = snackbar.view
		val tv = view.findViewById(android.support.design.R.id.snackbar_action) as TextView
		tv.setTextColor(ContextCompat.getColor(view.context, colorId))
	}

	fun createPressedColorStateList(
		ctx: Context, light: Boolean,
		@ColorRes lightNormal: Int, @ColorRes lightPressed: Int,
		@ColorRes darkNormal: Int = 0, @ColorRes darkPressed: Int = 0
	): ColorStateList {
		return createColorStateList(
			ctx, light, android.R.attr.state_pressed,
			lightNormal, lightPressed, darkNormal, darkPressed
		)
	}

	fun createColorStateList(
		ctx: Context, light: Boolean, state: Int,
		@ColorRes lightNormal: Int, @ColorRes lightState: Int,
		@ColorRes darkNormal: Int, @ColorRes darkState: Int
	): ColorStateList {
		return ColorStateList(
			arrayOf(intArrayOf(state), intArrayOf()),
			intArrayOf(
				ContextCompat.getColor(ctx, if (light) lightState else darkState),
				ContextCompat.getColor(ctx, if (light) lightNormal else darkNormal)
			)
		)
	}

	fun createPressedStateListDrawable(normal: Drawable, pressed: Drawable): StateListDrawable {
		return createStateListDrawable(normal, pressed, android.R.attr.state_pressed)
	}

	fun createStateListDrawable(
		normal: Drawable,
		stateDrawable: Drawable,
		state: Int
	): StateListDrawable {
		val res = StateListDrawable()
		res.addState(intArrayOf(state), stateDrawable)
		res.addState(intArrayOf(), normal)
		return res
	}

	@ColorInt
	fun getAttrColor(ctx: Context, @AttrRes attrId: Int, @ColorInt defaultColor: Int = 0): Int {
		val ta = ctx.theme.obtainStyledAttributes(intArrayOf(attrId))
		val color = ta.getColor(0, defaultColor)
		ta.recycle()
		return color
	}

	fun getUriForFile(context: Context, file: File): Uri {
		return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			Uri.fromFile(file)
		} else {
			FileProvider.getUriForFile(context,  "net.osmand.telegram.fileprovider", file)
		}
	}

	fun resourceToUri(ctx: Context, resID: Int): Uri {
		return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
					"://${ctx.resources.getResourcePackageName(resID)}" +
					"/${ctx.resources.getResourceTypeName(resID)}" +
					"/${ctx.resources.getResourceEntryName(resID)}"
		)
	}

	fun getAppVersionCode(ctx: Context, appPackage: String) = try {
		ctx.packageManager.getPackageInfo(appPackage, 0).versionCode
	} catch (e: PackageManager.NameNotFoundException) {
		-1
	}

	fun isAppInstalled(ctx: Context, appPackage: String): Boolean {
		if (appPackage.isEmpty()) {
			return false
		}
		try {
			ctx.packageManager.getPackageInfo(appPackage, 0)
		} catch (e: PackageManager.NameNotFoundException) {
			return false
		}

		return true
	}

	fun getPlayMarketIntent(ctx: Context, packageName: String) =
			Intent(Intent.ACTION_VIEW, Uri.parse(AndroidUtils.getPlayMarketLink(ctx, packageName)))

	fun getPlayMarketLink(ctx: Context, packageName: String): String {
		if (isAppInstalled(ctx, "com.android.vending")) {
			return "market://details?id=$packageName"
		}
		return "https://play.google.com/store/apps/details?id=$packageName"
	}

	fun isIntentSafe(ctx: Context, intent: Intent) =
		ctx.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
}
