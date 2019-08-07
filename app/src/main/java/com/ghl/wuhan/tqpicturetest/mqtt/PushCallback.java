package com.ghl.wuhan.tqpicturetest.mqtt;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ghl.wuhan.tqpicturetest.ListData;
import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


public class PushCallback implements MqttCallback {
    private String TAG = "Test";
    Context ctx;

    public PushCallback(Context ctx){
        this.ctx = ctx;
    }

    public void connectionLost(Throwable cause) {
        // 连接丢失后，一般在这里面进行重连
        Log.i( TAG, "connectionLost: 连接断开，可以做重连" );
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i( TAG, "deliveryComplete: "+token.isComplete() );
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面
        Log.i( TAG, "messageArrived: 接收消息主题:" +topic);
        Log.i( TAG, "messageArrived: 接收消息Qos:" +message.getQos());
        Log.i( TAG, "messageArrived: 接收消息内容:"+new String( message.getPayload() ) );

        Log.i(TAG,"topic is :"+topic);
        Log.i(TAG,"pushCallBack messages is :"+ message.toString());
        Gson gson = new Gson();
        ListData listData = gson.fromJson( message.toString(),ListData.class );
        Intent intent = new Intent(Constants.MY_MQTT_BROADCAST_NAME);
        if(listData.getToUser() != null && listData.getToUser().equals( "me" )){
            listData.setFlag( ListData.RECEIVE );
            String re_message = gson.toJson( listData,ListData.class );
            Log.i( TAG, "messageArrived: re_message is "+re_message );
            intent.putExtra("message",re_message);
        }
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(ctx);
        //发送本地广播
        localBroadcastManager.sendBroadcast(intent);

    }
}
