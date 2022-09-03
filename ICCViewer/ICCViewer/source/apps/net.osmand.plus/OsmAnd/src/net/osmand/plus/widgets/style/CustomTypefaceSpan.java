package net.osmand.plus.widgets.style;

import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class CustomTypefaceSpan extends MetricAffectingSpan {

	private Typeface typeface;

	public CustomTypefaceSpan(Typeface typeface) {
		this.typeface = typeface;
	}

	@Override
	public void updateMeasureState(TextPaint p) {
		update(p);
	}

	@Override
	public void updateDrawState(TextPaint tp) {
		update(tp);
	}

	private void update(TextPaint tp) {
		Typeface old = tp.getTypeface();
		int oldStyle = old.getStyle();
		Typeface font = Typeface.create(typeface, oldStyle);
		tp.setTypeface(font);
	}
}