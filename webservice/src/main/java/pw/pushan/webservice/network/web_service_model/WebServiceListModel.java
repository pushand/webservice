package pw.pushan.webservice.network.web_service_model;

import java.util.List;

/**
 * Created by pushan on 3/9/16.
 */
public class WebServiceListModel<T> extends WebServiceModel {

    protected List<T> response;

    public List<T> getResponse() {
        return response;
    }

    public int getNotificationCount() {
        return notif_count;
    }

    public String getPhotoboothPlaceholder() {
        return placeholder;
    }
}
