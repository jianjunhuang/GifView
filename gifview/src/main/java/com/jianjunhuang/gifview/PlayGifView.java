package com.jianjunhuang.gifview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlayGifView extends android.support.v7.widget.AppCompatImageView {

  private static final int DEFAULT_MOVIEW_DURATION = 1000;

  private int mMovieResourceId;
  private Movie mMovie;

  private long mMovieStart = 0;
  private int mCurrentAnimationTime = 0;

  private Handler mHandler = new Handler();

  @SuppressLint("NewApi")
  public PlayGifView(Context context, AttributeSet attrs) {
    super(context, attrs);

    /**
     * Starting from HONEYCOMB have to turn off HardWare acceleration to draw
     * Movie on Canvas.
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }
  }

  public void setImageResource(int mvId) {
    this.mMovieResourceId = mvId;
    mMovie = Movie.decodeStream(getResources().openRawResource(mMovieResourceId));
    requestLayout();
  }

  public void setImageResource(InputStream stream) {
    mMovie = Movie.decodeStream(stream);
    requestLayout();
  }

  public void setImageResource(final String url) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
          final InputStream inputStream = connection.getInputStream();
          String type = url.substring(url.lastIndexOf(".")+1);
          if ("gif".equals(type)) {
            mMovie = Movie.decodeStream(inputStream);
            mHandler.post(new Runnable() {
              @Override
              public void run() {
                requestLayout();
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
    if (mMovie != null) {
      updateAnimationTime();
      drawGif(canvas);
      invalidate();
    } else {
      super.onDraw(canvas);
    }
  }

  private void updateAnimationTime() {
    long now = android.os.SystemClock.uptimeMillis();

    if (mMovieStart == 0) {
      mMovieStart = now;
    }
    int dur = mMovie.duration();
    if (dur == 0) {
      dur = DEFAULT_MOVIEW_DURATION;
    }
    mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
  }

  private void drawGif(Canvas canvas) {
    mMovie.setTime(mCurrentAnimationTime);
    mMovie.draw(canvas, 0, 0);
    canvas.restore();
  }

}