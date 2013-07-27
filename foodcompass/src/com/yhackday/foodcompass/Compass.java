/* 
 * Reference: http://sunil-android.blogspot.com/2013/02/create-our-android-compass.html 
 */

package com.yhackday.foodcompass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Compass extends View {

	private final int STROKE_WIDTH = 5;
	
	private float direction;
	
	public Compass(Context context) {
		super(context);
	}

	public Compass(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public Compass(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int radius = (width > height) ? height/2 : width/2;
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(STROKE_WIDTH);
		paint.setColor(Color.WHITE);
		
		canvas.drawCircle(
				width/2, 
				height/2, 
				radius, 
				paint);
		
		paint.setColor(Color.RED);
		canvas.drawLine(width/2, 
				height/2, 
				(float)(width/2 + radius * Math.sin(-this.direction)), 
				(float)(height/2 - radius * Math.cos(-this.direction)), 
				paint);
	}
	
	public void updateDirection(float direction) {
		this.direction = direction;
		this.invalidate();
	}

}
