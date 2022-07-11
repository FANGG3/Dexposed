package com.example.ndk;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.example.ndk.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'ndk' library on application startup.

    static Context context;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this.getApplicationContext();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Example of a call to a native method
        TextView tv = binding.sampleText;
        //tv.setText(stringFromJNI());
        //DexLearn dexLearn = new DexLearn();
        //dexLearn.context = this.getApplicationContext();
        //dexLearn.getDex(this.getClass().getClassLoader());

    }

    /**
     * A native method that is implemented by the 'ndk' native library,
     * which is packaged with this application.
     */

    public native String stringFromJNI();
}