package anim.activity;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sms on 2015/9/17.
 */
public class SMSObserver extends ContentObserver {

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    private Context mContext;
    private Handler mHandler;
    public SMSObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mHandler = handler;
    }
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        String code = "";
        if(uri.toString().equals("content://sms/raw")){
            return;
        }
        Uri inboxUri = Uri.parse("content://sms/inbox");
        Cursor cursor = mContext.getContentResolver().query(inboxUri,null,null,null,"date desc");
        if(cursor.moveToFirst()){
            String address = cursor.getString(cursor.getColumnIndex("address"));
            String body = cursor.getString(cursor.getColumnIndex("body"));
//            正则表达式
            Pattern pattern = Pattern.compile("\\d{6}");
            Matcher matcher = pattern.matcher(body);
            if(matcher.find()){
                code = matcher.group(0);
                mHandler.obtainMessage(ChangeTelActivity.MSG_RECEIVED_CODE, code).sendToTarget();
            }
        }
        cursor.close();
    }
}
