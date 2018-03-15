package pw.pushan.webservice.network.webservice;

import com.google.gson.Gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Created by Pushan on 3/2/16.
 */
public abstract class NetworkApi implements WebServiceApi {


    public static final String REQUEST_TYPE_GET = "GET";
    public static final String REQUEST_TYPE_POST = "POST";

    public static final int PRIORITY_HIGH = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_LOW = 2;
    public static final int PRIORITY_LOWEST = 3;

    private static final int CONNECTION_TIMEOUT_IN__TEN_SEC = 10;
    private static final int READ_TIMEOUT_IN_FIFTEEN_SEC = 15;
    private static final int SEC_TO_MILLSEC = 1000;

    public static final String APP_URL_ENCODED = "application/x-www-form-urlencoded";
    public static final String APP_JSON = "application/json";
    public static final String APP_TEXT_PLAIN = "text/plain";
//    private final Gson gson;

    private int connectionTimeoutInSeconds = CONNECTION_TIMEOUT_IN__TEN_SEC * SEC_TO_MILLSEC;
    private int readTimeoutInSeconds = READ_TIMEOUT_IN_FIFTEEN_SEC * SEC_TO_MILLSEC;

    private boolean doOutput = false;
    private String charset = "UTF-8";
    private String postBody = APP_URL_ENCODED;

    private String jsonPostBody;
    private WebServiceHelper.Callback nCallback;

    private int statusCode;

    public static final int API_STATE_IDLE = 1;
    public static final int API_STATE_CALLING = 1;
    public static final int API_STATE_RECEIVED = 2;
    public static final int API_STATE_RECEIVED_SUCCESS = 3;
    public static final int API_STATE_RECEIVED_ERROR = 4;
    //public ObservableInt observableApiState;

    public Object getObservableApiState() {
        return null;
    }

    public abstract String baseUrl();

    public abstract String apiUrl();

    public abstract boolean isGetRequest();

    public abstract int getRequestId();

    /*If you want to forcefully override the params that has been already set than override this method*/
    protected abstract HashMap<String, String> requestParams(HashMap<String, String> params);

    protected HashMap<String, String> params = new HashMap<>();

    public void addParams(String key, String value) {
        if (key != null) {
            params.put(key, value);
        }
    }

    public void addCallback(WebServiceHelper.Callback callback) {
        this.nCallback = callback;
    }

    public WebServiceHelper.Callback getCallback() {
        return nCallback;
    }

    public void setConnectionTimeoutInSeconds(int connectionTimeoutInSeconds) {
        this.connectionTimeoutInSeconds = connectionTimeoutInSeconds * SEC_TO_MILLSEC;
    }

    public void setReadTimeoutInSeconds(int readTimeoutInSeconds) {
        this.readTimeoutInSeconds = readTimeoutInSeconds * SEC_TO_MILLSEC;
    }

    public int getConnectionTimeoutInSeconds() {
        return connectionTimeoutInSeconds;
    }

    public int getReadTimeoutInSeconds() {
        return readTimeoutInSeconds;
    }

    public boolean isDoInput() {
        boolean doInput = true;
        return doInput;
    }

    public boolean isDoOutput() {
        return doOutput;
    }

    public void setDoOutput(boolean doOutput) {
        this.doOutput = doOutput;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getPostBody() {
        return postBody;
    }

    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }

    public String getRequestMethod() {
        return isGetRequest() ? REQUEST_TYPE_GET : REQUEST_TYPE_POST;
    }

    public String getJsonPostBody() {
        return jsonPostBody;
    }

    public void setJsonPostBody(String jsonPostBody) {
        this.jsonPostBody = jsonPostBody;
    }

    private void updateApiState(int apiState) {
        /*if (observableApiState != null) {
            observableApiState.set(apiState);
        }*/
    }

    @Override
    public void success(String response, int statusCode) {
        WebRequest.log("WS", "Response " + response);
        updateApiState(API_STATE_RECEIVED);
        setStatusCode(statusCode);
        if (nCallback != null) {
            Type[] genericInterfaces = nCallback.getClass().getGenericInterfaces();
            Gson gson = new Gson();
            try {
                for (Type genericInterface : genericInterfaces) {
                    if (genericInterface instanceof ParameterizedType) {
                        final Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                        /*WebServiceModel  model = gson.fromJson(response, genericTypes[0]);
                        model.setStatusCode(statusCode);*/
                        setStatusCode(statusCode);
                        nCallback.response(gson.fromJson(response, genericTypes[0]));
                        updateApiState(API_STATE_RECEIVED_SUCCESS);
                    }
                }
            } catch (Exception e) {
                //Helper.logCrash("Api : " + getClass().getSimpleName() + " error " + e.getMessage());
                e.printStackTrace();
                nCallback.error("Response didn't match Parameterized Type");
                updateApiState(API_STATE_RECEIVED_ERROR);
            }
        }
    }

    @Override
    public void failure(String message, int statusCode) {
        WebRequest.log("WS", "Error " + message);
        setStatusCode(statusCode);
        if (nCallback != null) {
            nCallback.error(message);
        }
        updateApiState(API_STATE_RECEIVED_ERROR);
    }

    protected int priority() {
        return PRIORITY_LOW;
    }

    public int getPriority() {
        return priority();
    }

    @Override
    public void progress(int percentage) {
        WebRequest.log("WS", "progress " + percentage);
    }

    /**
     * Override this method if you want to do long task before netwok call
     */
    public void performTask() {
        updateApiState(API_STATE_CALLING);
    }

    @Override
    public void perforTaskAfterResponse(String rawResponse) {

    }

    public String getAuthorization() {
        return null;
    }

    public String getAccept() {
        return null;
    }


    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
