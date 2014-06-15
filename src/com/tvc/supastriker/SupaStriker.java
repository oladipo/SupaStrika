package com.tvc.supastriker;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.content.res.Configuration;
import android.graphics.Color;

import com.qualcomm.QCAR.QCAR;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.*;

public class SupaStriker extends Activity {

	// Focus mode constants:
    private static final int FOCUS_MODE_NORMAL = 0;
    private static final int FOCUS_MODE_CONTINUOUS_AUTO = 1;

    // Application status constants:
    private static final int APPSTATUS_UNINITED         = -1;
    private static final int APPSTATUS_INIT_APP         = 0;
    private static final int APPSTATUS_INIT_QCAR        = 1;
    private static final int APPSTATUS_INIT_TRACKER     = 2;
    private static final int APPSTATUS_INIT_APP_AR      = 3;
    private static final int APPSTATUS_LOAD_TRACKER     = 4;
    private static final int APPSTATUS_INITED           = 5;
    private static final int APPSTATUS_CAMERA_STOPPED   = 6;
    private static final int APPSTATUS_CAMERA_RUNNING   = 7;
    
    // Name of the native dynamic libraries to load:
    private static final String NATIVE_LIB_SUPASTRIKER = "SupaStriker";
    private static final String NATIVE_LIB_QCAR = "QCAR";
    
 // Constants for Hiding/Showing Loading dialog
    static final int HIDE_LOADING_DIALOG = 0;
    static final int SHOW_LOADING_DIALOG = 1;

    private View mLoadingDialogContainer;

    // Our OpenGL view:
    private QCARGLView mGlView;

    // Our renderer:
    private SupaStrikerRenderer mRenderer;
    

    // Display size of the device:
    private int mScreenWidth = 0;
    private int mScreenHeight = 0;

    // Constant representing invalid screen orientation to trigger a query:
    private static final int INVALID_SCREEN_ROTATION = -1;

    // Last detected screen rotation:
    private int mLastScreenRotation = INVALID_SCREEN_ROTATION;

    // The current application status:
    private int mAppStatus = APPSTATUS_UNINITED;
    
    // The async tasks to initialize the QCAR SDK:
    private InitQCARTask mInitQCARTask;
    private LoadTrackerTask mLoadTrackerTask;
    
    private Object mShutdownLock = new Object();
    
    private int mQCARFlags = 0;
    
    private Vector<Texture> mTextures;
    
    private GestureDetector mGestureDetector;
    
    private boolean mFlash = false;
    private boolean mContAutoFocus = false;
    
    boolean mSupaStrikerDataSetActive = false;
    
    private RelativeLayout mUILayout;
    
	public static native int getOpenGlEsVersionNative();
	public native int loadTrackerData();
	public native void destroyTrackerData();
	public native int initTracker();
	public native void deinitTracker();
	public native void onQCARInitializedNative();
	
	
	private native void startCamera();
	private native void stopCamera();
	private native boolean autofocus();
	private native boolean activateFlash(boolean flash);
	private native void setActivityPotraitMode(boolean isPotrait);
	private native void setProjectionMatrix();
	private native boolean setFocusMode(int mode);
	private native void initApplicationNative(int width, int height);
    private native void deinitApplicationNative();
    
    /** Returns the number of registered textures. */
    public int getTextureCount()
    {
        return mTextures.size();
    }


    /** Returns the texture object at the specified index. */
    public Texture getTexture(int i)
    {
        return mTextures.elementAt(i);
    }
    
    static{
    	loadLibrary(NATIVE_LIB_QCAR);
    	loadLibrary(NATIVE_LIB_SUPASTRIKER);
    }
    
    static class LoadingDialogHandler extends Handler{
    	private final WeakReference<SupaStriker> mSupaStriker;
    	
    	LoadingDialogHandler(SupaStriker supaStriker){
    		mSupaStriker = new WeakReference<SupaStriker>(supaStriker);
    	}
    	
    	public void handleMessage(Message msg){
    		SupaStriker supaStriker = mSupaStriker.get();
    		if(supaStriker == null){
    			return;
    		}
    		if(msg.what == SHOW_LOADING_DIALOG){
    			supaStriker.mLoadingDialogContainer.setVisibility(View.VISIBLE);
    		}
    		else if(msg.what == HIDE_LOADING_DIALOG){
    			supaStriker.mLoadingDialogContainer.setVisibility(View.GONE);
    		}
    	}
    }
    
    private Handler loadingDialogHandler = new LoadingDialogHandler(this);
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DebugLog.LOGD("SupaStriker::onCreate");
		
		super.onCreate(savedInstanceState);
		mTextures = new Vector<Texture>();
		loadTextures();
		mQCARFlags = getInitializationFlags();
		
		DebugLog.LOGI("open GL version : " + mQCARFlags);
		
		mGestureDetector = new GestureDetector(this,new GestureListener());
		
		updateApplicationStatus(APPSTATUS_INIT_APP);
		//setContentView(R.layout.activity_supa_striker);
		
		//setContentView(mGlView);
	}
	
	public void onConfigurationChanged(Configuration config){
		DebugLog.LOGD("SupaStriker::onConfigurationChanged");
		super.onConfigurationChanged(config);
		
		updateActivityOrientation();
		
		storeScreenDimensions();
		
		mLastScreenRotation = INVALID_SCREEN_ROTATION;
	}
	protected void onPause(){
		DebugLog.LOGD("SupaStriker::onPause");
		super.onPause();
		
		if(mGlView != null){
			mGlView.setVisibility(View.INVISIBLE);
			mGlView.onPause();
		}
		
		if(mAppStatus == APPSTATUS_CAMERA_RUNNING){
			updateApplicationStatus(APPSTATUS_CAMERA_STOPPED);
		}
		
		if(mFlash){
			mFlash = false;
			activateFlash(mFlash);
		}
		
		QCAR.onPause();
	}
	//should be able to load more than one texture file...
	private void loadTextures(){
		//mTextures.add(Texture.loadTextureFromApk("TextureSupaStrikas.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("eyes.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("face.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("file_1.png",getAssets()));
		mTextures.add(Texture.loadTextureFromApk("kit.PNG",getAssets()));
	}
	
	protected void onResume(){
		DebugLog.LOGD("SupaStrikas::onResume");
		super.onResume();
		
		QCAR.onResume();
		
		if(mAppStatus == APPSTATUS_CAMERA_STOPPED){
			updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
		}
		if(mGlView != null){
			mGlView.setVisibility(View.VISIBLE);
			mGlView.onResume();
		}
	}
	private int getInitializationFlags(){
		int flags = 0;
		
		if(getOpenGlEsVersionNative() == 1){
			flags = QCAR.GL_11;
		}
		else{
			flags = QCAR.GL_20;
		}
		
		return flags;
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		public boolean onDown(MotionEvent e){
			return true;
		}
		
		public boolean onSingleTapUp(MotionEvent e){
			
			autofocus();
			
			mContAutoFocus = false;
			return true;
		}
		
		public boolean onDoubleTap(MotionEvent e){
			return true;
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.supa_striker, menu);
		return true;
	}

	private class InitQCARTask extends AsyncTask<Void, Integer, Boolean>{

		private int mProgressValue = -1;
		@Override
		protected Boolean doInBackground(Void... arg0) {
			synchronized(mShutdownLock){
				QCAR.setInitParameters(SupaStriker.this, mQCARFlags);
				do{
					mProgressValue = QCAR.init();
					publishProgress(mProgressValue);
				}while(!isCancelled()&& mProgressValue >= 0 && mProgressValue < 100);
			}
			return (mProgressValue > 0);
		}
		
		protected void onProgressUpdate(Integer... values){
			
		}
		
		protected void onPostExecute(Boolean result){
			if(result){
				
				DebugLog.LOGD("initQCARTask::onPostExecute: QCAR " + 
				" initialization successful");
				
				updateApplicationStatus(APPSTATUS_INIT_TRACKER);
			}
			else{
				AlertDialog dialogError = new AlertDialog.Builder(SupaStriker.this)
				.create();
				dialogError.setButton(DialogInterface.BUTTON_POSITIVE,
						"Close",
						new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								System.exit(1);
							}
						});
			 String logMessage;
			 
			 if(mProgressValue == QCAR.INIT_DEVICE_NOT_SUPPORTED){
				 logMessage = "Failed to initialize QCAR because this device is not supported";
			}else{
				logMessage = "Failed to initialize QCAR";
			}
			 
			 DebugLog.LOGE("InitQCARTask::onPostExecute: "+logMessage+ " Exiting");
			 
			 dialogError.setMessage(logMessage);
			 dialogError.show();
		}
	}
	}
	private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean>{
		
		@Override
		protected Boolean doInBackground(Void... arg0){
			
			synchronized(mShutdownLock){
				return (loadTrackerData() > 0);
			}
		}
		
		protected void onPostExecute(Boolean result){
			DebugLog.LOGD("LoadTrackerTask:onPostExecute: execution " + 
		(result ? "sucessful" : "failed"));
			
			if(result){
				mSupaStrikerDataSetActive = true;
				
				updateApplicationStatus(APPSTATUS_INITED);
			}
			else{
				AlertDialog dialogError = new AlertDialog.Builder(SupaStriker.this)
				.create();
				dialogError.setButton(DialogInterface.BUTTON_POSITIVE, 
						"Close", new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								System.exit(1);
							}
						});
				dialogError.setMessage("Failed to load tracker data.");
				dialogError.show();
			}
		}
	}

	public static boolean loadLibrary(String nLibName){
		
		try{
			System.loadLibrary(nLibName);
			DebugLog.LOGI("Native library lib" + nLibName + ".so loaded");
			return true;
		}
		catch(UnsatisfiedLinkError ulee){
            DebugLog.LOGE("The library lib" + nLibName +
                    ".so could not be loaded");
		}
		catch(SecurityException se){
            DebugLog.LOGE("The library lib" + nLibName +
                    ".so was not allowed to be loaded");
		}
		
		return false;
	}
	private void initApplication(){
		int screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		if(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR){
			try{
				Field fullSensorField = ActivityInfo.class.getField("SCREEN_ORIENTATION_FULL_SENSOR");
				screenOrientation = fullSensorField.getInt(null);
			}catch(NoSuchFieldException ex){
				DebugLog.LOGE("NoSuchFieldException : initApplication");
			}
			catch(Exception ex){
				DebugLog.LOGE("Exception : initApplication");
				ex.printStackTrace();
			}
		}
		
		setRequestedOrientation(screenOrientation);
		
		updateActivityOrientation();
		
		storeScreenDimensions();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}
	
	private void updateActivityOrientation(){
		Configuration config = getResources().getConfiguration();
		
		boolean isPotrait = false;
		switch(config.orientation){
		case Configuration.ORIENTATION_PORTRAIT:
			isPotrait = true;
			break;
		case Configuration.ORIENTATION_LANDSCAPE:
			isPotrait = false;
			break;
		case Configuration.ORIENTATION_UNDEFINED:
			default:
				break;
		}
		
		DebugLog.LOGD("Activity is in " + (isPotrait ? "PORTRAIT" : "LANDSCAPE"));
		
		setActivityPotraitMode(isPotrait);
	}
	
	private void storeScreenDimensions(){
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mScreenWidth = metrics.widthPixels;
		mScreenHeight = metrics.heightPixels;
		
	}
	
	private void initApplicationAR(){
		initApplicationNative(mScreenWidth, mScreenHeight);
		
		//create openGL ES view:
		int depthSize = 16;
		int stencilSize = 0;
		boolean translucent = QCAR.requiresAlpha();
		
		mGlView = new QCARGLView(this);
		mGlView.init(mQCARFlags, translucent, depthSize, stencilSize);
		
		mRenderer = new SupaStrikerRenderer();
		mRenderer.mActivity = this;
		mGlView.setRenderer(mRenderer);
		
		LayoutInflater inflater = LayoutInflater.from(this);
		mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay, null, false);
		mUILayout.setVisibility(View.VISIBLE);
		mUILayout.setBackgroundColor(Color.BLACK);
		
		mLoadingDialogContainer = mUILayout.findViewById(R.id.loading_indicator);
		loadingDialogHandler.sendEmptyMessage(SHOW_LOADING_DIALOG);
		
		addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
	}
	
	public void updateRenderView(){
		int currentScreenRotation = getWindowManager().getDefaultDisplay().getRotation();
		if(currentScreenRotation != mLastScreenRotation)
		{
			if(QCAR.isInitialized() && (mAppStatus == APPSTATUS_CAMERA_RUNNING)){
				DebugLog.LOGD("SupaStrika::updateRenderView");
			}
			
			storeScreenDimensions();
			mRenderer.updateRendering(mScreenWidth, mScreenHeight);
			
			setProjectionMatrix();
			
			mLastScreenRotation = currentScreenRotation;
		}
	}
	private synchronized void updateApplicationStatus(int appStatus){
		if(mAppStatus == appStatus){
			return;
		}		
		mAppStatus = appStatus;
		
		switch(mAppStatus){
		case APPSTATUS_INIT_APP:
			initApplication();
			updateApplicationStatus(APPSTATUS_INIT_QCAR);
			break;
		case APPSTATUS_INIT_QCAR:
			try{
				mInitQCARTask = new InitQCARTask();
				mInitQCARTask.execute();
				
				DebugLog.LOGI("QCAR Initialization Successful");
			}catch(Exception ex){
				DebugLog.LOGE("QCAR SDK Initialization Failed");
			}
			break;
		case APPSTATUS_INIT_TRACKER:
			if(initTracker() > 0){
				updateApplicationStatus(APPSTATUS_INIT_APP_AR); 
			}
			break;
		case APPSTATUS_LOAD_TRACKER:
			try{
				mLoadTrackerTask = new LoadTrackerTask();
				mLoadTrackerTask.execute();
			}catch(Exception ex){
				DebugLog.LOGE("Load Tracker Failed "+ ex.getMessage());
			}
			break;
		case APPSTATUS_INITED:
			System.gc();
			
			onQCARInitializedNative();
			
			mRenderer.mIsActive = true;
			
			addContentView(mGlView,new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
			
			mUILayout.bringToFront();
			
			updateApplicationStatus(APPSTATUS_CAMERA_RUNNING);
			
			break;	
		case APPSTATUS_CAMERA_STOPPED:
			stopCamera();
			
			break;
		case APPSTATUS_CAMERA_RUNNING:
			startCamera();
			
			loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);
			mUILayout.setBackgroundColor(Color.TRANSPARENT);
			
			if(!setFocusMode(FOCUS_MODE_CONTINUOUS_AUTO)){
				mContAutoFocus = false;
				setFocusMode(FOCUS_MODE_NORMAL);
			}else{
				mContAutoFocus = true;
			}
			break;
		case APPSTATUS_INIT_APP_AR:
			initApplicationAR();
			
			updateApplicationStatus(APPSTATUS_LOAD_TRACKER);
			break;
		default:
			throw new RuntimeException("Invalid application state");
		}
	}
}
