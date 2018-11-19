package com.jianjunhuang.gifview;

import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {

  private ImageView mTestImageView;
  private GifView mGifView;
  private Handler mHandler = new Handler();
  private TextView logTextView;
  private MenuItem mMenuItem;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    logTextView = findViewById(R.id.log_text_view);
    mGifView = findViewById(R.id.gif_view);
    mGifView.setImageResource(
        "https://upload.wikimedia.org/wikipedia/commons/4/43/Animation-battery_Android_8.gif");
    mGifView.setTimes(5);
    mGifView.setPlaceHolder(R.mipmap.ic_launcher);
    mGifView.setOnGifSateListener(new GifView.OnGifStateListener() {
      @Override
      public void onStart(int executedTimes, GifView view) {
        showLog("onStart " + executedTimes);
        switchState();
      }

      @Override
      public void onStop(int executedTimes, GifView view) {
        showLog("onStop " + executedTimes);
        switchState();
      }

      @Override
      public void onMoving(int executedTimes, GifView view) {
        showLog("onMoving " + executedTimes);
      }
    });


    mTestImageView = findViewById(R.id.test_view);

  }

  @Override
  protected void onResume() {
    super.onResume();

  }

  //use ImageDecoder
  private void showGif() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final HttpURLConnection connection =
              (HttpURLConnection) new URL("https://media.giphy.com/media/lJuKVbIA2dYqc/giphy.gif").openConnection();
          InputStream inputStream = connection.getInputStream();
          final byte[] bytes = new byte[inputStream.available()];
          inputStream.read(bytes);
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              try {
                Drawable drawable =
                    ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(bytes)));
                mTestImageView.setImageDrawable(drawable);
                //                drawable.start();
              } catch (IOException e) {
                e.printStackTrace();
              }

            }
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  StringBuilder sb = new StringBuilder();

  private void showLog(String log) {
    sb.append(log);
    sb.append("\n");
    logTextView.setText(sb.toString());
  }


  boolean mIsPlaying = false;

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (mIsPlaying) {
      mGifView.stop();
    } else {
      mGifView.start();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.home, menu);
    mMenuItem = menu.findItem(R.id.action);
    return super.onCreateOptionsMenu(menu);
  }

  private void switchState() {
    mIsPlaying = !mIsPlaying;
    mMenuItem.setIcon(mIsPlaying
                          ? R.drawable.ic_stop_black_24dp
                          : R.drawable.ic_play_arrow_black_24dp);
  }
}
