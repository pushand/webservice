package pw.pushan.webservicelib.webservice;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static pw.pushan.webservicelib.BuildConfig.DEBUG;

/**
 * Created by Pushan on 3/2/16.
 */
public class WebRequest extends NetworkRequest {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private int statusCode;
    private final Handler handler;

    public WebRequest(WebServiceApi webServiceApi, Context context) {
        super(webServiceApi, context);
        handler = new Handler(context.getMainLooper());
    }

    @Override
    public void makeRequest(HashMap<String, String> requestParams) {
        if (isConnected()) {
            call(requestParams);
        } else {
            failed("No network");
        }
    }

    private void failed(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                webServiceApi.failure(message, statusCode);
            }
        });
    }

    private void success(final String message) {
        webServiceApi.perforTaskAfterResponse(message);
        handler.post(new Runnable() {
            @Override
            public void run() {
                webServiceApi.success(message, statusCode);
            }
        });
    }

    private void call(HashMap<String, String> requestParams) {
        HttpURLConnection urlConnection = null;
        try {
            NetworkApi networkApi = (NetworkApi) webServiceApi;
            URL url = getUrl(networkApi, requestParams);
            log("Service", "Request \n" + url.toExternalForm());
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(networkApi.getReadTimeoutInSeconds());
                urlConnection.setConnectTimeout(networkApi.getConnectionTimeoutInSeconds());
                urlConnection.setRequestMethod(networkApi.getRequestMethod());
                urlConnection.setDoInput(networkApi.isDoInput());
                urlConnection.setDoOutput(networkApi.isDoOutput());
                urlConnection.setRequestProperty("Authorization", networkApi.getAuthorization());
                if (networkApi.getAccept() != null) {
                    urlConnection.setRequestProperty("Accept", networkApi.getAccept());
                }
                urlConnection.setRequestProperty("Accept-Charset", networkApi.getCharset());
                if (networkApi.getPostBody() != null && networkApi.getPostBody().length() > 0) {
                    urlConnection.setRequestProperty("Content-Type", networkApi.getPostBody() + ";charset=" + networkApi.getCharset());
                }
                String data = null;
                if (!networkApi.isGetRequest()) {
                    if (networkApi.getPostBody().equals(NetworkApi.APP_URL_ENCODED)) {
                        Uri.Builder builder = new Uri.Builder();
                        Log.d("API", "api name "+ networkApi.getClass().getSimpleName());
                        for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                            builder.appendQueryParameter(entry.getKey(), entry.getValue()/* == null ? "" : entry.getValue()*/);
                        }
                        data = builder.build().getEncodedQuery();
                    } else if (networkApi.getPostBody().equals(NetworkApi.APP_JSON)) {
                        data = networkApi.getJsonPostBody();
                    }
                    log("Service", "Header \n" + urlConnection.getRequestProperty("Authorization"));
                    log("Service", "Post data \n" + data);
                    if (data != null) {
                        OutputStream os = urlConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, networkApi.getCharset()));
                        writer.write(data);
                        writer.flush();
                        writer.close();
                        os.close();
                    }
                    urlConnection.connect();
                }
                statusCode = urlConnection.getResponseCode();
                networkApi.setStatusCode(statusCode);

                int fileLength = urlConnection.getContentLength();

                String response = readStream(urlConnection.getInputStream(), fileLength);
                if (statusCode == 0 || statusCode > 227) {
                    failed("TimeOut");
                } else {
                    success(response);
                }
            } catch (IOException e) {
                String errorMessage = e.getMessage();
                try {
                    statusCode = urlConnection != null ? urlConnection.getResponseCode() : 0;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                failed(readStream(urlConnection.getErrorStream(), 0));
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        } catch (MalformedURLException e) {
            failed("MalformedURLException " + " " + e.getMessage());
        }

    }

    private void publishProgress(int progress) {
        webServiceApi.progress(progress);
    }

    private URL getUrl(NetworkApi networkApi, HashMap<String, String> requestParams) throws MalformedURLException {
        Uri.Builder builder = Uri.parse(networkApi.baseUrl() + networkApi.apiUrl()).buildUpon();
        if (networkApi.isGetRequest()) {
            for (Map.Entry<String, String> entry : requestParams.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return new URL(builder.build().toString());
    }

    private String readStream(InputStream inputStream, int fileLength) {
        if (inputStream != null) {
            StringWriter writer = new StringWriter();
            try {
                copy(inputStream, writer, fileLength);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return writer.toString();
        } else {
            return null;
        }
    }

    private void copy(InputStream input, Writer output, int fileLength) throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output, fileLength);
    }

    private int copy(Reader input, Writer output, int fileLength) throws IOException {
        return (int) copyLarge(input, output, fileLength);
    }

    private long copyLarge(Reader input, Writer output, int fileLength) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            if (fileLength > 0) {
                webServiceApi.progress((int) (count * 100 / fileLength));
            }
        }
        return count;
    }

    public static void log(String logKey, String msg) {
        if (DEBUG && msg != null) {
            Log.d(logKey, msg);
        }
    }


}
