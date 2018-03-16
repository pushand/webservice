package pw.pushan.webservicelib.webservice;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;

/**
 * Created by Pushan on 3/2/16.
 */
public abstract class NetworkRequest {

    protected WebServiceApi webServiceApi;
    private Context context;
    private NetworkInfo networkInfo;

    public NetworkRequest(WebServiceApi webServiceApi, Context context) {
        this.context = context;
        this.webServiceApi = webServiceApi;
        initConnection();
    }

    private void initConnection() {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connMgr.getActiveNetworkInfo();
    }

    public boolean isConnected() {
        return  networkInfo != null && networkInfo.isConnected();
    }

    public abstract void makeRequest(HashMap<String, String> params);
}
