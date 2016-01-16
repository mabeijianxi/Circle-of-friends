package anim.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.henanjianye.soon.communityo2o.R;

/**
 * Created by sms on 2015/9/16.
 */
public class RemoveBandDialog extends Dialog {
    public Context context;
    public RemoveBandDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_remove_bind_dialog);

    }
}
