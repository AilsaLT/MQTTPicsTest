package com.ghl.wuhan.tqpicturetest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {
        private EditText et_username, et_password;
        private Button btn_login;
        private String username, password;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login_activity);

            //初始化视图界面
            initView();
            btn_login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //获取EditText上的值
                    username = et_username.getText().toString().trim();
                    password = et_password.getText().toString().trim();

                    Log.i("TAG", "onCreate: " + username + "----" + password);

                    if (username.equals("1") && password.equals("1")) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else if (!username.equals("1") && !password.equals("1")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "用户名或密码不正确！", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                }
            });


        }


        //初始化视图界面
        public void initView() {
            et_username = (EditText) findViewById(R.id.et_username);
            et_password = (EditText) findViewById(R.id.et_password);
            btn_login = (Button) findViewById(R.id.btn_login);

        }
    }