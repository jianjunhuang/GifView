package com.jianjunhuang.gifview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

  private PlayGifView mGifView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mGifView = findViewById(R.id.gif_view);
    mGifView.setImageResource("https://media.giphy.com/media/lJuKVbIA2dYqc/giphy.gif");
  }
}
