package com.example.arsal.gameboi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    //Code from this program has been used from "Beginning Android Games" by Mario Zechner
    //Review SurfaceView, Canvas, continue

    GameSurface gameSurface;
    ArrayList<Integer> list = new ArrayList();
    ArrayList<Integer> list2 = new ArrayList();
    ArrayList<Boolean> list3 = new ArrayList();
    float value = 250;
    double x;
    int ay = -200;
    int score,time = 3000;
    boolean hit, touch;
    SoundPool soundPool;
    int hitID;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);

        gameSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Eggus", "touched");
                if (touch == false)
                    touch = true;
                else touch = false;
            }
        });

        SensorManager sensorManager =  (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,accelerometerSensor,SensorManager.SENSOR_DELAY_NORMAL);

        SoundPool.Builder builder = new SoundPool.Builder(); //creates builder
        builder.setMaxStreams(2); // how many sound effects can be played at the same time?
        soundPool = builder.build(); //create a SoundPool using the builder

        MediaPlayer player = MediaPlayer.create(MainActivity.this, R.raw.music);
        player.start();

        hitID = soundPool.load(this,R.raw.shock,2);
    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        x = sensorEvent.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap myImage,myImage2,myImage3,asteroid;
        Paint paintProperty;

        int screenWidth;
        int screenHeight;
        int count;

        public GameSurface(Context context) {
            super(context);

            holder=getHolder();

            myImage = BitmapFactory.decodeResource(getResources(),R.drawable.ship);
            myImage2 = BitmapFactory.decodeResource(getResources(),R.drawable.shipb);
            myImage3 = BitmapFactory.decodeResource(getResources(),R.drawable.shipc);

            asteroid = BitmapFactory.decodeResource(getResources(),R.drawable.asteroid);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(100);
            paintProperty.setColor(Color.WHITE);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {

            while (running == true){

                if (holder.getSurface().isValid() == false)
                    continue;

                Canvas canvas= holder.lockCanvas();
                canvas.drawRGB(155,55,155);
                canvas.drawText("Score: " + score, 50, 200, paintProperty);
                //canvas.drawText("x: "+x,50,320,paintProperty);

                if ((value-x > 615) || (value-x < -80)){ //out of bounds + tilt multiplier
                    value = value;
                }
                else {
                    value -= x*1.5;
                }

                if (count < 0){ //hit
                    canvas.drawBitmap(myImage3,value,800,null);
                }
                else if (count < 30){ //engine
                    canvas.drawBitmap(myImage,value,800,null);
                }
                else if (count < 90){ //no engine
                    canvas.drawBitmap(myImage2,value,800,null);
                }

                else{ //new asteroid
                    canvas.drawBitmap(myImage,value,800,null);
                    count = 0;
                    int ax = (int) (value + ((Math.random()*500)-50));
                    int ay = -250;
                    list.add(ax);
                    list2.add(ay);
                    list3.add(false);
                    canvas.drawBitmap(asteroid,ax,ay,null);
                }

                if (touch == false){ //accelerator
                    count++;
                }
                else{
                    paintProperty.setTextSize(50);
                    paintProperty.setColor(Color.YELLOW);
                    canvas.drawText("Turbo Mode Activated", 80, 600, paintProperty);
                    paintProperty.setTextSize(100);
                    paintProperty.setColor(Color.WHITE);
                    count+=2;
                }

                time-=2;
                paintProperty.setTextSize(80);
                canvas.drawText("Time: "+time/100, 50, 300, paintProperty);
                paintProperty.setTextSize(100);

                if (time < 0){
                    paintProperty.setTextSize(200);
                    paintProperty.setColor(Color.CYAN);
                    canvas.drawText("Game Over", 50, 900, paintProperty);
                    running = false;
                }

                for (int i = 0;i<list.size();i++){
                    canvas.drawBitmap(asteroid,list.get(i),list2.get(i),null);

                    if ((list.get(i) > value+100) && (list.get(i) < value+310)) {

                        if ((list2.get(i) > 750) && (list2.get(i) < 1050)) {
                            hit = true;
                            if (count >= 0) {
                                soundPool.play(hitID, 1, 1, 1, 0, 1);
                                count = -60;
                            }
                            list3.set(i,true);
                            canvas.drawText("Score: " + score, 50, 200, paintProperty);

                        }
                        else {
                            hit = false;
                            canvas.drawText("Score: " + score, 50, 200, paintProperty);
                        }
                    }
                    else {
                        hit = false;
                        canvas.drawText("Score: " + score, 50, 200, paintProperty);
                    }

                    if (touch == false){ //asteroid fall speed
                        list2.set(i,list2.get(i)+8);
                    }
                    else list2.set(i,list2.get(i)+16);

                    if (list2.get(i) > 1650){ //asteroid removal + score
                        list.remove(i);
                        list2.remove(i);
                        if (list3.get(i) == false){
                            list3.remove(i);
                            score++;
                        }
                        else{
                            list3.remove(i);
                            score=score;
                        }
                    }
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

    }//GameSurface
}//Activity

