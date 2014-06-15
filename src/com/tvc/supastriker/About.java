package com.tvc.supastriker;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity implements OnClickListener {

private Button mStartButton;
private TextView mAboutText;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		mAboutText = (TextView) findViewById(R.id.about_text);
		mAboutText.setText(Html.fromHtml(getString(R.string.about_text)));
		mAboutText.setMovementMethod(LinkMovementMethod.getInstance());
		
        mAboutText.setLinkTextColor(getResources().getColor(
                R.color.holo_light_blue));
		
		mStartButton = (Button) findViewById(R.id.button_start);
		mStartButton.setOnClickListener(this);
	}

	private void startActivity(){
		Intent i = new Intent(this, SupaStriker.class);
		startActivity(i);
	}
	
	public void onClick(View v){
		switch(v.getId()){
		case R.id.button_start:
			startActivity();
			break;
		}
	}

}
