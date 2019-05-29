package com.example.administrator;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent intent;
    private Calendar calendar;
    private int hour;
    private int minute;
    private int time_frequency;
    private int mType = 1;
    private String[] frequency = {"10分钟","30分钟","40分钟","60分钟"};
    private static String event;

    public static int stop_service = 1;
    private final static String TAG = "main";

    private Button btn_cancel;
    private Button btn_continue;
    private Button btn_makesure;
    private EditText et_event;
    private Spinner sp_frequency;
    private TimePicker tp_time;
    private TextView tv_time2,tv_currentEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();//获取当前时间

        Log.i(TAG,"activity is created");
        setContentView(R.layout.activity_main);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_continue = (Button) findViewById(R.id.btn_continue);
        btn_makesure = (Button) findViewById(R.id.btn_makesure);
        btn_cancel.setOnClickListener(this);
        btn_continue.setOnClickListener(this);
        btn_makesure.setOnClickListener(this);
        tv_time2 = (TextView) findViewById(R.id.tv_time2);
        et_event = (EditText) findViewById(R.id.et_event);
        tv_currentEvent = (TextView) findViewById(R.id.tv_currentEvent);
        sp_frequency = (Spinner) findViewById(R.id.sp_frequency);
        initTypeSpinner();//初始化Spinner
        ////使用SharedPreferences获取数据
        SharedPreferences sharedPreferences1 = getSharedPreferences("myData",Context.MODE_PRIVATE);
        mType = sharedPreferences1.getInt("typeNumber",1);
        sp_frequency.setSelection(mType);
        hour = sharedPreferences1.getInt("hour",calendar.get(Calendar.HOUR_OF_DAY));
        minute = sharedPreferences1.getInt("minute",calendar.get(Calendar.MINUTE));
        event = sharedPreferences1.getString("et_event","");
        et_event.setText(event);
        //使用savedInstanceState获取数据
        if (savedInstanceState != null){
            mType = savedInstanceState.getInt("typeNumber");
            sp_frequency.setSelection(mType);
            et_event.setText(savedInstanceState.getString("et_event"));
            calendar.set(Calendar.MINUTE,savedInstanceState.getInt("minute"));
            calendar.set(Calendar.HOUR_OF_DAY,savedInstanceState.getInt("hour"));
        }
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        String desc = String.format("您选择的时间是%d时%d分", hour, minute);
        tv_time2.setText(desc);
        tp_time = (TimePicker) findViewById(R.id.tp_time);
        //设置点击事件不弹键盘
        tp_time.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        tp_time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int min) {
                hour = hourOfDay;
                minute = min;
                String desc = String.format("您选择的时间是%d时%d分", hour, minute);
                tv_time2.setText(desc);
            }
        });
    }
    private void initTypeSpinner(){
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(MainActivity.this,R.layout.item_select,frequency);
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp_frequency.setPrompt("请选择提示频率");
        sp_frequency.setAdapter(typeAdapter);
        sp_frequency.setSelection(mType);
        sp_frequency.setOnItemSelectedListener(new TypeSelectedListener());
    }
    // 定义用户类型的选择监听器
    class TypeSelectedListener implements AdapterView.OnItemSelectedListener {
        // 选择事件的处理方法，其中arg2代表选择项的序号
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mType = arg2;
        }
        // 未选择时的处理方法，通常无需关注
        public void onNothingSelected(AdapterView<?> arg0) {}
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_makesure:
                if (tv_currentEvent.getText().toString().equals("")){
                    if (!et_event.getText().toString().equals("")){
                        stop_service = 1;
                        event = et_event.getText().toString();
                        tv_currentEvent.setText("起始时间："+hour+":"+minute+"，"+"频率："+sp_frequency.getSelectedItem().toString()+"，"+"当前事件："+event);
                        //使用SharedPreferences保存数据，其背后是用xml文件存放数据，文件存放在/data/data/<package name>/shared_prefs目录下
                        SharedPreferences sharedPreferences = getSharedPreferences("myData",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("typeNumber",sp_frequency.getSelectedItemPosition());
                        editor.putInt("hour",hour);
                        editor.putInt("minute",minute);
                        editor.putString("et_event",event);
                        editor.apply();

                        time_frequency = Integer.parseInt(sp_frequency.getSelectedItem().toString().trim().substring(0,2));
                        intent = new Intent(MainActivity.this,LongRunningService.class);
                        intent.putExtra("hour",hour);
                        intent.putExtra("minute",minute);
                        intent.putExtra("frequency",time_frequency);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            startForegroundService(intent);
                        } else {
                            startService(intent);
                        }
                        Toast.makeText(MainActivity.this,"提醒的功能已开启",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,"请输入提醒内容",Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this,"请先取消当前提醒",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.btn_cancel:
                stop_service = 0;
                tv_currentEvent.setText("");
                break;
            case R.id.btn_continue:
                stop_service = 1;
                event = et_event.getText().toString();
                tv_currentEvent.setText("起始时间："+hour+":"+minute+"，"+"频率："+sp_frequency.getSelectedItem().toString()+"，"+"当前事件："+event);
                time_frequency = Integer.parseInt(sp_frequency.getSelectedItem().toString().trim().substring(0,2));
                intent = new Intent(MainActivity.this,LongRunningService.class);
                intent.putExtra("hour",hour);
                intent.putExtra("minute",minute);
                intent.putExtra("frequency",time_frequency);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        Log.i(TAG,"onSaveInstanceState is saved");
        super.onSaveInstanceState(outState);
        outState.putString("et_event",event);
        outState.putInt("typeNumber",sp_frequency.getSelectedItemPosition());
        outState.putInt("hour",hour);
        outState.putInt("minute",minute);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        Log.i(TAG,"onRestoreInstanceState is saved");
        super.onRestoreInstanceState(savedInstanceState);
        mType = savedInstanceState.getInt("typeNumber");
        sp_frequency.setSelection(mType);
        et_event.setText(savedInstanceState.getString("et_event"));
        calendar.set(Calendar.MINUTE,savedInstanceState.getInt("minute"));
        calendar.set(Calendar.HOUR_OF_DAY,savedInstanceState.getInt("hour"));
    }
    @Override
    protected void onDestroy() {
        intent = new Intent(MainActivity.this, LongRunningService.class);
        stopService(intent);
        super.onDestroy();
        Log.i(TAG,"activity is destroied");
    }

    //有些软件当我们在主界面按系统返回键的时候是不会销毁当前Activity的，具体实现方式如下：
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (stop_service != 0) {
                if ("action1".equals(intent.getAction())) {
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notify=null;

                    Intent clickIntent = new Intent(Intent.ACTION_MAIN);
                    clickIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    clickIntent.setComponent(new ComponentName(context,MainActivity.class));
                    clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    PendingIntent contentIntent = PendingIntent.getActivity(context,
                            R.string.app_name, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.notification);
                    remoteViews.setTextViewText(R.id.tv_notification,event);
                    remoteViews.setTextColor(R.id.tv_notification, ContextCompat.getColor(context,R.color.purple));
                    remoteViews.setTextViewTextSize(R.id.tv_notification, TypedValue.COMPLEX_UNIT_SP,25);
                    remoteViews.setImageViewResource(R.id.iv_notification,R.drawable.strive);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        NotificationChannel notificationChannel = new NotificationChannel("default","Default Channel",NotificationManager.IMPORTANCE_HIGH);
                        notificationChannel.setDescription("description");
                        notificationChannel.enableLights(true);
                        manager.createNotificationChannel(notificationChannel);
                        Notification.Builder builder = new Notification.Builder(context,"default");
                        builder.setCustomContentView(remoteViews)
                                .setContentIntent(contentIntent)
                                .setSmallIcon(R.drawable.ic_app)//一定要有
                                .setAutoCancel(true)
                                .setTicker("有待做事件提醒")
                                .setWhen(System.currentTimeMillis())
                                .setChannelId("default");
                        notify = builder.build();
                        manager.notify(R.string.app_name, notify);
                        Intent i = new Intent(context, LongRunningService.class);
                        i.putExtra("mk", intent.getIntExtra("mk1", 2));
                        i.putExtra("hour",intent.getIntExtra("hour2AlarmReceiver",9));
                        i.putExtra("minute",intent.getIntExtra("minute2AlarmReceiver",0));
                        i.putExtra("frequency",intent.getIntExtra("frequency2AlarmReceiver",30*60*1000));
                        System.out.println("AlarmReceiver:" + intent.getIntExtra("mk1", 2));
                        context.startForegroundService(i);
                    } else{
                        Notification.Builder builder = new Notification.Builder(context);
                        builder.setContentIntent(contentIntent)
                                .setAutoCancel(true)
                                .setTicker("有待做事件提醒")
                                .setWhen(System.currentTimeMillis())
                                .setSmallIcon(R.drawable.ic_app)
                                .setContent(remoteViews)
                                .setDefaults(Notification.DEFAULT_ALL);
                        notify = builder.build();

                        manager.notify(R.string.app_name, notify);
                        Intent i = new Intent(context, LongRunningService.class);
                        i.putExtra("mk", intent.getIntExtra("mk1", 2));
                        i.putExtra("hour",intent.getIntExtra("hour2AlarmReceiver",9));
                        i.putExtra("minute",intent.getIntExtra("minute2AlarmReceiver",0));
                        i.putExtra("frequency",intent.getIntExtra("frequency2AlarmReceiver",30*60*1000));
                        System.out.println("AlarmReceiver:" + intent.getIntExtra("mk1", 2));
                        context.startService(i);
                    }
                }
            }
        }
    }


    /**
     * 点击空白位置 隐藏软键盘
     */
    public boolean onTouchEvent(MotionEvent event) {
        if(null != this.getCurrentFocus()){
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super .onTouchEvent(event);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {


                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
// 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }
    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
// 获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
// 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}

