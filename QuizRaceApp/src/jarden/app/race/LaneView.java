package jarden.app.race;

import com.jardenconsulting.quizrace.MainActivity;
import com.jardenconsulting.quizrace.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LaneView extends View {
	private int cellSize;
	private int border;
	private final static Paint gridPaint;
	private final static Paint facePaint;
	private final static Paint lastCellPaint;
	private Paint mePaint;
	private Bitmap meBitmap = null;
	private int xposition = 0;
	private int status = GameData.RUNNING;

	static {
		gridPaint = new Paint();
		gridPaint.setColor(Color.BLACK);
		gridPaint.setStyle(Paint.Style.STROKE);
		gridPaint.setStrokeWidth(2);
		lastCellPaint = new Paint();
		lastCellPaint.setColor(Color.RED);
		lastCellPaint.setStyle(Paint.Style.STROKE);
		lastCellPaint.setStrokeWidth(1);
		facePaint = new Paint();
		facePaint.setColor(Color.BLACK);
	}

	public LaneView(Context context) {
		super(context);
	}
	public LaneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources res = getResources();
		cellSize = res.getDimensionPixelSize(R.dimen.cellSize);
		if (cellSize == 0) cellSize = 24;
		border = res.getDimensionPixelSize(R.dimen.border);
		if (border == 0) border = 4;
		mePaint = new Paint();
	}
	public void setAttributes(int color) {
		mePaint.setColor(color);
	}
	public void setBitmapId(int bitmapId) {
		this.meBitmap = BitmapFactory.decodeResource(
				getResources(), bitmapId);
	}
	public int moveOn() {
		++this.xposition;
		invalidate();
		return this.xposition;
	}
	public void reset() {
		this.xposition = 0;
		invalidate();
	}
	public int getPosition() {
		return this.xposition;
	}
	public void setStatus(int status) {
		this.status = status;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(cellSize * MainActivity.LANE_LENGTH,
				0, cellSize * (MainActivity.LANE_LENGTH+1), cellSize, lastCellPaint);
		canvas.drawRect(0, 0, cellSize * MainActivity.LANE_LENGTH, cellSize, gridPaint);
		for (int i = 0; i < MainActivity.LANE_LENGTH; i++) {
			canvas.drawLine(cellSize * i, 0, cellSize * i, cellSize, gridPaint);
		}
		float tx = cellSize * xposition + (cellSize / 2);
		float ty =  cellSize / 2;
		float radius = (cellSize - border) / 2;
		if (this.meBitmap != null) {
			canvas.drawBitmap(this.meBitmap, cellSize * xposition + 2, 2, mePaint);
		} else {
			canvas.drawCircle(tx, ty, radius, mePaint);
			float ix = cellSize * (xposition + 1) - cellSize / 3;
			float iy = cellSize * 0.4F;
			float iradius = cellSize / 8;
			canvas.drawCircle(ix, iy, iradius, facePaint);
			float mx = ix;
			float my = cellSize * 0.6F;
			float mlen = cellSize / 4;
			canvas.drawLine(mx, my, mx + mlen, my, facePaint);
		}
		if (this.status == GameData.CAUGHT) {
			canvas.drawLine(tx - radius, ty + radius, tx + radius, ty - radius, gridPaint);
		}
	}
	public void setData(GameData gameData) {
		this.xposition = gameData.position;
		this.status = gameData.status;
		this.invalidate();
	}
}
