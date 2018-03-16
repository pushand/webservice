package pw.pushan.webservice;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import pw.pushan.webservice.api.TestApi;
import pw.pushan.webservicelib.webservice.WebServiceHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView textView = findViewById(R.id.tv);
        textView.setText("Requesting...");
        WebServiceHelper.makeWebServiceCall(new TestApi(), new WebServiceHelper.Callback<String>() {
            @Override
            public void response(String response) {
                Log.d("Network", " response "+ response);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    textView.setText(Html.fromHtml(response, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    textView.setText(Html.fromHtml(response));
                }
            }

            @Override
            public void error(String error) {
                textView.setText(error);
            }
        }, this);
    }
}
