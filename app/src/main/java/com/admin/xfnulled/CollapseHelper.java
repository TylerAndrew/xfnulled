package com.admin.xfnulled;

import android.annotation.SuppressLint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.OvershootInterpolator;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.admin.xfnulled.R;

public class CollapseHelper {

	/** Whole slide down view. */
	private RelativeLayout slideDownView;

	/** View that accepts the touch events. */
	private View handle;
	
	private TextView titleView;

	int DIRECTION_UP = 2;
	int DIRECTION_DOWN = 1;
	int DIRECTION_NO = 0;

	CollapseHelper(View IncludeView, final WebView browser, final MainActivity act){
		slideDownView = (RelativeLayout) IncludeView;
		handle = IncludeView.findViewById(R.id.handle);
		titleView = (TextView) IncludeView.findViewById(R.id.titleView);
		
	    LayerDrawable layers = (LayerDrawable) handle.getBackground();
	    
	    layers.getDrawable(1).setColorFilter(act.getResources().getColor(R.color.theme),Mode.SRC_ATOP);
		
		handle.setOnTouchListener(new View.OnTouchListener() {
			/* Starting Y point (where touch started). */
			float yStart = 0;

			/* Default height when in the close state. */
			float closedHeight;

			/* Default height when in the open state. */
			float openHeight;

			/* The height during the transition (changed on ACTION_MOVE). */
			float currentHeight;

			/*
			 * The last y touch that occurred. This is used to determine if the
			 * view should snap up or down on release. Used in conjunction with
			 * directionDown boolean.
			 */
			float lastY = 0;
			
			// boolean directionDown = false;
			int direction = DIRECTION_NO;

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if (closedHeight == 0){
					closedHeight = handle.getHeight();
				}
				
				if (openHeight == 0){
					openHeight = dpToPx(50, slideDownView) + handle.getHeight();
				}

				switch (event.getAction()) {

				/* User tapped down on screen. */
				case MotionEvent.ACTION_DOWN:
					// User has tapped the screen
					yStart = event.getRawY();
					lastY = event.getRawY();
					currentHeight = slideDownView.getHeight();

					direction = DIRECTION_NO;
					break;

				/* User is dragging finger. */
				case MotionEvent.ACTION_MOVE:

					if (ViewHelper.getY(handle) > 0 || (direction == DIRECTION_NO)) {
						// Calculate the total height change thus far.
						float totalHeightDiff = event.getRawY() - yStart;

						// Adjust the slide down height immediately with touch
						// movements.
						LayoutParams params = slideDownView.getLayoutParams();
						params.height = (int) (currentHeight + totalHeightDiff);
						slideDownView.setLayoutParams(params);

						// Check and set which direction drag is moving.
						if (event.getRawY() > lastY) {
							direction = DIRECTION_DOWN;
						} else {
							direction = DIRECTION_UP;
						}

						// Set the lastY for comparison in the next ACTION_MOVE
						// event.
						lastY = event.getRawY();
					}
					break;

				/* User lifted up finger. */
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:

					/*
					 * Need to snap either up or down. Using ValueAnimator to
					 * "finish" the action. HeightEvaluator is a custom class.
					 * 
					 * NOTE: I'm using the nineoldandroids library for
					 */
					if (direction == DIRECTION_DOWN
							|| (!isOpen(currentHeight, openHeight) && direction == DIRECTION_NO)) {

						// Open the sliding view.
						int startHeight = slideDownView.getHeight();

						ValueAnimator animation = ValueAnimator.ofObject(
								new HeightEvaluator(slideDownView),
								startHeight, (int) openHeight).setDuration(300);

						// See Table 3 for other interpolator options
						// -
						// http://developer.android.com/guide/topics/graphics/prop-animation.html
						animation.setInterpolator(new OvershootInterpolator(1));
						animation.start();

					} else if (direction == DIRECTION_UP
							|| (isOpen(currentHeight, openHeight) && direction == DIRECTION_NO)) {

						closeSlidingView();
					}
					
					direction = DIRECTION_NO;
					break;

				}
				return true;
			}
		});
		
		IncludeView.findViewById(R.id.btn_home).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				browser.loadUrl("WEBURL");		
				closeSlidingView();
			}
			
		});
		
		IncludeView.findViewById(R.id.btn_back).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				browser.goBack();	
				closeSlidingView();
			}
			
		});
		
		IncludeView.findViewById(R.id.btn_forward).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				browser.goForward();
				closeSlidingView();
			}
			
		});

	}
	
	void titleChanged(String s){
		titleView.setText(s);
	}
	
	public int dpToPx(int dp, View v) {
	    DisplayMetrics displayMetrics = v.getContext().getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
	}
	
	void closeSlidingView(){
		 int closedHeight = handle.getHeight();

		 int startHeight = slideDownView.getHeight();
				ValueAnimator animation = ValueAnimator.ofObject(
						new HeightEvaluator(slideDownView),
						startHeight, (int) closedHeight).setDuration(
						300);
				animation.setInterpolator(new OvershootInterpolator(1));
				animation.start();
		 
	}

	boolean isOpen(float currentHeight, float openHeight) {
		if (currentHeight == openHeight) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
