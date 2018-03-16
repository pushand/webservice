package pw.pushan.webservicelib.web_service_model;

/**
 * Created by Pushan on 3/3/16.
 */
public abstract class WebServiceModel {

    protected boolean status;
    protected String message;
    protected int notif_count;
    protected int statusCode;
    protected String placeholder;

    public boolean isSuccess () {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

}
