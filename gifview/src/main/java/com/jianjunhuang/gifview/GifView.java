package com.jianjunhuang.gifview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 1. resume or restart play
 * 2. place holder
 * 3. onStart,onDrawing,onFinished
 * 4. play model: repeat、times、recycle
 */
public class GifView extends android.support.v7.widget.AppCompatImageView {

  private static final String TAG = "GifView";

  private static final int DEFAULT_MOVIE_DURATION = 1000;

  private Movie mMovie;

  private long mMovieStart = 0;
  private float mCurrentAnimationTime = 0;

  private Handler mHandler = new Handler();

  private OnGifStateListener mOnGifSateListener;

  private int mTimes = Integer.MAX_VALUE;

  private int mExecutedTimes = 0;

  private boolean isStart = false;

  public static final int MARGIN = 10;

  private Bitmap mPlaceHolderBmp;

  public GifView(Context context, AttributeSet attrs) {
    super(context, attrs);

    /*
     * Starting from HONEYCOMB have to turn off HardWare acceleration to draw
     * Movie on Canvas.
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }

  public void setImageResource(@RawRes int mvId) {
    mMovie = Movie.decodeStream(getResources().openRawResource(mvId));
    requestLayout();
  }

  public void setImageResource(InputStream stream) {
    mMovie = Movie.decodeStream(stream);
    start();
  }

  public void setImageResource(final String url) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
          final InputStream inputStream = connection.getInputStream();
          String type = url.substring(url.lastIndexOf(".") + 1);
          if ("gif".equals(type)) {
            mMovie = Movie.decodeStream(inputStream);
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                setImageResource(inputStream);
              }
            });
          } else {
            final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                setImageBitmap(bitmap);
                requestLayout();
              }
            });
          }

        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public void setPlaceHolder(int id) {
    Bitmap mPlaceHolderBmp = BitmapFactory.decodeResource(getResources(), id);
    setPlaceHolder(mPlaceHolderBmp);
  }

  public void setPlaceHolder(Bitmap bmp) {
    mPlaceHolderBmp = bmp;
    if (bmp != null) {
      setImageBitmap(bmp);
      requestLayout();
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (mMovie != null) {
      setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  private int measureWidth(int widthMeasureSpec) {
    int mode = MeasureSpec.getMode(widthMeasureSpec);
    int size = MeasureSpec.getSize(widthMeasureSpec);
    if (mode == MeasureSpec.EXACTLY) {
      return size;
    } else {
      if (mMovie != null) {
        if (size == 0) {
          return mMovie.width();
        }
        return mMovie.width() > size ? size : mMovie.width();
      } else {
        return size;
      }
    }
  }

  private int measureHeight(int heightMeasureSpec) {
    int mode = MeasureSpec.getMode(heightMeasureSpec);
    int size = MeasureSpec.getSize(heightMeasureSpec);
    if (mode == MeasureSpec.EXACTLY) {
      return size;
    } else {
      if (mMovie != null) {
        if (size == 0) {
          return mMovie.height();
        }
        return mMovie.height() > size ? size : mMovie.height();
      } else {
        return size;
      }
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (mMovie != null && isStart) {
      drawGif(canvas);
    } else {
      super.onDraw(canvas);
    }
  }

  private void drawGif(Canvas canvas) {
    //update time
    long now = android.os.SystemClock.uptimeMillis();

    if (mMovieStart == 0) {
      mMovieStart = now;
    }
    int dur = mMovie.duration();
    if (dur == 0) {
      dur = DEFAULT_MOVIE_DURATION;
    }
    //todo bug
    mCurrentAnimationTime = (now - mMovieStart) % (dur + MARGIN);
    Log.i(TAG,
          String.format(
              "start = %d , dur = %d , currentTime = %f ,mTimes = %d , mExecutedTime = %d ",
              mMovieStart,
              dur,
              mCurrentAnimationTime,
              mTimes,
              mExecutedTimes));

    //stop times
    if (mExecutedTimes == mTimes) {
      stop();
      return;
    }
    //draw
    mMovie.setTime((int) mCurrentAnimationTime);
    mMovie.draw(canvas, 0, 0);
    canvas.restore();

    //todo bug
    if (mCurrentAnimationTime >= dur) {
      mExecutedTimes++;
      if (mOnGifSateListener != null) {
        mOnGifSateListener.onMoving(mExecutedTimes, this);
      }
    }

    invalidate();
  }

  /**
   * start play the gif
   */
  public void start() {
    if (isStart) {
      return;
    }
    if (mMovie != null) {
      isStart = true;
      mExecutedTimes = 0;
      mMovieStart = 0;
      requestLayout();
      //make sure onDraw will be invoke
      invalidate();
      if (mOnGifSateListener != null) {
        mOnGifSateListener.onStart(mExecutedTimes, this);
      }
    } else {
      Log.e(TAG, "Movie is null");
    }
  }

  /**
   * stop play the gif
   */
  public void stop() {
    if (!isStart) {
      return;
    }
    isStart = false;
    setPlaceHolder(mPlaceHolderBmp);
    if (mOnGifSateListener != null) {
      mOnGifSateListener.onStop(mExecutedTimes, this);
    }
  }

  public OnGifStateListener getOnGifSateListener() {
    return mOnGifSateListener;
  }

  /**
   * listening the gif state .
   * NOTICE: It can't promise that the number of times is always right
   *
   * @param mOnGifSateListener
   *     listener
   */
  public void setOnGifSateListener(OnGifStateListener mOnGifSateListener) {
    this.mOnGifSateListener = mOnGifSateListener;
  }

  public int getTimes() {
    return mTimes;
  }

  /**
   * set playing times, the default number of plays is Integer.MAX_VALUE
   * NOTICE: It can't promise that the number of times is always right
   *
   * @param mTimes
   *     number of plays
   */
  public void setTimes(int mTimes) {
    this.mTimes = mTimes;
  }

  public interface OnGifStateListener {
    void onStart(int executedTimes, GifView view);

    void onStop(int executedTimes, GifView view);

    void onMoving(int executedTimes, GifView view);
  }
}