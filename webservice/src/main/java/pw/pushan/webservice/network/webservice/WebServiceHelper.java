package pw.pushan.webservice.network.webservice;

import android.content.Context;
import android.os.Handler;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import static pw.pushan.webservice.network.webservice.WebRequest.log;

/**
 * Created by pushan on 24/07/15.
 */
public class WebServiceHelper {

    private static WebServiceHelper webServiceHelper;
    private ExecutorService executorService;
    private Queue<ApiTask> requestPriorityQueue;
    private Handler handler;

    private WebServiceHelper() {}


    public interface Callback<T> {

        void response(T response);

        void error(String error);

    }

    private Future<?> submit;

    public static void makeWebServiceCall(NetworkApi networkApi, Callback callback, Context context) {
        networkApi.addCallback(callback);
        if (makeAvailable()) {
            webServiceHelper.requestPriorityQueue.add(new ApiTask(networkApi, context));
            log("WSH", "Q size " + webServiceHelper.requestPriorityQueue.size());
            if (webServiceHelper.submit == null || webServiceHelper.submit.isDone()) {
                log("WSH", "request");
                webServiceHelper.submit = webServiceHelper.executorService.submit(webServiceHelper.runnable);
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
                log("WSH", "wait for more request");
            while (!requestPriorityQueue.isEmpty()) {
                ApiTask apiTask = requestPriorityQueue.poll();
                log("WSH", "requesting " + apiTask.getNetworkApi().apiUrl());

                executorService.execute(apiTask);
            }
        }
    };

    public static void makeWebServiceCallOnSameThread(NetworkApi networkApi, Callback callback, Context context) {
        networkApi.addCallback(callback);
        networkApi.performTask();
        NetworkRequest networkRequest = new WebRequest(networkApi, context);
        networkRequest.makeRequest(networkApi.requestParams(networkApi.params));
    }

    public static void newThread(Runnable runnable) {
        if (makeAvailable()) {
            webServiceHelper.executorService.execute(runnable);
        }
    }

    private static boolean makeAvailable() {
        if (webServiceHelper == null) {
            webServiceHelper = new WebServiceHelper();
            webServiceHelper.executorService = Executors.newCachedThreadPool();
            webServiceHelper.requestPriorityQueue = new PriorityBlockingQueue<>();
        }
        return webServiceHelper.executorService != null;
    }

    private static void shutdown() {
        if (webServiceHelper != null && webServiceHelper.executorService != null) {
            webServiceHelper.executorService.shutdown();
        }
    }

    private static class ApiTask implements Runnable, Comparable<ApiTask> {

        private NetworkApi networkApi;
        private Context context;

        public ApiTask(NetworkApi networkApi, Context context) {
            this.networkApi = networkApi;
            this.context = context;
        }

        @Override
        public void run() {
            networkApi.performTask();
            NetworkRequest networkRequest = new WebRequest(networkApi, context);
            networkRequest.makeRequest(networkApi.requestParams(networkApi.params));
        }

        public NetworkApi getNetworkApi() {
            return networkApi;
        }

        @Override
        public int compareTo(ApiTask apiTask) {
            return networkApi.getPriority() - apiTask.getNetworkApi().getPriority();
        }
    }


}