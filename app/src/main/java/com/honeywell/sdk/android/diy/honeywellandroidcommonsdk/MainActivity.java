package com.honeywell.sdk.android.diy.honeywellandroidcommonsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.honeywell.sdk.android.diy.scanlib.IScan;
import com.honeywell.sdk.android.diy.scanlib.ScanLib;

public class MainActivity extends AppCompatActivity {

    private Button btnclear;//清空按钮
    private Button btninitScan;//初始化扫描按钮
    private Button btncloseScan;//关闭扫描按钮
    private Button btnstartscan;//扫描
    private Button btnstopscan;//停止扫描
    private Button btnsetDelay;//设定连续扫描时间

    private Switch switch1;//广播开关
    private Switch switch2;//白光开关
    private Switch switch3;//红光开关
    private Switch switch4;//声音开关
    private Switch switch5;//回车符开关
    private Switch switch6;//回车符开关
    private Switch switch7;//连续扫描开关
    private Switch switch8;//启用扫描按键开关

    private EditText editText;//扫码输入框
    private EditText editText2;//设定连续扫描时间间隔

    private ScanLib scanLib;//扫码库

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化View
        initView();

        //初始化扫描库
        ScanLib.init(this);

    }


    //初始化界面
    private void initView() {
        editText = (EditText) findViewById(R.id.edittext);
        btnclear = (Button) findViewById(R.id.btnclear);
        btnclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });

        btnstartscan = (Button)findViewById(R.id.btnstartscan);
        btnstopscan = (Button)findViewById(R.id.btnstopscan);
        btnstartscan.setOnClickListener(onClickListenerstartscan);
        btnstopscan.setOnClickListener(onClickListenerstopscan);

        btninitScan = (Button) findViewById(R.id.btnOpenScan);
        btninitScan.setOnClickListener(onClickListenerinitScan);
        btncloseScan = (Button) findViewById(R.id.btnCloseScan);
        btncloseScan.setOnClickListener(onClickListenercloseScan);

        editText2 = (EditText)findViewById(R.id.edittext2);
        btnsetDelay = (Button)findViewById(R.id.btnsetdelay);
        btnsetDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLib.continueDelay = Integer.valueOf(editText2.getText().toString());
            }
        });


        switch1= (Switch)findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.isBroadCastMode = b;
            }
        });

        switch2= (Switch)findViewById(R.id.switch2);
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.isLight = b;
            }
        });

        switch3= (Switch)findViewById(R.id.switch3);
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.isAim = b;
            }
        });

        switch4= (Switch)findViewById(R.id.switch4);
        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.setOpenScanSound(b);
            }
        });

        switch5= (Switch)findViewById(R.id.switch5);
        switch5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.addEnterString = b;
            }
        });

        switch6= (Switch)findViewById(R.id.switch6);
        switch6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.setCENTER_DECODE(b);
            }
        });

        switch7= (Switch)findViewById(R.id.switch7);
        switch7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.setContinueScan(b);


            }
        });

        switch8= (Switch)findViewById(R.id.switch8);
        switch8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                scanLib.setScanKeyEnable(b);


            }
        });

    }

    /**
     * 开始扫描
     */
    View.OnClickListener onClickListenerstartscan =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            scanLib.startScan();
        }
    };

    /**
     * 停止扫描
     */
    private View.OnClickListener onClickListenerstopscan =new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            scanLib.stopScan();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

        //恢复扫码头
        if (scanLib != null)
            scanLib.onResumeHandle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //释放扫描头
        if (scanLib != null)
            scanLib.onPauseHandle();
    }

    /**
     * 初始化扫描点击
     */
    private View.OnClickListener onClickListenerinitScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //初始化扫码库
            scanLib = ScanLib.getIntance();
            scanLib.setIScan(iScan);

            //注册广播
            IntentFilter intentFilter = new IntentFilter(ScanLib.action);
            registerReceiver(broadcastReceiver, intentFilter);
        }
    };


    /**
     * 广播接收器
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction() == ScanLib.action) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String s = editText.getText().toString() + "来自广播:" +
                                intent.getStringExtra("data") ;

                        editText.setText(s);
                    }
                });
            }
        }
    };


    /**
     * 扫描接口回调
     */
    IScan iScan = new IScan() {
        @Override
        public void onErrMsg(String errMsg, String sdkMsg) {

        }

        @Override
        public void onInitSuccess() {

        }


        //获得扫码数据
        @Override
        public void onDecode(final String barcode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String s = editText.getText().toString() + "来自接口:" +
                            barcode ;

                    editText.setText(s);
                }
            });
        }
    };

    /**
     * 关闭扫描
     */
    private View.OnClickListener onClickListenercloseScan = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //释放扫码库
            scanLib.unInitScan();
            //反注册广播
            unregisterReceiver(broadcastReceiver);
        }
    };
}
