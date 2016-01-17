package com.mabeijianxi.circle_of_friends.adapter;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mabeijianxi.circle_of_friends.R;
import com.mabeijianxi.circle_of_friends.bean.EvaluatereplysBean;

import java.util.List;

/**
 * Created by jian on 2016/1/6.
 */
public class EvaluatereplysAdapter extends BaseAdapter {
    private final Context mContext;
    private List<EvaluatereplysBean> mEvaluatereplysList;

    public EvaluatereplysAdapter(Context mContext, List<EvaluatereplysBean> evaluatereplysList) {
        this.mContext = mContext;
        this.mEvaluatereplysList = evaluatereplysList;
    }

    @Override
    public int getCount() {
        if (mEvaluatereplysList != null) {
            return mEvaluatereplysList.size();
        }
        return 0;
    }

    @Override
    public EvaluatereplysBean getItem(int position) {
        return mEvaluatereplysList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
//           convertView = View.inflate(mContext, R.layout.item_evaluatereply, parent);
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_evaluatereply, parent, false);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_text);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        EvaluatereplysBean evaluatereplysBean = mEvaluatereplysList.get(position);

        SpannableString msp = new SpannableString(evaluatereplysBean.erReplyuser + ":" + evaluatereplysBean.erContent);
        msp.setSpan(new ForegroundColorSpan(0xff6b8747), 0, evaluatereplysBean.erReplyuser.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        viewHolder.tv.setText(msp);
        return convertView;
    }

    static class ViewHolder {
        public TextView tv;
    }

}
