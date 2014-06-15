package com.tvc.supastriker;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import com.qualcomm.QCAR.QCAR;

import android.opengl.GLSurfaceView;;

public class SupaStrikerRenderer implements GLSurfaceView.Renderer{
	public boolean mIsActive = false;
	public SupaStriker mActivity;
	
	public native void initRendering();
	public native void updateRendering(int width, int height);
	public native void renderFrame();
	
	public void onSurfaceCreated(GL10 g1, EGLConfig config){
		DebugLog.LOGD("GLRenderer::onSurfaceCreated");
		initRendering();
		QCAR.onSurfaceCreated();
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height){
		DebugLog.LOGD("GLRenderer::onSurfaceChanged");
		updateRendering(width, height);
		QCAR.onSurfaceChanged(width, height);
	}
	
	public void onDrawFrame(GL10 gl){
		if(!mIsActive){
			return;
		}
		mActivity.updateRenderView();
		
		renderFrame();
	}
}
