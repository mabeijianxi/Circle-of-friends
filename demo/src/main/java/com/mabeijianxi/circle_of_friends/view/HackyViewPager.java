package com.mabeijianxi.circle_of_friends.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public  class HackyViewPager extends ViewPager {
	private HackyViewPagerDispatchListener mHackyViewPagerDispatchListener;
	public HackyViewPager(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public HackyViewPager(Context context, AttributeSet attrs)
	{
	    super(context, attrs);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
	    try
	    {
	            return super.onInterceptTouchEvent(ev);
	    }
	    catch (IllegalArgumentException e)
	    {
	    }
	    catch (ArrayIndexOutOfBoundsException e)
	    {

	    }
	    return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()){
			case MotionEvent.ACTION_DOWN:
				if(mHackyViewPagerDispatchListener!=null){
					mHackyViewPagerDispatchListener.isDown();
				}
				break;
			case MotionEvent.ACTION_UP:
				if(mHackyViewPagerDispatchListener!=null){
					mHackyViewPagerDispatchListener.isUp();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				if(mHackyViewPagerDispatchListener!=null){
					mHackyViewPagerDispatchListener.isCancel();
				}
				break;
		}
		return super.dispatchTouchEvent(ev);
	}
	public interface HackyViewPagerDispatchListener{
		void isDown();
		void isUp();
		void isCancel();
	}
	public HackyViewPagerDispatchListener getmHackyViewPagerDispatchListener() {
		return mHackyViewPagerDispatchListener;
	}

	public void setmHackyViewPagerDispatchListener(HackyViewPagerDispatchListener mHackyViewPagerDispatchListener) {
		this.mHackyViewPagerDispatchListener = mHackyViewPagerDispatchListener;
	}

}
