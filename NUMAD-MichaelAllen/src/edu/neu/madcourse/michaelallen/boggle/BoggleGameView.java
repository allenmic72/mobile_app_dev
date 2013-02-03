package edu.neu.madcourse.michaelallen.boggle;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.neu.madcourse.michaelallen.R;

public class BoggleGameView extends View {

	private float viewWidth;
	private float viewHeight;
	private int blockWidth;
	private ArrayList<Rect> selectedBlocks;
	
	private final BoggleGame game;
	
	private static final String TAG = "BoggleGameView";
	
	public BoggleGameView(Context context, AttributeSet attrs) {
		super(context);
		this.game = (BoggleGame) context;
		
		selectedBlocks = new ArrayList<Rect>();
	}
	
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
	    int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
	    setMeasuredDimension(width,width);
	    Log.v("measure","width:"+width + " height:"+width);
	}
	
	@Override
	   protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		viewHeight = h;
		viewWidth = w;
	    blockWidth = (int) (viewWidth / 4f);
	    Log.d(TAG, "onSizeChanged: block width " + blockWidth);
	    super.onSizeChanged(w, h, oldw, oldh);
	   }	
	
	@Override
	protected void onDraw(Canvas canvas){
		int greenBlueTranslucent = Color.argb(75, 0, 140, 200);
		
		Paint boardPaint;
		boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		boardPaint.setColor(greenBlueTranslucent);
		boardPaint.setStyle(Paint.Style.STROKE);
		boardPaint.setStrokeWidth(8);
		
		for (int vert = 0; vert < 5; vert++){
			float lineXVal = vert * blockWidth;
			canvas.drawLine(lineXVal, 0, lineXVal, viewHeight, boardPaint);
		}
		
		for (int horiz = 0; horiz < 5; horiz++){
			float lineYVal = horiz * blockWidth;
			canvas.drawLine(0, lineYVal, viewWidth, lineYVal, boardPaint);
		}		
		
		Paint selected = new Paint();
	    selected.setColor(getResources().getColor(R.color.puzzle_selected));
		for (int block = 0; block < selectedBlocks.size(); block++){
			if (selectedBlocks.get(block) != null){				
			    canvas.drawRect(selectedBlocks.get(block), selected);
			}
		}
		
		
		
		
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		
		
		
		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:
			Rect block = createTouchedBlock(x, y);
			if (block != null){
				selectedBlocks.add(block);
				invalidate(block);
			}
			break;
		case MotionEvent.ACTION_UP:
			//TODO: word checking and clearing drawn blocks
			return false;
		case MotionEvent.ACTION_MOVE:
			if(moveEventWithinTolerableRange(x / blockWidth, y / blockWidth)){
				Rect block1 = createTouchedBlock(x, y);
				if (block1 != null){
					selectedBlocks.add(block1);
					invalidate(block1);
				}
			}
			break;
		}
		
		return true;
		
	}
	
	/**
	 * 
	 * @param x left x position of the block touched by user
	 * @param y top y position of the block touched by user
	 * @return a new Rect to be drawn on the touched block iff the block
	 * 			has not already been touched since last word completion
	 * 			else null 
	 */
	private Rect createTouchedBlock(float x, float y){
		
		int touchedBlockX =(int) (x / blockWidth);
		int touchedBlockY =(int) (y / blockWidth);
		
		if(blockNotAlreadyTouched(touchedBlockX, touchedBlockY)){

			
			int touchedBlockRightX = touchedBlockX + 1;
			int touchedBlockBottomY = touchedBlockY + 1;
			
			Rect block = new Rect(
					touchedBlockX * blockWidth,
					touchedBlockY * blockWidth,
					touchedBlockRightX * blockWidth,
					touchedBlockBottomY * blockWidth);
			return block;
		}
		else{
			return null;
		}
		
	}
	
	private boolean blockNotAlreadyTouched(int x, int y){
		boolean notTouched = true;
		for (int blocks = 0; blocks < selectedBlocks.size(); blocks++){
			Rect block = selectedBlocks.get(blocks);
			if (block.left == x && block.top == y){
				notTouched = false;
				break;
			}
		}
		return notTouched;
	}
	
	private void invalidateAndClearSelectedBlocks(){
		for (int block = 0; block < selectedBlocks.size(); block++){
			Rect rect = selectedBlocks.get(block);
			if (rect != null){
				invalidate(rect);
			}
		}
		selectedBlocks.clear();
	}
	
	/**
	 * Checks if the decimal value of both x and y is > 0.1 and < 0.9
	 * 
	 * @param x block normalized x value of touch event
	 * @param y block normalized y value of touch event
	 */
	private boolean moveEventWithinTolerableRange(float x, float y){
		boolean withinTolerableRange = false;
		
		while(x > 1.0 || y > 1.0){
			if (x > 1.0){
				x -= 1;
			}
			if (y > 1.0){
				y -= 1.0;
			}
		}
		
		if((x > .1 && x < .9) &&
			(y > .1 && y < .9)){
			withinTolerableRange = true;
		}
		Log.d("moveEventWithinTolerableRange", "" + withinTolerableRange);
		return withinTolerableRange;
	}
	
}