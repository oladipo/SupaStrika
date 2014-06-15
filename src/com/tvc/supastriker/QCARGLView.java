package com.tvc.supastriker;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import com.qualcomm.QCAR.QCAR;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

public class QCARGLView extends GLSurfaceView{

	private static boolean mUseOpenGLES2 = true;
	
	public QCARGLView(Context context) {
		super(context);
		
	}
	
	public void init(int flags, boolean translucent, int depth, int stencil ){
		
		mUseOpenGLES2 = (flags & QCAR.GL_20) != 0;
		DebugLog.LOGI("Using openGL ES " + (mUseOpenGLES2 ? "2.0" : "1.x"));
		DebugLog.LOGI("Using " + (translucent ? "translucent" : "opaque")+ 
				"GLView, depth buffer size: "+ depth + ", stencil size: "+ stencil);
		
		if(translucent){
			this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		}
		
		setEGLContextFactory(new ContextFactory());
		setEGLConfigChooser(translucent ?
				new ConfigChooser(8,8,8,8,depth, stencil):
					new ConfigChooser(5,6,5,0, depth, stencil));
	}
	
	private static class ContextFactory implements GLSurfaceView.EGLContextFactory{

		private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
		
		public EGLContext createContext(EGL10 egl, EGLDisplay display,
				EGLConfig eglConfig) {
			EGLContext context;
			if(mUseOpenGLES2){
				DebugLog.LOGI("Creating openGL ES 2.0 context");
				checkEglError("Before eglCreateContext", egl);
				int[] attrib_list_g120 = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
				context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list_g120);
			}else{
				DebugLog.LOGI("Creating openGL ES 1.x context");
				checkEglError("Before eglCreateContext", egl);
				int[] attrib_list_gl1x = {EGL_CONTEXT_CLIENT_VERSION,1, EGL10.EGL_NONE};
				context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list_gl1x);
				
			}
			checkEglError("After eglCreateContext", egl);
			return context;
		}

		public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
			egl.eglDestroyContext(display, context);
		}
		
	}
	
	private static void checkEglError(String prompt, EGL10 eg1){
		int error;
		while((error = eg1.eglGetError()) != EGL10.EGL_SUCCESS){
			DebugLog.LOGE(String.format("%s: EGL error: 0x%x",prompt, error));
		}
	}
	
	private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser{

        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
        
		public ConfigChooser(int r, int g, int b, int a, int depth, int stencil){
			mRedSize = r;
			mGreenSize = g;
			mBlueSize = b;
			mAlphaSize = a;
			mDepthSize = depth;
			mStencilSize = stencil;
		}

		private EGLConfig getMatchingConfig(EGL10 egl, EGLDisplay display,
				int[] configAttribs){
			int[] num_config = new int[1];
			egl.eglChooseConfig(display, configAttribs, null, 0, num_config);
			
			int numConfigs = num_config[0];
			if(numConfigs <= 0)
				throw new IllegalArgumentException("No matching EGL configs");
			
			EGLConfig[] configs = new EGLConfig[numConfigs];
			egl.eglChooseConfig(display, configAttribs, configs, numConfigs, num_config);
			
			return chooseConfig(egl, display, configs);
		}
		
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
			if(mUseOpenGLES2){
				final int EGL_OPENGL_ES2_BIT = 0x0004;
				final int[] s_configAttribs_gl20 = {
					EGL10.EGL_RED_SIZE, 4,
					EGL10.EGL_GREEN_SIZE, 4,
					EGL10.EGL_BLUE_SIZE, 4,
					EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
					EGL10.EGL_NONE
				};
				return getMatchingConfig(egl, display, s_configAttribs_gl20);
			}else{
				final int EGL_OPENGL_ES1X_BIT  = 0x0001;
				final int[] s_configAttribs_gl1x ={
						EGL10.EGL_RED_SIZE, 5,
						EGL10.EGL_GREEN_SIZE, 6,
						EGL10.EGL_BLUE_SIZE, 5,
						EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES1X_BIT,
						EGL10.EGL_NONE
				};
				return getMatchingConfig(egl, display, s_configAttribs_gl1x);
			}
		}
		
		public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs){
			
			for(EGLConfig config : configs){
				int d = findConfigAttrib(egl, display, config,
						EGL10.EGL_DEPTH_SIZE, 0);
				int s = findConfigAttrib(egl, display, config,
						EGL10.EGL_STENCIL_SIZE, 0);
				if(d < mDepthSize || s < mStencilSize){
					continue;
				}
				
				int r = findConfigAttrib(egl, display, config,
						EGL10.EGL_RED_SIZE, 0);
				int g = findConfigAttrib(egl, display, config,
						EGL10.EGL_GREEN_SIZE, 0);
				int b = findConfigAttrib(egl, display, config,
						EGL10.EGL_BLUE_SIZE,0);
				int a = findConfigAttrib(egl, display, config,
						EGL10.EGL_ALPHA_SIZE, 0);
				
				if(r == mRedSize &&
					g == mGreenSize &&
					b == mBlueSize &&
					a == mAlphaSize){
					return config;
			}
				
		}
		return null;
	}
		
		private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute,
				int defaultValue){
			if(egl.eglGetConfigAttrib(display, config, attribute, mValue))
			return mValue[0];
			
			return defaultValue;
		}
}
}
