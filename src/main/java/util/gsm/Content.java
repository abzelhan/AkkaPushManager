package util.gsm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by baha on 10/30/15.
 */
public class Content{
    private String to;
    private String priority = "normal";
    //private List<String> registration_ids;
    private Map<String,String> data;
    private Map<String,String> notification;

    public void addRegId(String regId){
        /*if(registration_ids == null)
            registration_ids = new LinkedList<String>();
        registration_ids.add(regId);*/
        to = regId;
    }

    public void createData(String title, String message){
        if(data == null)
            data = new HashMap<String,String>();

        data.put("title", title);
        data.put("message", message);
    }

    /*public List<String> getRegistration_ids() {
        return registration_ids;
    }

    public void setRegistration_ids(List<String> registration_ids) {
        this.registration_ids = registration_ids;
    }*/

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Map<String, String> getNotification() {
        return notification;
    }

    public void setNotification(Map<String, String> notification) {
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
