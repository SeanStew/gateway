package ca.gatewaybaptistchurch.gateway.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import ca.gatewaybaptistchurch.gateway.R;

/**
 * Created by sean1 on 1/15/2017.
 */

public class FixedRatioRelativeLayout extends RelativeLayout {
	private int mAspectRatioWidth;
	private int mAspectRatioHeight;

	public FixedRatioRelativeLayout(Context context) {
		super(context);
	}

	public FixedRatioRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		Init(context, attrs);
	}

	public FixedRatioRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		Init(context, attrs);
	}

	private void Init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FixedRatioRelativeLayout);

		mAspectRatioWidth = a.getInt(R.styleable.FixedRatioRelativeLayout_aspectRatioWidth, 16);
		mAspectRatioHeight = a.getInt(R.styleable.FixedRatioRelativeLayout_aspectRatioHeight, 9);

		a.recycle();
	}
	// **overrides**

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
		int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
		int calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth;
		int finalWidth, finalHeight;

		if (calculatedHeight > originalHeight) {
			finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight;
			finalHeight = originalHeight;
		} else {
			finalWidth = originalWidth;
			finalHeight = calculatedHeight;
		}

		super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
	}
}