package model;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by abzalsahitov@gmail.com  on 5/21/18.
 */
public class Push {
    public static int DELAY_DAYTIME = 1;

    Long id;
    
    private Calendar created;
    private Calendar sent;
    
    String body;
    String title;
    String subtitle;
    String actionKey;
    
    Long deviceToken_id;
    Long user_id;
    
    long timestamp;
    
    Map<String, String> arguments;
    

    int badge;
    int s;
    int s2;
    int attempts;
    int delayType;

    /*Flag to get only admin device tokens. This should be replaced to app_id in future*/
    boolean admin;

    public Calendar getCreated() {
        return created;
    }

    public void setCreated(Calendar created) {
        this.created = created;
    }

    public Calendar getSent() {
        return sent;
    }

    public void setSent(Calendar sent) {
        this.sent = sent;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getActionKey() {
        return actionKey;
    }

    public void setActionKey(String actionKey) {
        this.actionKey = actionKey;
    }

    public Long getDeviceToken_id() {
        return deviceToken_id;
    }

    public void setDeviceToken_id(Long deviceToken_id) {
        this.deviceToken_id = deviceToken_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, String> arguments) {
        this.arguments = arguments;
    }

    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public int getS2() {
        return s2;
    }

    public void setS2(int s2) {
        this.s2 = s2;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getDelayType() {
        return delayType;
    }

    public void setDelayType(int delayType) {
        this.delayType = delayType;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Push{" +
                "id=" + id +
                ", created=" + created +
                ", sent=" + sent +
                ", body='" + body + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", actionKey='" + actionKey + '\'' +
                ", deviceToken_id=" + deviceToken_id +
                ", user_id=" + user_id +
                ", timestamp=" + timestamp +
                ", arguments=" + arguments +
                ", badge=" + badge +
                ", s=" + s +
                ", s2=" + s2 +
                ", attempts=" + attempts +
                ", delayType=" + delayType +
                ", admin=" + admin +
                '}';
    }
}
