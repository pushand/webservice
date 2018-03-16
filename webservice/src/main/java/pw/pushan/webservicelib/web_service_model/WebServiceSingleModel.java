package pw.pushan.webservicelib.web_service_model;


/**
 * Created by pushan on 3/9/16.
 */
public class WebServiceSingleModel<T> extends WebServiceModel {

    protected T response;

    public T getResponse() {
        return response;
    }

    public int getNotificationCount() {
        return notif_count;
    }

    public void setResponse(T response) {
        this.response = response;
    }
}
