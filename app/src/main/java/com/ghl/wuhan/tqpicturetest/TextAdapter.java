package com.ghl.wuhan.tqpicturetest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class TextAdapter extends BaseAdapter {

    private List<ListData> lists; //消息列表
    private Context mContext; //上下文
    private RelativeLayout layout; //布局
    //    private RelativeLayout layoutview;//布局

    public TextAdapter(List<ListData> lists, Context mContext) {
        this.lists = lists;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("TAG", "運行到了TextAdapter的getView: ");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        //左布局
        if (lists.get(position).getFlag() == ListData.RECEIVE) {
            layout = (RelativeLayout) inflater.inflate(R.layout.leftitem, null);
            Log.i("TAG", "运行到getView: leftitem");
        }
        //右布局
        if (lists.get(position).getFlag() == ListData.SEND) {
            layout = (RelativeLayout) inflater.inflate(R.layout.rightitem, null);
            Log.i("TAG", "运行到getView: rightitem");
        }
        TextView tv = (TextView) layout.findViewById(R.id.tv);
        ImageView iv_img = (ImageView) layout.findViewById(R.id.iv_img);
        //默认状态
        tv.setVisibility(View.VISIBLE);
        iv_img.setVisibility(View.GONE);
        //文字
        if (lists.get(position).getState() == ListData.USER_WORD) {
            tv.setText(lists.get(position).getContent());
        }
        //图片
        if (lists.get(position).getState() == ListData.USER_IMG) {
            tv.setVisibility(View.GONE);
            iv_img.setVisibility(View.VISIBLE);
            if(lists.get(position).getFlag() == ListData.RECEIVE){
                Glide.with(mContext).load(lists.get(position).getPitureUrl()).into(iv_img);
                Log.i(TAG, "getView: lists.get(position).getPitureUrl()"+lists.get(position).getPitureUrl());
            }else {
                iv_img.setImageBitmap(lists.get(position).getBitmap());//显示图片
            }
        }
        //聊天时间显示
        TextView tv_time = (TextView) layout.findViewById(R.id.tv_time);
        tv_time.setText(lists.get(position).getTime());
        return layout;
    }

}
