package edu.neu.madcourse.michaelallen.persistentboggle;

import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import edu.neu.madcourse.michaelallen.R;
import edu.neu.madcourse.michaelallen.sudoku.Music;
import edu.neu.mobileclass.apis.KeyValueAPI;

public class PersBoggleGameView extends View {

	private float viewWidth;
	private float viewHeight;
	private int blockWidth;
	private ArrayList<Rect> selectedBlocks;
	private String selectedLetters;
	
	private ArrayList<Rect> goodSelectionBlocks;
	private ArrayList<Rect> badSelectionBlocks;
	private ArrayList<Rect> goodOtherUserBlocks;
	private ArrayList<Rect> badOtherUserBlocks;
	
	private ArrayList<Rect> userLineSelection;
	
	private final PersBoggleGame game;
	
	
	private static final String TAG = "PersBoggleGameView";
	
	private Rect otherUserRect = new Rect();
	
	private Rect rectToDrawLine;
	
	private int opponentVersion;
	
	Bitmap blockBitmap;
	Bitmap goodWordBitmap;
	Bitmap badWordBitmap;
	Bitmap selectionBitmap;
	Paint boardPaint;
	Paint selected;
	Paint goodWordSelection;
	Paint goodOtherUserSelection;
	Paint badOtherUserSelection;
	Paint letterColor;
	Rect blockRect;
	
	public PersBoggleGameView(Context context, AttributeSet attrs) {
		super(context);
		this.game = (PersBoggleGame) context;
		
		selectedBlocks = new ArrayList<Rect>();
		selectedLetters = "";
		goodSelectionBlocks = new ArrayList<Rect>();
		badSelectionBlocks = new ArrayList<Rect>();
		goodOtherUserBlocks = new ArrayList<Rect>();
		badOtherUserBlocks = new ArrayList<Rect>();
		userLineSelection = new ArrayList<Rect>();
		rectToDrawLine  = new Rect();
		
		initiateCanvasObjects();
		
		opponentVersion = 0;
		//this.setBackgroundResource(R.drawable.bogglebck);
		
		startPollingServer(PersGlobals.getGlobals().getOpponent() + PersGlobals.getGlobals().getUsername(), this.game);
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
	    blockWidth = (int) (viewWidth / PersGlobals.getGlobals().getNumberOfBlocks());
		letterColor.setTextSize(blockWidth * 0.75f);
	    super.onSizeChanged(w, h, oldw, oldh);
	   }	
	
	@Override
	protected void onDraw(Canvas canvas){
		/*
		int brownTranslucent = getResources().getColor(R.color.pers_boggle_board_line);
		boardPaint.setColor(brownTranslucent);
		boardPaint.setStyle(Paint.Style.STROKE);
		boardPaint.setStrokeWidth(8);*/
		
		for (int vert = 0; vert < PersGlobals.getGlobals().getNumberOfBlocks(); vert++){
			for (int horiz = 0; horiz < PersGlobals.getGlobals().getNumberOfBlocks(); horiz++){
			//float lineXVal = vert * blockWidth;
			//canvas.drawLine(lineXVal, 0, lineXVal, viewHeight, boardPaint);
				blockRect.left = horiz * blockWidth;
				blockRect.top = vert * blockWidth;
				blockRect.right = horiz * blockWidth + blockWidth;
				blockRect.bottom = vert * blockWidth + blockWidth;
				
				
				canvas.drawBitmap(blockBitmap, null, blockRect, null);
			}	
		}
		
		/*
			float lineYVal = horiz * blockWidth;
			canvas.drawLine(0, lineYVal, viewWidth, lineYVal, boardPaint);
			*/
		
	    selected.setColor(getResources().getColor(R.color.puzzle_selected));
	    
		for (int block = 0; block < selectedBlocks.size(); block++){
			if (selectedBlocks.get(block) != null){				
				canvas.drawBitmap(selectionBitmap, 
						null, selectedBlocks.get(block), null);
			    //canvas.drawRect(selectedBlocks.get(block), selected);
			}
		}
		
		goodWordSelection.setColor(getResources().getColor(R.color.boggle_correctWord));
		for (int block = 0; block < goodSelectionBlocks.size(); block++){
			if (goodSelectionBlocks.get(block) != null){
				canvas.drawBitmap(goodWordBitmap, 
						null, goodSelectionBlocks.get(block), null);
			    //canvas.drawRect(goodSelectionBlocks.get(block), goodWordSelection);
			}
		}
		
		/*Paint badWordSelection = new Paint();
		badWordSelection.setColor(getResources().getColor(R.color.boggle_incorrectWord));*/
		for (int block = 0; block < badSelectionBlocks.size(); block++){
			if (badSelectionBlocks.get(block) != null){
				canvas.drawBitmap(badWordBitmap, 
						null, badSelectionBlocks.get(block), null);
				
				//canvas.drawRect(badSelectionBlocks.get(block), badWordSelection);
			}
		}
		
		/*Paint userSwipeLine = new Paint();
		userSwipeLine.setColor(getResources().getColor(R.color.pers_boggle_user_drawLine));
		userSwipeLine.setStyle(Paint.Style.STROKE);
		userSwipeLine.setStrokeWidth(8);
		for (int linePoint = 0; linePoint < userLineSelection.size(); linePoint++){
			if (linePoint > 0){
				if (userLineSelection.get(linePoint) != null && 
						userLineSelection.get(linePoint - 1) != null){
					Rect start = userLineSelection.get(linePoint);
					Rect end = userLineSelection.get(linePoint - 1);
					//Log.d(TAG, "drawling line at  " + point.left + point.top);
					canvas.drawLine(start.left + 10, start.top + 10, end.left + 10, end.top + 10, userSwipeLine);
				}
			}
			
		}*/
		/*
		if(userLineSelection.size() > 1){
			Rect start = userLineSelection.get(userLineSelection.size() - 2);
			Rect end = userLineSelection.get(userLineSelection.size() - 1);
			canvas.drawLine(start.left + 5, start.top + 5, end.left + 5, end.top + 5, userSwipeLine);
		}*/
		//canvas.drawRect(rectToDrawLine, userSwipeLine);
		//userLineSelection.add(rectToDrawLine);
		
		
		//Draw the other user's recent actions
		
		goodOtherUserSelection.setColor(getResources().getColor(R.color.pers_boggle_otherUserGoodWord));
		for (int i = 0; i < goodOtherUserBlocks.size(); i++){
			if (goodOtherUserBlocks.get(i) != null){
				canvas.drawRect(goodOtherUserBlocks.get(i), goodOtherUserSelection);
			}
		}
		
		
		badOtherUserSelection.setColor(getResources().getColor(R.color.pers_boggle_otherUserBadWord));
		for (int i = 0; i < badOtherUserBlocks.size(); i++){
			if (badOtherUserBlocks.get(i) != null){
				canvas.drawRect(badOtherUserBlocks.get(i), badOtherUserSelection);
			}
		}
		
		
	    
		for (int i = 0; i < PersGlobals.getGlobals().getNumberOfBlocks(); i++){
			for (int j = 0; j < PersGlobals.getGlobals().getNumberOfBlocks(); j++){
				canvas.drawText(this.game.getBoardLetter(i, j),
		                  i * blockWidth + blockWidth/2,
		                  j * blockWidth +  7.5f * blockWidth / 10,
		                  letterColor);
			}
		}
		
		
		
		
		
		
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event){
		if (PersGlobals.getGlobals().getIsPaused()){
			return false;
		}
		
		float x = event.getX();
		float y = event.getY();
		
		
		
		switch (event.getAction()){
		case MotionEvent.ACTION_DOWN:
			invalidateBlocks(goodSelectionBlocks);
			invalidateBlocks(badSelectionBlocks);
			
			Rect block = createTouchedBlock(x, y);
			if (block != null){
				int typewriter = PersGlobals.getGlobals().getTypewriter();
				PersGlobals.getGlobals().getSP().play(typewriter , 1, 1, 2, 0, 1);
				
				Vibrator vibrator = (Vibrator) game.getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(60);
				
				
				selectedBlocks.add(block);
				String touchedLetter = getTouchedLetter(x, y);
				selectedLetters += touchedLetter;
				this.game.addToSelectedLetterTextView(touchedLetter);
				invalidate(block);
				
				//addPointToDrawUserLine(x, y);
			}
			break;
		case MotionEvent.ACTION_UP:
			checkSelectedLetters();
			userLineSelection.clear();
			return false;
		case MotionEvent.ACTION_MOVE:
			//addPointToDrawUserLine(x, y);
			
			float xBlock = x / blockWidth;
			float yBlock = y / blockWidth;
			if(moveEventWithinTolerableRange(xBlock, yBlock) 
					&& blockIsAdjacentToLastTouchedBlock(xBlock, yBlock)
					){
				Rect block1 = createTouchedBlock(x, y);
				if (block1 != null){
					selectedBlocks.add(block1);
					String touchedLetter = getTouchedLetter(x, y);
					selectedLetters += touchedLetter;
					this.game.addToSelectedLetterTextView(touchedLetter);
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
			int blockXFixed = block.left / blockWidth;
			int blockYFixed = block.top / blockWidth;
			if (blockXFixed == x && blockYFixed == y){
				notTouched = false;
				break;
			}
		}
		return notTouched;
	}
	
	private void addPointToDrawUserLine(float x, float y){
		int xInt = (int) x;
		int yInt = (int) y;
		if (userLineSelection.size() > 0){
			Rect lastTouch = userLineSelection.get(userLineSelection.size() - 1);
			if (!(x > lastTouch.left && x < lastTouch.right 
				&& y < lastTouch.bottom && y > lastTouch.top)){
				addAndInvalidateRectPoint(xInt, yInt);
			}
		}
		else{
			addAndInvalidateRectPoint(xInt, yInt);
		}
	}
	
	private void addAndInvalidateRectPoint(int xInt, int yInt){
		if (xInt + 10 < viewWidth && yInt + 10 < viewHeight){
			rectToDrawLine = new Rect(xInt - 10, yInt - 10, xInt + 10, yInt + 10);
			invalidate(rectToDrawLine);
			userLineSelection.add(rectToDrawLine);
			//Log.d(TAG, "added to userLineSelection: " + point);
		}
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
	 * Checks if the decimal value of both x and y is > 0.20 and < 0.80
	 * and if the touch event occurred inside the game board
	 * 
	 * @param x block normalized x value of touch event
	 * @param y block normalized y value of touch event
	 */
	private boolean moveEventWithinTolerableRange(float x, float y){
		boolean withinTolerableRange = false;
		
		if (x >= PersGlobals.getGlobals().getNumberOfBlocks() || 
				y >= PersGlobals.getGlobals().getNumberOfBlocks()){
			return withinTolerableRange;
		}
		
		while(x > 1.0 || y > 1.0){
			if (x > 1.0){
				x -= 1;
			}
			if (y > 1.0){
				y -= 1.0;
			}
		}
		
		if((x > .20 && x < .80) &&
			(y > .20 && y < .80)){
			withinTolerableRange = true;
		}
		return withinTolerableRange;
	}
	
	private String getTouchedLetter(float x, float y){
		int blockXLocation = (int) x / blockWidth;
		int blockYLocation = (int) y / blockWidth;
		
		String letter = this.game.getBoardLetter(blockXLocation, blockYLocation);
		return letter;
	}
	
	/**
	 * checks the word that the user has formed against the dictionary
	 * if it is valid:
	 * 	 user gets points
	 * 	 adds valid word to list of words user has chosen
	 * 
	 * clearAllSelections will be called
	 * @throws IOException 
	 */
	private void checkSelectedLetters(){
		boolean goodWord = this.game.checkWordAndRewardUser(selectedLetters);
		ArrayList<Rect> selectionAnimationBlocks;
		int goodOrBad = -1;
		int[][] selectionMatrix;
		
		if (goodWord){
			selectionAnimationBlocks = goodSelectionBlocks;
			goodOrBad = 1;
		}
		else{
			selectionAnimationBlocks = badSelectionBlocks;
		}
		
		//selectionMatrix = convertRectsToMatrix(selectedBlocks, goodOrBad);
		//this.game.packageGameStateAndPublish(selectionMatrix);
		
		clearAllSelections(selectionAnimationBlocks);
	}
	
	/**
	 * convert the rect arraylist into a matrix of blocks, 
	 * with an int value indicating a selected block
	 * @param selectedBlocks the blocks that the user selected to form a word
	 * @param goodOrBad: 1 indicates a good word selection, -1 a bad one 
	 * @return
	 */
	private int[][] convertRectsToMatrix(ArrayList<Rect> selectedBlocks, int goodOrBad) {
		int size = PersGlobals.getGlobals().getNumberOfBlocks();
		int[][] matrix = new int[size][size];
		
		Log.d(TAG, "selected blocks: " + selectedBlocks);
		
		for (int i = 0; i < selectedBlocks.size(); i++){
			Rect r = selectedBlocks.get(i);
			int x = r.left / blockWidth;
			int y = r.top / blockWidth;
			matrix[x][y] = goodOrBad;
		}
		
		return matrix;
	}

	/**
	 * invalidates all selected blocks,
	 * clears the selected blocks, 
	 * removes the text from Selected letters textview
	 * 
	 * adds the blocks in selectedBlocks to the passed Array
	 * this will color the blocks either red or green
	 * sleeps for 500 ms then invalidates the blocks again to remove color
	 * 
	 */
	private void clearAllSelections(ArrayList<Rect> goodOrBadSelection){
		this.game.clearSelectedLetterTextView();
		
		
		
		for (int i = 0; i < selectedBlocks.size(); i++){
			Rect block = selectedBlocks.get(i);
			goodOrBadSelection.add(block);
			invalidate(block);
		}
		
		
		selectedLetters = "";
		selectedBlocks.clear();
		
		createTimerToRemoveAnimationOnBlocks();		
		
	}
	
	private void createTimerToRemoveAnimationOnBlocks(){
		CountDownTimer blockTimer = new CountDownTimer(500, 500){

			@Override
			public void onFinish() {
				invalidateBlocks(goodSelectionBlocks);
				invalidateBlocks(badSelectionBlocks);
			}

			@Override
			public void onTick(long millisUntilFinished) {
				
			}
			
		};
		blockTimer.start();
	}
	
	private void invalidateBlocks(ArrayList<Rect> rects){
		
		for (int i = 0; i < rects.size(); i++){
			Rect block = rects.get(i);
			invalidate(block);
		}
		
		rects.clear();
		
			
	}
	
	private boolean blockIsAdjacentToLastTouchedBlock(float xBlock, float yBlock){
		int lastTouchedBlockIndex = selectedBlocks.size() - 1;
		Rect lastTouchedBlock = selectedBlocks.get(lastTouchedBlockIndex);
		int lastTouchedXBlock = lastTouchedBlock.left / blockWidth;
		int lastTouchedYBlock = lastTouchedBlock.top / blockWidth;
		
		int distxToX = Math.abs((int)xBlock - lastTouchedXBlock);
		int distyToY = Math.abs((int)yBlock - lastTouchedYBlock);
		
		if(distxToX > 1 || distyToY > 1){
			return false;
		}
		else{
			return true;
		}
		
	}
	
	/**
	 * Starts polling and continues until cancelled
	 * checks "opponent" + "username" key
	 * updates certain game state variables based on value gotten,
	 * and animates the board based on new words the opponent has selected
	 * @param key
	 */
	private void startPollingServer(final String key, final PersBoggleGame game){
		//final PersBoggleGameView gameView = (PersBoggleGameView) findViewById(R.id.pers_boggle_game_view);
		
		AsyncTask<String, Void, Void> pollingServer = new AsyncTask<String, Void, Void>(){
			@Override
			protected Void doInBackground(String... params) {
				String key = params[0];
				Log.d("PersBoggleGame", "Starting to poll server for " + key);
				
				//poll server maximum of once every 500ms
				while(true){
					if (isCancelled() == true){
						if(KeyValueAPI.isServerAvailable()){
							//TODO move this somewhere else?
							KeyValueAPI.clearKey("allenmic", "allenmic", key);
						}
						break;
					}
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						
					}
					
					resetOtherUserBlocks();
					
					if(KeyValueAPI.isServerAvailable()){
						String json = KeyValueAPI.get("allenmic", "allenmic", key);
						
						if (json != null && json != ""){

							
							Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
							PersBoggleGameState opponentGame = gson.fromJson(json, PersBoggleGameState.class);
							
							if(opponentGame.gameVersion > opponentVersion){
								Log.d(TAG, "got new opponent state: " + json);
								//TODO
								//set opponent score
								//check game state vars
								
								//animateOpponentSelection(opponentGame.blockSelection);
								
								if (opponentGame.priorChosenWords != null && !opponentGame.priorChosenWords.isEmpty()){
									PersGlobals.getGlobals().setOpponentPriorWords(opponentGame.priorChosenWords);
								}
								
								if (opponentGame.foundWords != ""){
									PersGlobals.getGlobals().setOpponentPriorWordString(opponentGame.foundWords);
								}
								
								if (opponentGame.score > 0){
									Log.d(TAG, "opponent score is now " + opponentGame.score);
									boolean b = game.handler.sendEmptyMessage(opponentGame.score);
									Log.d(TAG, "message placing successful? " + b);
									
								}
								
								opponentVersion = opponentGame.gameVersion;
							}
								
						}
					}
				}
				
				return null;
				
				
			}
			
		};
		pollingServer.execute(key);
		PersGlobals.getGlobals().setPollingTask(pollingServer);
	}
	
	private void resetOtherUserBlocks(){//reset the opponent selected blocks
		for(int i = 0; i < goodOtherUserBlocks.size(); i++){						
			Rect xy = goodOtherUserBlocks.get(i);
			Log.d("", "invaliding blocks again at " + xy.left + " " + xy.top);
			postInvalidate(xy.left, xy.top, xy.right, xy.bottom);
		}
		for(int i = 0; i < badOtherUserBlocks.size(); i++){						
			Rect xy = badOtherUserBlocks.get(i);
			Log.d("", "invaliding blocks again at " + xy.left + " " + xy.top);
			postInvalidate(xy.left, xy.top, xy.right, xy.bottom);
		}
		goodOtherUserBlocks.clear();
		badOtherUserBlocks.clear();
		}
	
	/**
	 * invalidates the blocks in goodSelection, with a delay
	 * the delay gives some semblance of animation
	 */
	private void animateOpponentSelection(int[][] opponentSelection){
		//TODO replaces otherUserBlocks, so might miss some opponent actions
		if (opponentSelection != null){
			for (int x = 0; x < PersGlobals.getGlobals().getNumberOfBlocks(); x++){
				for (int y = 0; y < PersGlobals.getGlobals().getNumberOfBlocks(); y++){
					int blockAtXY = opponentSelection[x][y];
					if (blockAtXY == 1 || blockAtXY == -1){
						Log.d(TAG, "going to invalidate at " + x + " " + y);
						//change rect dimensions from normalized blocks to pixels
						Rect xy = new Rect();
						xy.top = y * blockWidth;
						xy.bottom = (y + 1) * blockWidth;
						xy.left = x * blockWidth;
						xy.right = (x + 1) * blockWidth;
						Log.d(TAG, "animating opponent selection at " + xy.left + ", " + xy.top);
						postInvalidate(xy.left, xy.top, xy.right, xy.bottom);
						if (blockAtXY == 1){
							goodOtherUserBlocks.add(xy);
						}
						else{
							badOtherUserBlocks.add(xy);
						}
					}
				}
			}
			
		}
	}
	
	private void initiateCanvasObjects(){
		blockBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_blue_matte);
		goodWordBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_green_matte);
		badWordBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_red_matte);
		selectionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.btn_orange_matte);
		boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selected = new Paint();
		goodWordSelection = new Paint();
		goodOtherUserSelection = new Paint();
		badOtherUserSelection = new Paint();
		letterColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		letterColor.setColor(getResources().getColor(
		           R.color.pers_boggle_letter_text));
		letterColor.setStyle(Style.FILL);
		letterColor.setTextAlign(Paint.Align.CENTER);
		letterColor.setTypeface(Typeface.DEFAULT_BOLD);
		blockRect = new Rect();
	}
}