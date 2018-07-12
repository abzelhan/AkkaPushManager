import actors.PushSenderAktor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import model.Push;
import util.DBUtil;
import util.PushConfigs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by abzalsahitov@gmail.com on 5/21/18.
 */
public class PushManagerRunner {

    public static final int PUSH_FETCH_LIMIT = 500;

    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("pushmanager");
        PushConfigs.initAPNS();
        List<ActorRef> actorWorkers = new ArrayList<>();
        try {
            for (int i = 0; i < PUSH_FETCH_LIMIT; i++) {
                //creating new aktor
                final ActorRef howdyGreeter = system.actorOf(PushSenderAktor.props());
                actorWorkers.add(howdyGreeter);
            }

            while (true) {
                try {
                    //here we need to obtain all pushes
                    List<Push> pushes = DBUtil.getAvailablePushesListAndMarkFetched(PUSH_FETCH_LIMIT);
                    for (int i = 0; i < PUSH_FETCH_LIMIT; i++) {
                        //triggering aktor to work and sending push to it
                        actorWorkers.get(i).tell(pushes.get(i), ActorRef.noSender());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                for (ActorRef actorWorker : actorWorkers) {
                    system.stop(actorWorker);
                }
                //before we terminate system we need to wait until all actors complete their roles
                //4 seconds will enough for this action
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //nice work. Thank you for sending pushes
            system.terminate();
            // In your headphones must be played a very sad song....
            //R.I.P 2018 - ~
        }
    }

}
