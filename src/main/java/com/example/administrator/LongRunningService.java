package com.example.administrator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class LongRunningService extends Service{
    int mk;
    int hour_LRS;
    int minute_LRS;
    int frequency_LRS;
    private final static String TAG = "main";

    @Override
    public IBinder onBind(Intent intent){
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"service is created");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notify=null;
            NotificationChannel notificationChannel = new NotificationChannel("mk","Default Channel",NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(notificationChannel);
            Notification.Builder builder = new Notification.Builder(this,"mk");
            builder.setChannelId("mk");
            notify = builder.build();
            startForeground(1,notify);
            stopSelf();
        }

    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId){

        mk = intent.getIntExtra("mk",1);
        hour_LRS = intent.getIntExtra("hour",9);
        minute_LRS = intent.getIntExtra("minute",0);
        frequency_LRS = intent.getIntExtra("frequency",30*60*1000);
        Log.i(TAG,"service is started");

        long systemTime = System.currentTimeMillis();//距离1970年后的毫秒数
        Calendar calendar = Calendar.getInstance();
        Calendar calendar1 = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar1.setTimeInMillis(System.currentTimeMillis());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar1.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.MINUTE,minute_LRS);
        calendar.set(Calendar.HOUR_OF_DAY,hour_LRS);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar1.set(Calendar.MINUTE,30);
        calendar1.set(Calendar.HOUR_OF_DAY,23);
        calendar1.set(Calendar.SECOND,0);
        calendar1.set(Calendar.MILLISECOND,0);
        //计算出设定的时间，获取设置后的时间
        long selectTime = calendar.getTimeInMillis();
        long selectTime1 = calendar1.getTimeInMillis();
        if (mk==1){
            //如果当前时间大于设定的时间，则推迟一天执行
            if(systemTime>=selectTime){
                calendar.add(Calendar.DAY_OF_MONTH,1);
                calendar1.add(Calendar.DAY_OF_MONTH,1);
                selectTime = calendar.getTimeInMillis();
                selectTime1 = calendar1.getTimeInMillis();
            }
        }
        long intervalTimeMills = frequency_LRS*60*1000*mk;
        mk+=1;
        if (intervalTimeMills>=(selectTime1-selectTime)){
            mk=1;
        }

        Intent intent1 = new Intent(LongRunningService.this,MainActivity.AlarmReceiver.class);
        intent1.putExtra("mk1",mk);
        intent1.putExtra("hour2AlarmReceiver",hour_LRS);
        intent1.putExtra("minute2AlarmReceiver",minute_LRS);
        intent1.putExtra("frequency2AlarmReceiver",frequency_LRS);
        intent1.setAction("action1");
        PendingIntent pi = PendingIntent.getBroadcast(LongRunningService.this,mk,intent1,0);
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (manager!=null) {
            manager.setWindow(AlarmManager.RTC_WAKEUP, selectTime+intervalTimeMills-frequency_LRS*60*1000, 1000 * 60, pi);
        }
        return super.onStartCommand(intent,Service.START_STICKY_COMPATIBILITY,startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"service is destroied");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            stopForeground(true);
        }
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent i = new Intent(this,MainActivity.AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this,mk,i,0);
        manager.cancel(pi);
    }
}

