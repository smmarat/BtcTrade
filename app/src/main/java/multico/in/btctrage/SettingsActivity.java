package multico.in.btctrage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import in.multico.tool.Tool;

public class SettingsActivity extends AppCompatActivity {

    private EditText pub, priv;
    private boolean scanPub = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        priv = (EditText) findViewById(R.id.set_priv);
        pub = (EditText) findViewById(R.id.set_pub);
        priv.setText(Tool.getSecret(this));
        pub.setText(Tool.getPub(this));
    }

    public void scanPubKey(View view) {
        scanPub = true;
        new IntentIntegrator(this).initiateScan();
    }

    public void scanPrivKey(View view) {
        scanPub = false;
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String s = result.getContents();
                if (scanPub) {
                    pub.setText(s);
                    Tool.setPub(SettingsActivity.this, s);
                }
                else {
                    priv.setText(s);
                    Tool.setSecret(SettingsActivity.this, s);
                }
            }
        }
    }

    public void doContinue(View view) {
        onBackPressed();
    }
}
