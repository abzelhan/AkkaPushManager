/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.auth.ApnsSigningKey;
import java.io.File;

/**
 *
 * @author bakhyt
 */
public class PushConfigs {

    public static ApnsClient client_dev = null;
    public static ApnsClient client_prod = null;
    static String apns_filename = "/crypto/p8/AuthKey_MEU3QB52A5.p8";
    static String apns_teamId = "ZUHFYH44LS";
    static String apns_keyId = "MEU3QB52A5";
    public static String apns_topic = "kz.smartfuture.autoVse";
    public static String apns_topic_admin = "kz.dnd.avtovse.service";

    public static String firebase_apiKey
            = "AAAAW67jbFU:APA91bGuPua8fr2a8P-cqb6JCJjDq-ScC6zgUyUjhaIxjiBKP9wn91gBbouxhcLMhpeNmSbE0M9pbrWxnP-T9FW3EaSVMG0RNHv36XuxYdOWcEPt_UFeKZt9SHFoyFwdE_5va1Mohf5t";
    public static String firebase_apiKey_admin
            = "AAAADvBddSU:APA91bEzyQsLz8WOFARqlsQNyKaJRaWA2APDzBDQhEzaiEhrsl8rXY-prqfGyP0JUbfFuNbkpVD188L5Cz-Lgv0z9KlU8Si-yc38hXO4udDthmk-IRor-ptsUtHu5K7SeDCGKmGbQdMt";
    public static String firebase_apiKey_admin_web
            = "AAAA_RE_Hyk:APA91bEV8a3ifSE2OetA50Ojroyms3UITvSLRx_VPMtEwzSSt0qwlMtyPrzfw48SpY5CULV563Ou3V6ZyuluJQmMLKfZfemvixmGFDJY5Ms32fHALVesSIBV8vPXwsLCvhBIRvBdUlhO";


    public static void initAPNS() throws Exception {
        if (client_dev == null) {

            client_dev = new ApnsClientBuilder()
                    .setSigningKey(ApnsSigningKey.loadFromPkcs8File(new File(apns_filename),
                            apns_teamId, apns_keyId))
                    .setApnsServer("api.development.push.apple.com", 443)
                    .build();
        }

        if (client_prod == null) {
            client_prod = new ApnsClientBuilder()
                    .setSigningKey(ApnsSigningKey.loadFromPkcs8File(new File(apns_filename),
                            apns_teamId, apns_keyId))
                    .setApnsServer("api.push.apple.com", 443)
                    .build();
        }
    }
}
