package edu.neu.madcourse.michaelallen.persistentboggle;

import java.util.ArrayList;

import edu.neu.madcourse.michaelallen.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class PersBoggleTitleView extends View{

	private float viewWidth;
	private float viewHeight;
	private int blockWidth;
	Rect blockRect;
	Bitmap blockBitmap;
	Paint letterColor;
	ArrayList<String> letters;
	
	public PersBoggleTitleView(Context context, AttributeSet attrs) {
		super(context);
		
		blockRect = new Rect();
		blockBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_blue_matte);
		letterColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		letterColor.setColor(getResources().getColor(
		           R.color.pers_boggle_letter_text));
		letterColor.setStyle(Style.FILL);
		letterColor.setTextAlign(Paint.Align.CENTER);
		letterColor.setTypeface(Typeface.DEFAULT_BOLD);
		
	    letters = new ArrayList<String>();
		letters.add("B");
		letters.add("O");
		letters.add("G");
		letters.add("G");
		letters.add("L");
		letters.add("E");
	}
	
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
	    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
	    setMeasuredDimension(width,width);
	}
	
	@Override
	   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		viewHeight = h;
		viewWidth = w;
	    blockWidth = (int) (viewWidth / 6);
	    letterColor.setTextSize(blockWidth * 0.75f);
	    super.onSizeChanged(w, h, oldw, oldh);
	   
	   }	
	
	@Override
	protected void onDraw(Canvas canvas){
		for (int horiz = 0; horiz < letters.size(); horiz++){
			//float lineXVal = vert * blockWidth;
			//canvas.drawLine(lineXVal, 0, lineXVal, viewHeight, boardPaint);
				blockRect.left = horiz * blockWidth;
				blockRect.top =  0;
				blockRect.right = horiz * blockWidth + blockWidth;
				blockRect.bottom = blockWidth;
				
				canvas.drawBitmap(blockBitmap, null, blockRect, null);
			}
		
		

	    
	    
	   
		for (int i = 0; i < letters.size(); i++){
				canvas.drawText(letters.get(i),
		                  i * blockWidth + blockWidth/2,
		                  7.5f * blockWidth / 10,
		                  letterColor);
		}
		
		
	}
}