package pw.pushan.webservice.api;

import pw.pushan.webservicelib.webservice.NetworkApi;

import java.util.HashMap;

public class TestApi extends NetworkApi {

    @Override
    public String baseUrl() {
        return "https://www.google.co.in/";
    }

    @Override
    public String apiUrl() {
        return "search";
    }

    @Override
    public boolean isGetRequest() {
        return true;
    }

    @Override
    public int getRequestId() {
        return 0;
    }

    @Override
    protected HashMap<String, String> requestParams(HashMap<String, String> params) {
        params.put("q", "android");
        return params;
    }
}
