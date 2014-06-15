package com.tvc.supastriker;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

public class SupaStrikerSplashScreen extends Activity {

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_supa_striker_splash_screen);
		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			
			public void run(){
				startActivity(new Intent(SupaStrikerSplashScreen.this, About.class));
			}
		}, 2000L);
	}

	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		setContentView(R.layout.activity_supa_striker_splash_screen);
	}

}
