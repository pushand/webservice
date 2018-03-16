package pw.pushan.webservicelib.webservice;


/**
 * Created by Pushan on 3/2/16.
 */
public interface WebServiceApi {

    void success(String response, int statusCode);
    void failure(String message, int statusCode);
    void progress(int percentage);
    void perforTaskAfterResponse(String rawResponse);

}
