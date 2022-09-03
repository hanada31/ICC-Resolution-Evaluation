package net.osmand.plus.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;

public class TextViewExProgress extends TextViewEx {

	public Paint paint;
	public float percent;

	public TextViewExProgress(Context context) {
		super(context);
		initPaint();
	}

	public TextViewExProgress(Context context, AttributeSet attrs) {
		super(context, attrs);
		initPaint();
	}

	public TextViewExProgress(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initPaint();
	}

	public TextViewExProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initPaint();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawProgress(canvas);
	}

	private void drawProgress(Canvas canvas) {
		int w = getWidth();
		int h = getHeight();
		float rectW = w * (percent);
		canvas.drawRect(0, 0, rectW, h, paint);
	}

	private void initPaint() {
		paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
		setLayerType(LAYER_TYPE_SOFTWARE, null);
	}
}