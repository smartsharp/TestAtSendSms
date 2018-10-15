package org.example.testsendsms;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "zzzMainActivity";

    private static final int SMS_ENCODING_7BIT = 1;
    private static final int SMS_ENCODING_8BIT = 2;
    private static final int SMS_ENCODING_UCS2 = 3;


    private static final int STATE_PREPARE_GMS_SMS = 1;
    private static final int STATE_READ_SMS_MODE = 2;
    private static final int STATE_WRITE_SMS_MODE = 3;
    private static final int STATE_SEND_SMS_START = 4;
    private static final int STATE_SEND_SMS_CONT = 5;
    private static final int STATE_CHECK_SMS_CARD = 6;


    private int phoneCount = 0;
    private String oldSmsMode;
    private Object phoneObj = null;
    private Method phoneObjInvoke = null;
    private DemoHandler mDemoHandler;
    private String sampleText = "工作愉快！";
    private String sampleNumber= "13800138000";
    private PduPack pdu;
    private int checkingSim = 0;
    private int availSim = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDemoHandler = new DemoHandler(this);
        phoneCount = getPhoneCount();
        Log.d(TAG, "phoneCount="+phoneCount);
        if(phoneCount > 0){
            checkingSim = 0;
            checkSimStatus(checkingSim);
        }
    }

    private int getPhoneCount() {
        try {
            Method m = TelephonyManager.class.getMethod("getDefault");
            TelephonyManager telMgr = (TelephonyManager) m.invoke(null);
            ClassLoader loader = this.getClassLoader();
            m = TelephonyManager.class.getMethod("getPhoneCount");
            return (int) m.invoke(telMgr);
        }catch (Exception e){
            return 0;
        }
    }

    private void checkSimStatus(int sim) {
        try {
            Log.d(TAG, "checkSimStatus "+sim);
            ClassLoader loader = this.getClassLoader();
            Method m;
            Class<?> factoryCls = loader.loadClass("com.android.internal.telephony.PhoneFactory");
            if(phoneCount > 1) {
                m = factoryCls.getMethod("getPhone", int.class);
                m.setAccessible(true);
                phoneObj = m.invoke(null, sim); //PhoneConstants.SIM_ID_1
            }else {
                m = factoryCls.getMethod("getDefaultPhone");
                m.setAccessible(true);
                phoneObj = m.invoke(null);
            }
            Class<?> phoneCls = phoneObj.getClass(); //loader.loadClass("com.android.internal.telephony.Phone");
            phoneObjInvoke = phoneCls.getMethod("invokeOemRilRequestStrings", String[].class, Message.class);
            phoneObjInvoke.setAccessible(true);
            String cmdStr[] = { "AT+ESIMS?", "+ESIMS: " };
            phoneObjInvoke.invoke(phoneObj, cmdStr, mDemoHandler.obtainMessage(STATE_CHECK_SMS_CARD));
        }catch (Exception e){
            Log.e(TAG, "checkSimStatus exception "+e+","+Log.getStackTraceString(e));
        }

    }

    public void testSendSms(View view) {
        if(availSim < 0){
            Toast.makeText(this, "testSendSms failed. no available sim card", Toast.LENGTH_SHORT).show();
            return;
        }
        pdu = new PduPack();
        pdu.setAddr(sampleNumber.trim());
        pdu.setMsgContent(sampleText.trim());
        int length = pdu.getmsgLen();
        if(length == 0 || !pdu.getCodedResult().endsWith("5DE54F5C61095FEBFF01")){
            Log.e(TAG, "testSendSms encode failed: "+pdu.getCodedResult());
            return;
        }else{
            Log.d(TAG, "testSendSms "+length+", "+pdu.getCodedResult());
        }

        prepareGsmSms();
        //readSmsMode();
        //writeSmsMode(0);
    }
    private void prepareGsmSms() {
        try {
            String cmdStr[] = { "AT+CSCS=\"GSM\"", "" };
            Log.d(TAG, "prepareGsmSms: "+cmdStr[0]);
            phoneObjInvoke.invoke(phoneObj, cmdStr, mDemoHandler.obtainMessage(STATE_PREPARE_GMS_SMS));

        }catch (Exception e){
            Log.e(TAG, "prepareGsmSms exception "+e+","+Log.getStackTraceString(e));
        }
    }
    private void readSmsMode() {
        try {
            String cmdStr[] = { "AT+CMGF?", "+CMGF: " };
            Log.d(TAG, "readSmsMode: "+cmdStr[0]);
            phoneObjInvoke.invoke(phoneObj, cmdStr, mDemoHandler.obtainMessage(STATE_READ_SMS_MODE));

        }catch (Exception e){
            Log.e(TAG, "readSmsMode exception "+e+","+Log.getStackTraceString(e));
        }
    }
    private void writeSmsMode(int mode) {
        try {
            String cmdStr[] = { "AT+CMGF="+mode, "" };
            Log.d(TAG, "writeSmsMode: "+cmdStr[0]);
            phoneObjInvoke.invoke(phoneObj, cmdStr, mDemoHandler.obtainMessage(STATE_WRITE_SMS_MODE));

        }catch (Exception e){
            Log.e(TAG, "writeSmsMode exception "+e+","+Log.getStackTraceString(e));
        }
    }

    private void sendSmsTextStart() {
        try {
            String cmdStr[] = { "AT+CMGS="+String.valueOf(pdu.getmsgLen()+15), "+CMGS: " };
            Log.d(TAG, "sendSmsTextStart: "+cmdStr[0]);
            phoneObjInvoke.invoke(phoneObj, cmdStr, mDemoHandler.obtainMessage(STATE_SEND_SMS_START));
        }catch (Exception e){
            Log.e(TAG, "sendSmsTextStart exception "+e+","+Log.getStackTraceString(e));
        }
    }
    private void sendSmsTextCont() {
        try {
            String cmdStr[] = {pdu.getCodedResult()+"\u001A", ""};
            Log.d(TAG, "sendSmsTextCont: "+cmdStr[0]);
            phoneObjInvoke.invoke(phoneObj, cmdStr, mDemoHandler.obtainMessage(STATE_SEND_SMS_CONT));
        }catch (Exception e){
            Log.e(TAG, "sendSmsTextCont exception "+e+","+Log.getStackTraceString(e));
        }
    }
    class DemoHandler extends Handler {
        private Context mContext;
        public DemoHandler(Context context){
            this.mContext = context;
        }


        /*
        public class AsyncResult
        {
        // Expect either exception or result to be null
        public Object userObj;
        public Throwable exception;
        public Object result;
        ...
        }
         */
        public void handleMessage(Message message){
            Throwable exception = null;
            String result = null;
            //Log.d(TAG, "DemoHandler handleMessage "+message.what);
            try {
                ClassLoader loader = mContext.getClassLoader();
                Class<?> cls = message.obj.getClass();//loader.loadClass("android.os.AsyncResult");
                exception = (Throwable)cls.getField("exception").get(message.obj);
                Object resultObj = cls.getField("result").get(message.obj);
                //Object userObj = cls.getField("userObj").get(message.obj);
                if(resultObj != null && resultObj instanceof  String[]){
                    String[] resultArray = (String[])resultObj;
                    if(resultArray.length>0) {
                        result = "";
                        for (int i = 0; i < resultArray.length; i++) {
                            result += resultArray[i] + " ";
                        }
                        result = result.trim();
                    }
                }

                switch(message.what){
                    case STATE_PREPARE_GMS_SMS:
                        if(exception == null){
                            Log.d(TAG, "STATE_PREPARE_GMS_SMS successfully. " + result);
                            readSmsMode();
                        }else{
                            Toast.makeText(mContext, "STATE_PREPARE_GMS_SMS failed. " + result, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "STATE_PREPARE_GMS_SMS failed. " + exception.toString());
                        }
                        break;

                    case STATE_READ_SMS_MODE:
                        if(exception == null){
                            Log.d(TAG, "STATE_READ_SMS_MODE successfully. " + result);
                            oldSmsMode = result.substring("+CMGF: ".length());
                            if(oldSmsMode.equals("1")){
                                writeSmsMode(0);
                            }else{
                                sendSmsTextStart();
                            }

                        }else{
                            Toast.makeText(mContext, "STATE_READ_SMS_MODE failed. " + result, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "STATE_READ_SMS_MODE failed. " + exception.toString());
                        }
                        break;
                    case STATE_WRITE_SMS_MODE:
                        if(exception == null){
                            Log.d(TAG, "STATE_WRITE_SMS_MODE successfully. " + result);
                            sendSmsTextStart();
                        }else{
                            Toast.makeText(mContext, "STATE_WRITE_SMS_MODE failed. " + result, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "STATE_WRITE_SMS_MODE failed. " + exception.toString());
                        }
                        break;
                    case STATE_SEND_SMS_START:
                        if(exception == null){
                            Log.d(TAG, "STATE_SEND_SMS_START successfully. " + result);
                            sendSmsTextCont();
                        }else{
                            Toast.makeText(mContext, "STATE_SEND_SMS_START failed. " + result, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "STATE_SEND_SMS_START failed. " + exception.toString());
                        }
                        break;
                    case STATE_SEND_SMS_CONT:
                        if(exception == null){
                            Log.d(TAG, "STATE_SEND_SMS_CONT successfully. " + result);
                            sendSmsTextCont();
                        }else{
                            Toast.makeText(mContext, "STATE_SEND_SMS_CONT failed. " + result, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "STATE_SEND_SMS_CONT failed. " + exception.toString());
                        }
                        break;
                    case STATE_CHECK_SMS_CARD:
                        if(exception == null){
                            Log.d(TAG, "STATE_CHECK_SMS_CARD "+checkingSim+" successfully. " + result);
                            if("+ESIMS: 1".equals(result)){
                                availSim = checkingSim;
                                Log.d(TAG, "found availSim="+availSim);
                            }else if(checkingSim < phoneCount-1) {
                                checkingSim++;
                                checkSimStatus(checkingSim);
                            }
                        }else{
                            Toast.makeText(mContext, "STATE_CHECK_SMS_CARD "+checkingSim+" failed. " + result, Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "STATE_CHECK_SMS_CARD "+checkingSim+" failed. " + exception.toString());
                        }
                        break;

                }
            }catch (Exception e){
                Log.e(TAG, "DemoHandler exception "+e+","+Log.getStackTraceString(e));
            }


        }
    }


}
