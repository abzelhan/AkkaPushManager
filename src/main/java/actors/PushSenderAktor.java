package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.gson.Gson;
import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.zaxxer.hikari.HikariDataSource;
import kz.wg.push.FirebaseResult;
import model.Push;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.CMDColors;
import util.DBUtil;

import static util.DBUtil.blockDeviceToken;
import static util.DBUtil.getTokens;
import util.PushConfigs;
import util.gsm.Content;
import util.gsm.POST2GSM;

/**
 * Created by abzalsahitov@gmail.com  on 5/21/18.
 */
public class PushSenderAktor extends AbstractActor {

  private final static  Logger logger = LoggerFactory.getLogger(PushSenderAktor.class);

    static public Props props() {
        return Props.create(PushSenderAktor.class, () -> new PushSenderAktor());
    }

    public PushSenderAktor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Push.class, push -> {
                    send(push);
                    context().stop(getSelf());
                })
                .match(String.class,msg->{
                    System.out.println(msg);
                })
                .build();
    }
    
    public void send(Push p) throws Exception {

        int count = 0;
        
        System.out.println(" > sending: " + p.getSubtitle() + " to userId: " + p.getUser_id() + " isAdmin: " + p.isAdmin());

        List<String> tokens = DBUtil.getTokens(p.getUser_id(), "ios", p.isAdmin());

        System.out.println(" > sending " + p.getSubtitle());
        System.out.println(" > got ios:  " + tokens.size());

        String topic = PushConfigs.apns_topic;

        if (p.isAdmin()) {
            topic = PushConfigs.apns_topic_admin;
        }
        String reason = "";

        for (String token : tokens) {
            final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
            payloadBuilder.setAlertSubtitle(p.getTitle());
            payloadBuilder.setAlertTitle(p.getSubtitle());
            
            if (p.getBody() != null && !p.getBody().isEmpty()) {
                payloadBuilder.setAlertBody(p.getBody());
            }

            boolean needConvertType = false;
            try {
                needConvertType = p.getArguments().get("action").equalsIgnoreCase("viewPost");
            } catch (Exception e) {
                e.printStackTrace();
            }


            for (String key : p.getArguments().keySet()) {
                if (needConvertType && key.equalsIgnoreCase("type")) {
                    payloadBuilder.addCustomProperty(key, Integer.parseInt(p.getArguments().get(key)));
                } else {
                    payloadBuilder.addCustomProperty(key, p.getArguments().get(key));
                }
            }

            payloadBuilder.setBadgeNumber(p.getBadge());

            final String payload = payloadBuilder.buildWithDefaultMaximumLength();

            SimpleApnsPushNotification pushNotification =
                    new SimpleApnsPushNotification(token, topic, payload);

            System.out.println(">payload: " + pushNotification.getPayload() + " to " + token);

            ApnsClient[] clients = {PushConfigs.client_dev, PushConfigs.client_prod};

            boolean accepted = false;
            for (ApnsClient client : clients) {
                PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture
                        = client.sendNotification(pushNotification);

                reason = sendNotificationFuture.get().getRejectionReason();
                accepted = sendNotificationFuture.get().isAccepted();
            }

            count = checkForAccepted(p, count, reason, token, accepted);
        }

        tokens = getTokens(p.getUser_id(), "android", p.isAdmin());

        System.out.println("> got android:  " + tokens.size());

        String apiKey = PushConfigs.firebase_apiKey;
        if (p.isAdmin()) {
            apiKey = PushConfigs.firebase_apiKey_admin;
        }

        for (String token : tokens) {
            System.out.println(">seding push to android: " + token + " key: " + apiKey);

            boolean accepted = sendToFirebase(p, token, apiKey);

            count = checkForAccepted(p, count, reason, token, accepted);
        }

        tokens = getTokens(p.getUser_id(), "web", p.isAdmin());

        System.out.println(">got web:  " + tokens.size());

        apiKey = PushConfigs.firebase_apiKey_admin_web;

        for (String token : tokens) {
            System.out.println(">seding push to web: " + token + " key: " + apiKey);
            boolean accepted = sendToFirebase(p, token, apiKey);

            count = checkForAccepted(p, count, reason, token, accepted);
        }

        if (count > 0) {
            //p.setState(1);
            //p._save();
            DBUtil.setSended(p.getId());
        }
    }

    private int checkForAccepted(Push p, int count, String reason, String token, boolean accepted) throws SQLException {
        if (accepted) {
            System.out.println(CMDColors.ANSI_GREEN + " Sent to " + token + " title " + p.getTitle() + " [" + p.getId() + "]" + CMDColors.ANSI_RESET);
            count++;
        } else {
            System.out.println(CMDColors.ANSI_RED + " Can't send to " + token + " title " + p.getTitle() + " [" + p.getId() + "]. Removing token" + CMDColors.ANSI_RESET);
            //DeviceToken.block(token, reason);
            blockDeviceToken(token,reason);
            /*
            TODO: we should block device token if we couldn't sent to it
            */
        }
        return count;
    }


    public boolean sendToFirebase(Push p, String token, String apiKey) {
        boolean sent = false;

        Content content = new Content();
        content.addRegId(token);
        Map<String, String> map = new HashMap<>();

        map.put("title", p.getTitle());
        if (p.getSubtitle() == null || p.getSubtitle().isEmpty()) {
            map.put("subtitle", "АвтоВсё");
        } else {
            map.put("subtitle", p.getSubtitle());
        }

        if (p.getBody() != null && !p.getBody().isEmpty()) {
            map.put("body", p.getBody());
        }

        map.put("badge", ""+p.getBadge());
        map.put("push_id", "" + p.getId());
        map.put("timestamp", "" + System.currentTimeMillis());

        content.setData(map);

        for (String key : p.getArguments().keySet()) {
            map.put(key, p.getArguments().get(key));
        }

        content.setData(map);

        String response = POST2GSM.post(apiKey, content);
        try {
            Gson gson = new Gson();
            FirebaseResult result = gson.fromJson(response, FirebaseResult.class);

            if (result.getFailure() == 1) {
                String reason = (result.getResults() != null && !result.getResults().isEmpty()) ?
                        result.getResults().get(0).getError() : "";
                //DeviceToken.block(token, reason);
                blockDeviceToken(token,reason);
                /*
                TODO: we should block device token if we couldn't sent to it
                */

            } else if (result.getSuccess() == 1) {
                sent = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sent;
    }

}
