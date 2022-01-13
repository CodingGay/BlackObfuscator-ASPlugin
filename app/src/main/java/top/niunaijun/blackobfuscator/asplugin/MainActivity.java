package top.niunaijun.blackobfuscator.asplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (System.currentTimeMillis() > 3) {
            Log.d("123", "onCreate: sdfasdffakea");
        } else {
            Log.d("123", "onCreate: aax1xaaa");
        }
        Log.d("123", "onCreate: " + Abx.go());
    }
}