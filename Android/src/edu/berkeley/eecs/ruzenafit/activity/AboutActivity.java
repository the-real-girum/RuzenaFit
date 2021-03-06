package edu.berkeley.eecs.ruzenafit.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import edu.berkeley.eecs.ruzenafit.R;

public class AboutActivity extends Activity {
//	private static Context mContext;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutactivity);
//        mContext = this;
        
        final Button button = (Button) findViewById(R.id.moreinfo);
        button.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
				//The intent will open our website
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				i.addCategory(Intent.CATEGORY_BROWSABLE);
				i.setData(Uri.parse("http://ehs.sph.berkeley.edu/edmund/calfit/"));
				startActivity(i); 
        	}
        });
    }
}