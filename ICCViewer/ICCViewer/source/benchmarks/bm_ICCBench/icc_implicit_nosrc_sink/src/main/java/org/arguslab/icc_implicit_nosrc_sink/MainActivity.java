package org.arguslab.icc_implicit_nosrc_sink;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

/**
 * @testcase_name ICC_Implicit_NoSrc_Sink
 * @author Fengguo Wei & Sankardas Roy
 * @author_mail fgwei521@gmail.com & sroy@ksu.edu
 *
 * @description Insensitive value v is sent to component FooActivity via implicit ICC.
 * 				In FooActivity, it will retrieve value v and leak it.
 * @dataflow v -> MainActivity's intent -> _
 * @number_of_leaks 0
 * @challenges The analysis must be able to resolve implicit (Action) ICC calls and handle data flow
 * 				across different components.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String v = "noSrc";

        Intent i = new Intent("amandroid.impliciticctest_action.testaction");
        i.putExtra("data", v);
        startActivity(i);
    }
    
}