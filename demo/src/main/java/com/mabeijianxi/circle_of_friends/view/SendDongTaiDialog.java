package com.mabeijianxi.circle_of_friends.view;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.mabeijianxi.circle_of_friends.R;

/**
 * Created by jian on 2016/3/24.
 */
public class SendDongTaiDialog extends Dialog implements
        View.OnClickListener {

    private DialogClickListener mDialogClickListener;
    private Activity mActivity;
    private Button btn_take_photo;
    private Button btn_pick_photo;
    private Button btn_cancel;

    private SendDongTaiDialog(Context context, boolean flag,
                              OnCancelListener listener) {
        super(context, flag, listener);
    }

    @SuppressLint("InflateParams")
    private SendDongTaiDialog(Context context, int defStyle) {
        super(context, defStyle);
        View contentView = getLayoutInflater().inflate(
                R.layout.send_select_photo_dialog, null);
        initView(contentView);
        initListener();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mDialogClickListener != null) {
                    mDialogClickListener.onClick(R.id.btn_cancel);
                }
                SendDongTaiDialog.this.dismiss();
                return true;
            }
        });
        super.setContentView(contentView);

    }

    public void setButtonTextShow(String button1, String button2, String button3) {
        btn_take_photo.setText(button1);
        btn_pick_photo.setText(button2);
        btn_cancel.setText(button3);
    }

    private void initView(View contentView) {
        btn_take_photo = (Button) contentView.findViewById(R.id.btn_take_photo);
        btn_pick_photo = (Button) contentView.findViewById(R.id.btn_pick_photo);
        btn_cancel = (Button) contentView.findViewById(R.id.btn_cancel);
    }

    private void initListener() {
        btn_take_photo.setOnClickListener(this);
        btn_pick_photo.setOnClickListener(this);
        btn_cancel.setOnClickListener(this);
    }

    public Button getBtn1() {
        return btn_take_photo;
    }

    public Button getBtn2() {
        return btn_pick_photo;
    }

    public SendDongTaiDialog(Activity context) {
        this(context, R.style.quick_option_dialog);
        this.mActivity = context;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setGravity(Gravity.BOTTOM);
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        p.width = d.getWidth();
        getWindow().setAttributes(p);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_take_photo) {
            if (mDialogClickListener != null) {
                mDialogClickListener.onClick(R.id.btn_take_photo);
            }
        } else if (id == R.id.btn_pick_photo) {
            if (mDialogClickListener != null) {
                mDialogClickListener.onClick(R.id.btn_pick_photo);
            }
        } else if (id == R.id.btn_cancel) {
            if (mDialogClickListener != null) {
                mDialogClickListener.onClick(R.id.btn_cancel);
            }
        }
        dismiss();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDialogClickListener != null) {
            mDialogClickListener.onClick(R.id.btn_cancel);
        }
    }

    public interface DialogClickListener {
        void onClick(int id);
    }

    public DialogClickListener getmDialogClickListener() {
        return mDialogClickListener;
    }

    public void setmDialogClickListener(DialogClickListener mDialogClickListener) {
        this.mDialogClickListener = mDialogClickListener;
    }
}
