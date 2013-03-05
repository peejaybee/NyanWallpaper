package com.pjbsoftware.nyanwallpaper;

import java.io.IOException;
import java.io.InputStream;

import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * 
 */

/**
 * @author pbirmi02
 * 
 */
public class NyanNyanService extends WallpaperService {
	static final String TAG = "NYAN";
	static final Handler mNyanHandler = new Handler();

	/**
	 * @see android.service.wallpaper.WallpaperService#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * @see android.service.wallpaper.WallpaperService#onCreateEngine()
	 */
	@Override
	public Engine onCreateEngine() {
//		android.os.Debug.waitForDebugger();
		try {
			return new NyanEngine();
		} catch (IOException e) {
			Log.w(TAG, "Error creating NyanEngine", e);
			stopSelf();
			return null;
		}
	}

	class NyanEngine extends Engine {
		private final Movie mNyan;
		private final int mNyanDuration;
		private final Runnable mNyanNyan;
		float mScaleX;
		float mScaleY;
		int mWhen = 0;
		long mStart;

		NyanEngine() throws IOException {
			InputStream is = getResources().openRawResource(R.raw.nyan);
			if (is != null) {
				try {
					mNyan = Movie.decodeStream(is);
					mNyanDuration = mNyan.duration();
				} finally {
					is.close();
				}
			} else {
				throw new IOException("Unable to open R.raw.nyan");
			}

			mNyanNyan = new Runnable() {
				public void run() {
					drawFrame();
				}
			};
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			mNyanHandler.removeCallbacks(mNyanNyan);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			if (visible) {
				drawFrame();
			} else {
				mNyanHandler.removeCallbacks(mNyanNyan);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			mScaleX = width / (1f * mNyan.width());
			mScaleY = height / (1f * mNyan.height());
			drawFrame();
		}
		
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mNyanHandler.removeCallbacks(mNyanNyan);
        }

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset,
				float xOffsetStep, float yOffsetStep, int xPixelOffset,
				int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep,
					xPixelOffset, yPixelOffset);
			drawFrame();
		}

		void drawFrame() {
			//not even going to try to start at the beginning --- it loops
			//so why do math every frame just to get the first loop perfect?
			mWhen = (int) (SystemClock.uptimeMillis() % mNyanDuration);
			SurfaceHolder surfaceHolder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = surfaceHolder.lockCanvas();
				if (canvas != null) {
					drawImage(canvas);
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
			mNyanHandler.removeCallbacks(mNyanNyan);
			if (isVisible()) {
				mNyanHandler.postDelayed(mNyanNyan, 1000L / 25L);
			}
		}

		void drawImage(Canvas canvas) {
			canvas.save();
			canvas.scale(mScaleX, mScaleY);
			mNyan.setTime(mWhen);
			mNyan.draw(canvas, 0, 0);
			canvas.restore();
		}
	}
}