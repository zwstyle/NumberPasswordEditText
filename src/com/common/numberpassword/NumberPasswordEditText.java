package com.common.numberpassword;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.common.numberpassword.R;

public class NumberPasswordEditText extends LinearLayout {

	/**
	 * 输入框数量
	 */
	private int mCount = 4;
	private List<EditText> mEditTexts;
	private List<ImageView> mImageViews;
	private Context mContext;
	/**
	 * 输入框之间的padding
	 */
	private int mPadding = 20;
	/**
	 * 输入框的宽度
	 */
	private int mEditTextWidth = -1;
	private OnCompletePasswordInput mCompletePasswordInput;
	
	InputMethodManager imm = null;  
	
	private int mScreenWidth = -1;
	
	public NumberPasswordEditText(Context context) {
		super(context);
		init(context, null);
	}

	@SuppressLint("NewApi")
	public NumberPasswordEditText(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public NumberPasswordEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (attrs == null) {
			return;
		}
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.NumberPassword);
		if (a.hasValue(R.styleable.NumberPassword_numberCount)) {
			mCount = a.getInteger(R.styleable.NumberPassword_numberCount, 4);
		}
		a.recycle();

		mContext =context;
		
		mEditTexts = new ArrayList<EditText>();
		mImageViews = new ArrayList<ImageView>();
		imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		setGravity(Gravity.CENTER_HORIZONTAL);
		setPadding(10, 0, 10, 0);

		setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				imm.showSoftInput(getCurrentInputEditText(),0);  
			}
		});
		
	}

	
	
	

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		removeAllViews();
		for (int i = 0; i < mCount; i++) {
			addView(createView(getEditTextWidth()));
		}
		
		mEditTexts.get(0).setEnabled(true);
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				mEditTexts.get(0).requestFocus();
				imm.showSoftInput(mEditTexts.get(0),0);  
			}
		}, 100);
	}

	public void hintInputMethod(){
		imm.hideSoftInputFromWindow(getCurrentInputEditText().getWindowToken(), 0);
	}
	/**
	 * 获取输入框的宽度
	 * @return
	 */
	private int getEditTextWidth() {
		if (mEditTextWidth == -1) {
			int width = (getScreenWidth(mContext) - (mCount)
					* dip2px(mPadding))/mCount;
			int minWidth = dip2px(30);
			int maxWidth = dip2px(50);
			if (width < minWidth) {
				mPadding = 10;
				width = minWidth;
			} else if (width > maxWidth) {
				width = maxWidth;
			}
			mEditTextWidth = width;
		}
		return mEditTextWidth;
	}

	private int getScreenWidth(Context context) {
		if(mScreenWidth == -1){
			WindowManager manager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = manager.getDefaultDisplay();
			mScreenWidth = display.getWidth();
		}
		return mScreenWidth;
	}

	

	/**
	 * 创建输入框
	 * @param width
	 * @return
	 */
	private View createView(int width) {
		View view = inflate(mContext,R.layout.number_password_item, null);
		final EditText editText = (EditText) view
				.findViewById(R.id.et_number_password);
		editText.setLayoutParams(new FrameLayout.LayoutParams(width, width));
		ImageView imageView = (ImageView) view
				.findViewById(R.id.img_number_password);

		mEditTexts.add(editText);
		mImageViews.add(imageView);
		final MPasswordTransformationMethod transformationMethod =
				MPasswordTransformationMethod.getInstance();
		editText.setTransformationMethod(transformationMethod);
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				final int index = mEditTexts.indexOf(editText);
				if(TextUtils.isEmpty(s)){
					mImageViews.get(index).setVisibility(View.GONE);
				}else{
					postDelayed(new  Runnable() {
						public void run() {
							mImageViews.get(index).setVisibility(View.VISIBLE);
						}
					}, 300);
					
					if(index == mCount-1){
						
						if(mCompletePasswordInput != null){
							mCompletePasswordInput.complete(getPassword());
						}
						
					}else{
						mEditTexts.get(index+1).requestFocus();
						mEditTexts.get(index+1).setEnabled(true);
						mEditTexts.get(index).clearFocus();
						mEditTexts.get(index).setEnabled(false);
					}
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
		
		editText.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				
				if(keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN){
					EditText editText =getCurrentInputEditText();
					int index = mEditTexts.indexOf(editText);
					if(mCompletePasswordInput != null){
						mCompletePasswordInput.onReset();
					}
					if(index > 0){
						mEditTexts.get(index-1).setText("");
						mEditTexts.get(index-1).requestFocus();
						mEditTexts.get(index-1).setEnabled(true);
						mEditTexts.get(index).setEnabled(false);
						return true;
					}
				}
				return false;
			}
		});
		
		view.setPadding(mPadding/2, 0, mPadding/2, 0);
		return view;
	}

	
	
	
	public void setmCompletePasswordInput(
			OnCompletePasswordInput mCompletePasswordInput) {
		this.mCompletePasswordInput = mCompletePasswordInput;
	}

	public String getPassword(){
		String text = "";
		for(EditText et : mEditTexts){
			text += et.getText().toString().trim();
			if(TextUtils.isEmpty(text)){
				return null;
			}
		}
		return text;
	}
	
	private EditText getCurrentInputEditText(){
		for(EditText et:mEditTexts){
			if(TextUtils.isEmpty(et.getText().toString())){
				return et;
			}
		}
		return null;
	}
	
	public int dip2px(float dpValue) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
	
	public void setScreenWidth(int width){
		this.mScreenWidth = width;
	}
	
	/**
	 * 完成输入时的回调
	 * @author Handler
	 *
	 * 2015-5-28
	 */
	public interface  OnCompletePasswordInput{
		public void complete(String value);
		public void onReset();
	}
	
}
