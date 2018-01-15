package Services;

import Config.ConfigReader;
import Config.ConfigTwitter;
import MessageHandler.MessageConstructorTwitter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import twitter4j.Status;

import java.awt.*;
import java.util.*;
import java.util.List;

import static jdk.nashorn.internal.objects.NativeMath.min;

public class ServiceTwitter extends TimerTask {

    Map<String, Boolean> firstRunMap = new HashMap<>();

    Map<String, List<Long>> checkedMap = new HashMap<>();

    JDA jda;
    int initialTweetCheck = 8;
    int tweetCheck = 2;

    public ServiceTwitter (JDA jda){
        this.jda = jda;
        initialise();
    }

    public void initialise(){
        ConfigReader configReader = new ConfigReader();
        Map<String, List<Long>> twitterMap = configReader.getTwitterMap();
        for(Map.Entry<String, List<Long>> entry : twitterMap.entrySet()){
            String username = entry.getKey();
            firstRunMap.put(username, true);
            checkedMap.put(username, new LinkedList<>());
        }
    }

    public void run() {
        ConfigReader configReader = new ConfigReader();
        Map<String, List<Long>> twitterMap = configReader.getTwitterMap();


        for (Map.Entry<String, List<Long>> entry : twitterMap.entrySet()) {
            ConfigTwitter configTwitter = new ConfigTwitter();

            String twitterUser = entry.getKey();
            if (!firstRunMap.containsKey(twitterUser)) {
                initialise();
            }

            List<Long> channelIDs = entry.getValue();

            if (firstRunMap.get(twitterUser) == true) {
                List<Status> tweets = configTwitter.getMainTweetsOnly(twitterUser);
                for (int i = 0; i < min(initialTweetCheck, tweets.size()); i++) {
                    List<Long> checked = checkedMap.get(twitterUser);
                    checked.add(tweets.get(i).getId());
                    checkedMap.put(twitterUser, checked);
                }
                firstRunMap.put(twitterUser, false);
            }

            Map<String, List<Status>> newTweetsMap = new HashMap<>();


            List<Status> tweets = configTwitter.getMainTweetsOnly(twitterUser);
            for (int i = 0; i < min(tweetCheck, tweets.size()); i++) {
                Status tweet = tweets.get(i);
                if (!checkedMap.get(twitterUser).contains(tweet.getId())) {
                    if (newTweetsMap.get(twitterUser) != null) {
                        newTweetsMap.get(twitterUser).add(tweet);
                    } else {
                        List<Status> list = new LinkedList<>();
                        list.add(tweet);
                        newTweetsMap.put(twitterUser, list);
                    }


                    checkedMap.get(twitterUser).add(tweet.getId());
                }
            }

            if (newTweetsMap.get(twitterUser) != null && !newTweetsMap.get(twitterUser).isEmpty() && !checkedMap.isEmpty()) {
                for (int i = 0; i < newTweetsMap.get(twitterUser).size(); i++) {
                    MessageConstructorTwitter messageConstructorTwitter = new MessageConstructorTwitter(jda);
                    messageConstructorTwitter.sendTweetMessages(tweets.get(i), channelIDs);
                }
            }

        }
    }
}