package com.ghl.wuhan.tqpicturetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.ghl.wuhan.tqpicturetest.app.AppStr;
import com.ghl.wuhan.tqpicturetest.mqtt.Constants;
import com.ghl.wuhan.tqpicturetest.mqtt.SubscriptClient;
import com.ghl.wuhan.tqpicturetest.utils.COSUtils;
import com.ghl.wuhan.tqpicturetest.utils.ImageUtils;
import com.google.gson.Gson;
import com.longsh.optionframelibrary.OptionBottomDialog;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MqttClient client;//创建可用于与MQTT服务器通信的MqttClient。
    private String host = "tcp://47.105.183.54:61613"; //主机的ip(tcp连接)---要连接的服务器的地址，指定为URI
    private String userName = "admin";    // MQTT的server的用户名
    private String passWord = "password"; // MQTT的server的密码
    private MqttTopic topic;
    private MqttMessage message;

    private String myTopic = "LT/me";     //   发布消息主题
    private String myClientID = "187"; //  发布消息的ID , 可以是任意唯一字符串 （比如：邮箱，手机号，UUID等）


    private String fromWho = "LT";
    private String toUser = "me";

    private List<ListData> lists; //消息列表
    private ListView lv;    //列表控件
    private EditText et_sendText; //消息输入框
    private Button btn_send;  //发送button
    private String content_str; //
    private TextAdapter adapter;
    private double currentTime,oldTime = 0;//

    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private String TAG = "Test";

    //拍照
    private ImageView iv_picture;
    private Uri imageUri;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int CROP_IMAGE = 3;
    private File filePath;


    private int state;
    private ListData listData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView(); //初始化界面
        state = ListData.USER_WORD;//默认是文字类型0
        connectMQTTServer(); // 连接MQTT服务

        //订阅
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG,"==============The client begin to start ....");
                SubscriptClient client = new SubscriptClient(MainActivity.this);
                client.start();
                Log.i(TAG,"==============The client is running....");

            }
        }).start();

        intentFilter = new IntentFilter();
        intentFilter.addAction( Constants.MY_MQTT_BROADCAST_NAME );
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        //注册本地接收器
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }

    /*
    *处理文字发送点击事件的处理
 */
    @Override
    public void onClick(View v) {
        state = ListData.USER_WORD;
        switch (v.getId()){
            case R.id.btn_send:
            {
                Log.i(TAG, "運行到了onClick: ");

                if (state == ListData.USER_WORD){
                    Log.i(TAG, "onClick: ListData.USER_WORD--->"+ListData.USER_WORD);//0
                    content_str = et_sendText.getText().toString();
                    et_sendText.setText("");
                    Log.i(TAG, "----------content_str="+content_str);
                    listData = new ListData(fromWho,toUser,content_str, ListData.SEND, getTime(),state);
                }

                lists.add(listData);//将数据内容加入lists中
                adapter.notifyDataSetChanged();//
                lv.setAdapter(adapter);
                Gson gson = new Gson();
                final String jsonStr = gson.toJson(listData, ListData.class);
                Log.i( TAG, "myRepublish: jsonStr is "+jsonStr );
                if(lists.size() > 30){
                    for (int i = 0; i < lists.size(); i++) {
                        lists.remove(i);
                    }
                }
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        myRepublish(jsonStr);
                    }
                }).start();
            }
            break;
            case R.id.iv_picture:
            {
                List<String> stringList = new ArrayList<String>();
                stringList.add("拍照");
                stringList.add("从相册选择");
                final OptionBottomDialog optionBottomDialog = new OptionBottomDialog(MainActivity.this, stringList);
                optionBottomDialog.setItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        switch (position) {
                            case 0://选择调用手机相机
                                Intent photo = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                imageUri = ImageUtils.getImageUri(MainActivity.this);
                                photo.putExtra(MediaStore.EXTRA_OUTPUT, imageUri); //putExtra()指定图片的输出地址，填入之前获得的Uri对象
                                startActivityForResult(photo, TAKE_PHOTO);
                                optionBottomDialog.dismiss();  //底部弹框消失
                                break;
                            case 1: //选择相册
                                Intent picsIn = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(picsIn, CHOOSE_PHOTO);
                                optionBottomDialog.dismiss();//底部弹框消失
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
            break;
            default:
                break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    File saveFile = ImageUtils.getTempFile(); //设置照片存储文件及剪切图片
                    filePath = ImageUtils.getTempFile();
                    startImageCrop(saveFile, imageUri);
                }
                break;
            case CHOOSE_PHOTO://相册
                if (resultCode == RESULT_OK) {
                    try {
                        imageUri = data.getData(); //获取系统返回的照片的Uri
                        File saveFile = ImageUtils.setTempFile(MainActivity.this);//设置照片存储文件及剪切图片
                        filePath = ImageUtils.getTempFile();
                        startImageCrop(saveFile, imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CROP_IMAGE://裁剪
                if (resultCode == RESULT_OK) {
                    state = ListData.USER_IMG;//0
                    final Bitmap bitmap = BitmapFactory.decodeFile(filePath.toString());//调用BitmapFactory的decodeStream()方法把图片解析成Bitmap对象
                    listData = new ListData(fromWho,toUser,ListData.SEND,getTime(),state,bitmap);//未确保图片是否上传成功不传picUrl
                    lists.add(listData);//将数据内容加入lists中
                    adapter.notifyDataSetChanged();
                    lv.setAdapter(adapter);
                    AppStr appStr = (AppStr)getApplication();//全局变量
                    appStr.setIsCompleted(false);
                    COSUtils.initCOS(MainActivity.this);//上传对象
                    publishPic(appStr,listData);//检查图片是否上传成功
                }
                break;
            default:
                break;
        }
    }
    //检查图片是否上传成功（解决网络延时问题，直到图片上传成功再进行下一步）
    private void publishPic(final AppStr appStr, final ListData listData) {
        if(appStr.isCompleted() == true){
            String httpMessage = COSUtils.getHttpMessage();
            String picPath  = COSUtils.getPictureUrl();
            if(httpMessage.equals("OK") && picPath != null){
                Gson gson = new Gson();
                listData.setPitureUrl(picPath);
                final String jsonStr = gson.toJson(listData, ListData.class);
                Log.i( TAG, "myRepublish: jsonStr is "+jsonStr );
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        myRepublish(jsonStr);
                    }
                }).start();
            }else{
                Toast.makeText(MainActivity.this, "上传图片失败", Toast.LENGTH_SHORT).show();
            }
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    publishPic(appStr,listData);
                }
            }).start();
        }
    }

    /*
      *广播接收器
      */
    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"MainActivity message is:"+message);
                    ListData listData;
                    Gson gson = new Gson();
                    listData = gson.fromJson( message,ListData.class );
                    lists.add(listData);
                    //notifyDataSetChanged方法通过一个外部的方法控制如果适配器的内容改变时
                    //需要强制调用getView来刷新每个Item的内容
                    adapter.notifyDataSetChanged();
                    lv.setAdapter(adapter);
                }
            });
        }
    }

    /*
     *连接MQTT服务
     */
    private void connectMQTTServer(){
        try {
            Log.i(TAG, "=================begin to connect MQTT server====================");
            client = new MqttClient(host, myClientID, new MemoryPersistence()); // myClientID 是客户端ID，可以是任何唯一的标识

            // 连接 MQTT服务器
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }).start();

            Log.i(TAG, "=================connect MQTT server end=========================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void connect() {

        MqttConnectOptions options = new MqttConnectOptions();//一组覆盖默认值的连接参数
        options.setCleanSession(false);
        options.setUserName(userName);
        options.setPassword(passWord.toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(20);
        try {
            client.setCallback(new MqttCallback() {
                /**
                 * 消息连接丢失
                 */
                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(TAG, "connectionLost-----------");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(TAG, "deliveryComplete---------" + token.isComplete());
                }

                /**
                 * 接收到消息的回调的方法
                 */
                @Override
                public void messageArrived(String topic, MqttMessage arg1)
                        throws Exception {
                    Log.i(TAG, "messageArrived----------");

                }
            });

            topic = client.getTopic(myTopic);//获取可用于发布消息的主题对象。
            client.connect(options);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     *初始化界面
     */
    private void initView(){
        lists = new ArrayList<ListData>();
        lv = (ListView) findViewById(R.id.lv);
        et_sendText = (EditText) findViewById(R.id.et_sendText);
        btn_send = (Button) findViewById(R.id.btn_send);
        iv_picture = (ImageView) findViewById(R.id.iv_picture);
        btn_send.setOnClickListener(this);//发送文字监听
        iv_picture.setOnClickListener(this);//拍照监听
        adapter = new TextAdapter(lists, this);
        lv.setAdapter(adapter);

    }

    /*
     *设置时间
     */
    private String getTime(){
        currentTime = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        if(currentTime - oldTime >= 5*60*1000){
            oldTime = currentTime;
            return str;
        }else{
            return "";
        }
    }

    /*
     *发布消息
     */
    private void myRepublish(String jsonStr) {
        try {
            message = new MqttMessage();
            message.setQos(0); // 可以有三种值（0,1,2），分别代表消息发送情况：至少发送一次，至少
            message.setRetained(false);
            Log.i(TAG, message.isRetained() + "------retained状态");
            //设置负载，即消息内容
            message.setPayload(jsonStr.getBytes());
            Log.i(TAG, "message is :" + new String(message.getPayload()));
            MqttDeliveryToken token = topic.publish(message);
            token.waitForCompletion();
            Log.i(TAG, token.isComplete() + "===============================================");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i( TAG, "myRepublish: "+message.toString() );
        }
    }


    //图片裁剪
    private void startImageCrop(File saveToFile, Uri uri) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        Log.i(TAG, "startImageCrop: " + "执行到压缩图片了" + "uri is " + uri);
        intent.setDataAndType(uri, "image/*");//设置Uri及类型
        //uri权限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.putExtra("crop", "true");//
        intent.putExtra("aspectX", 1);//X方向上的比例
        intent.putExtra("aspectY", 1);//Y方向上的比例
        intent.putExtra("outputX", 150);//裁剪区的X方向宽
        intent.putExtra("outputY", 150);//裁剪区的Y方向宽
        intent.putExtra("scale", true);//是否保留比例
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);//是否将数据保留在Bitmap中返回dataParcelable相应的Bitmap数据，防止造成OOM
        //判断文件是否存在
        //File saveToFile = ImageUtils.getTempFile();
        if (!saveToFile.getParentFile().exists()) {
            saveToFile.getParentFile().mkdirs();
        }
        //将剪切后的图片存储到此文件
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(saveToFile));
        Log.i(TAG, "startImageCrop: " + "即将跳到剪切图片");
        startActivityForResult(intent, CROP_IMAGE);
    }

}
