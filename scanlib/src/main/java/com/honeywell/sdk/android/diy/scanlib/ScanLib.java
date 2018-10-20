package com.honeywell.sdk.android.diy.scanlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.TriggerStateChangeEvent;

import java.util.Date;

/**
 * Created by yangxiaoguang on 2018/1/5.
 */

public class ScanLib {

    private static Context appContext;
    public static final String action = "com.honeywell.scan";//广播名称
    private static ScanLib scanLib;
    private boolean isScanKeyEnable = true;


    //Honeywell aidc 管理器
    private AidcManager manager = null;
    // Honeywell 扫描头读取器
    private BarcodeReader reader;


    private IScan _iScan;
    /**
     * 白光
     */
    public Boolean isLight = true;//白光
    /**
     * 红光
     */
    public Boolean isAim = true;//红光
    /**
     * 添加回车结束符
     */
    public Boolean addEnterString = true;

    /**
     * 广播模式
     */
    public Boolean isBroadCastMode = true;

    /**
     * 连续扫描
     */
    private Boolean isContinueScan = false;
    private Boolean startContinueScan = false;//连续扫描开始
    /**
     * 连续扫描频率
     */
    public int continueDelay = 300;


    public static void init(Context context) {
        appContext = context;
    }


    /**
     * 获得扫描单例
     *
     * @return
     */
    public static ScanLib getIntance() {
        if (scanLib == null) {
            scanLib = new ScanLib();
        }


        return scanLib;
    }


    /**
     * 获得扫码头名称
     *
     * @return
     */
    public String getReaderName() {
        if (reader != null) {
            try {
                return reader.getInfo().getName();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }


    /**
     * 获得扫码头ID
     *
     * @return
     */
    public String getReaderScannerId() {
        if (reader != null) {
            try {
                return reader.getInfo().getScannerId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }


    /**
     * 获得解码版本号
     *
     * @return
     */
    public String getReaderDecodeVersion() {
        if (reader != null) {
            try {
                return reader.getInfo().getFullDecodeVersion();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 反注册服务
     */
    public void unInitScan() {
        reader.release();

        reader = null;
        manager = null;
        _iScan = null;
        scanLib = null;

    }


    public ScanLib() {
        AidcManager.create(ScanLib.appContext, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                reader = manager.createBarcodeReader();
                // 设置扫描属性
//
                try {
                    // 设置扫描属性
                    reader.setProperty(BarcodeReader.PROPERTY_CODE_93_ENABLED, true);
                    reader.setProperty(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                    reader.setProperty(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false);
                    reader.setProperty(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
                    reader.setProperty(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
//                        reader.setProperty(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
                    // 设置控制类型
                    reader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                            BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
                    reader.claim();
                } catch (Exception e) {
                    e.printStackTrace();
                    if (_iScan != null) {
                        _iScan.onErrMsg("初始化扫码头失败", e.getLocalizedMessage());
                        return;
                    }
                }

                //添加扫描监听 step 4
                reader.addBarcodeListener(barcodeListener);
                //监听按扫描键 step 5
                reader.addTriggerListener(triggerListener);


                if (_iScan != null) {
                    _iScan.onInitSuccess();

                }

            }
        });
    }


    /**
     * 返回Reader对象
     * @return
     */
    public BarcodeReader getReader() {
        return reader;
    }

    /**
     * 开始扫描
     */
    public void startScan() {
        if (reader != null) {
            startContinueScan = isContinueScan;
            new Thread(runnableContinueScan).start();
        }
    }

    public void stopScan() {
        if (reader != null) {
            try {
                if (isContinueScan) {
                    startContinueScan = false;
                }
                reader.decode(false);
                //打开白灯
                if (isLight) {
                    reader.light(false);
                }
                //打开红灯
                if (isAim) {
                    reader.aim(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //开始解码

        }
    }

    /**
     * 设置启用或者禁用扫描硬案件
     *
     * @param b
     */
    public void setScanKeyEnable(Boolean b) {
        if (reader != null) {
            isScanKeyEnable = b;
//            if (!b)
//                reader.removeTriggerListener(null);
//            else
//                reader.addTriggerListener(triggerListener);
        }
    }

    /**
     * 扫描监听
     */
    private BarcodeReader.BarcodeListener barcodeListener = new BarcodeReader.BarcodeListener() {
        @Override
        public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {

            if (_iScan != null) {
                if (isBroadCastMode) {
                    Intent intent = new Intent();
                    intent.setAction(action);
                    String data = barcodeReadEvent.getBarcodeData();
                    if (addEnterString)
                        data += "\n";
                    intent.putExtra("data", data);
                    appContext.sendBroadcast(intent);
                } else
                    _iScan.onDecode(barcodeReadEvent.getBarcodeData());

            }


            if (isContinueScan && startContinueScan) {
                try {
                    Thread.sleep(continueDelay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Thread(runnableContinueScan).start();
            }

        }

        @Override
        public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

        }
    };

    /**
     * 声音开关
     *
     * @param b
     */
    public void setOpenScanSound(Boolean b) {
        try {

            reader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_GOOD_READ_ENABLED, b);
            reader.setProperty(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 中心解码
     *
     * @param b
     */
    public void setCENTER_DECODE(Boolean b) {
        try {

            reader.setProperty(BarcodeReader.PROPERTY_CENTER_DECODE, b);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 监听扫描键 true 为 按下
     */
    private BarcodeReader.TriggerListener triggerListener = new BarcodeReader.TriggerListener() {
        @Override
        public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {
            if (!isScanKeyEnable)
                return;
            Log.i("扫描键状态", String.valueOf(triggerStateChangeEvent.getState()));
            try {

                if (!isContinueScan) {

                    //打开白灯
                    if (isLight) {
                        reader.light(triggerStateChangeEvent.getState());
                    }
                    //打开红灯
                    if (isAim) {
                        reader.aim(triggerStateChangeEvent.getState());
                    }
                    //开始解码
                    reader.decode(triggerStateChangeEvent.getState());
                } else {
                    if (triggerStateChangeEvent.getState()) {
                        if (startContinueScan) {
                            reader.light(false);
                            reader.aim(false);
                            reader.decode(false);
                            startContinueScan = false;
                            return;
                        } else {
                            if (isContinueScan) {
                                startContinueScan = true;
                                new Thread(runnableContinueScan).start();
                            }

                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    /**
     * 连续扫描任务
     */
    private Runnable runnableContinueScan = new Runnable() {
        @Override
        public void run() {
            try {



                //打开白灯
                if (isLight) {
                    reader.light(true);
                }
                //打开红灯
                if (isAim) {
                    reader.aim(true);
                }
                //开始解码
                reader.decode(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 释放扫码头
     */
    public void onPauseHandle() {
        if (reader != null) {
            try {
                reader.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 恢复扫码头
     */
    public void onResumeHandle() {
        if (reader != null) {
            try {
                reader.claim();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 连续扫描
     * @param continueScan
     */
    public void setContinueScan(Boolean continueScan) {
        isContinueScan = continueScan;
        if (!continueScan) {
            if (reader != null) {
                try {

                    reader.light(false);
                    reader.aim(false);
                    reader.decode(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }
    }

    /**
     * 设置回调接口
     *
     * @param iScan
     */
    public void setIScan(IScan iScan) {
        this._iScan = iScan;
    }


}
