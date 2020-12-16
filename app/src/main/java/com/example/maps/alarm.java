package com.example.maps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class alarm extends AppCompatActivity {
    MediaPlayer Alarmplay;
    boolean play;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        play = true;
        Alarmplay = MediaPlayer.create(this,R.raw.alarm);//알람
        Alarmplay.start();//시작
        Alarmplay.setLooping(play);//지속 반복

        Button button = (Button)findViewById(R.id.home);

        button.setOnClickListener(new View.OnClickListener() {//시작
            public void onClick(View v) {//버튼 클릭시 처음 화면으로 넘어가면서 음악 종료
                Alarmplay.stop();
                Alarmplay.reset();
                Alarmplay.release();
                play = false;
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
    }
}
