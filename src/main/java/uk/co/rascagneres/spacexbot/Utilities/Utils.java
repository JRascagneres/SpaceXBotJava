package uk.co.rascagneres.spacexbot.Utilities;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.SubScene;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.co.rascagneres.spacexbot.Config.ConfigReader;
import uk.co.rascagneres.spacexbot.Config.PermissionLevel;
import uk.co.rascagneres.spacexbot.LaunchData.Launch;
import uk.co.rascagneres.spacexbot.LaunchData.LaunchLibrary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static javafx.scene.input.KeyCode.L;

public class Utils {
    private static String getText(String url) throws Exception{
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        return response.toString();
    }

    public static LaunchLibrary getLaunches(int amount) {
        try {
            LaunchLibrary launchLibrary = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(getText("https://launchlibrary.net/1.3/launch/next/" + amount), LaunchLibrary.class);
            for(int i = 0; i < launchLibrary.launches.size(); i++){
                String status = "";
                switch (launchLibrary.launches.get(i).status){
                    case 1:
                        status = "Green";
                        break;
                    case 2:
                        status = "Red";
                        break;
                    case 3:
                        status = "Success";
                        break;
                    case 4:
                        status = "Failed";
                        break;
                }
                launchLibrary.launches.get(i).statusText = status;
                launchLibrary.launches.get(i).padName = launchLibrary.launches.get(i).location.pads.get(0).name;
            }
            return launchLibrary;
        } catch (Exception e) {
            System.out.println("ERROR GETTING LAUNCHES");
            return null;
        }
    }

    public static Launch getNextLaunch(){
        try {
            int launches = 1;
            Launch currentLaunch = getLaunches(launches).launches.get(launches - 1);
            while (currentLaunch.status != 1) {
                launches++;
                currentLaunch = getLaunches(launches).launches.get(launches - 1);
            }
            return currentLaunch;
        }catch (Exception e){
            System.out.println("ERROR GETTING LAUNCHES");
            return null;
        }
    }

    public static PermissionLevel PermissionResolver(Member member, Channel channel){

        if(member.getUser().getId().equals(String.valueOf(150768477152477186L))){
            return PermissionLevel.BotOwner;
        }

        if(member.getUser().equals(channel.getGuild().getOwner().getUser())){
            return PermissionLevel.ServerOwner;
        }

        List<Permission> serverPerms = member.getPermissions(channel);

        if(serverPerms.contains(Permission.MANAGE_ROLES)){
            return PermissionLevel.ServerAdmin;
        }
        if(serverPerms.contains(Permission.MESSAGE_MANAGE) && serverPerms.contains(Permission.KICK_MEMBERS) && serverPerms.contains(Permission.BAN_MEMBERS)){
            return PermissionLevel.ServerModerator;
        }

        if(member.getRoles().stream().map(Role::getName).anyMatch(name -> name.equalsIgnoreCase("Bot Manager"))){
            return PermissionLevel.BotManager;
        }

        return PermissionLevel.User;
    }

    public static boolean checkSubredditExists(String subreddit){
        RedditClient redditClient = getRedditClient();
        try {
            redditClient.subreddit(subreddit);
        }catch (Exception ex){
            return false;
        }
        return true;
    }

    public static RedditClient getRedditClient(){
        UserAgent userAgent = new UserAgent("desktop", "uk.co.rascagneres.spacexbot", "v1.0", "Scorp1579");
        NetworkAdapter adapter = new OkHttpNetworkAdapter(userAgent);
        Map<String, String> redditData = new ConfigReader().getRedditData();
        Credentials credentials = Credentials.script(redditData.get("username"), redditData.get("password"), redditData.get("clientid"), redditData.get("clientsecret"));
        RedditClient redditClient = OAuthHelper.automatic(adapter, credentials);
        redditClient.setLogHttp(false);
        return redditClient;
    }

    public static List<Submission> getRedditPosts(String subreddit, int numberOfPosts){
        DefaultPaginator<Submission> paginator = getRedditClient().subreddit(subreddit).posts()
                .limit(numberOfPosts)
                .sorting(SubredditSort.NEW)
                .build();
        return paginator.next();
    }

    public static String getRedditTitle(String subreddit){
        return getRedditClient().subreddit(subreddit).about().getTitle();
    }

    public static Twitter getTwitter(){
        ConfigReader configReader = new ConfigReader();
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder
                .setDebugEnabled(true)
                .setOAuthConsumerKey(configReader.getTwitterData().get("consumerkey"))
                .setOAuthConsumerSecret(configReader.getTwitterData().get("consumersecret"))
                .setOAuthAccessToken(configReader.getTwitterData().get("accesstoken"))
                .setOAuthAccessTokenSecret(configReader.getTwitterData().get("accesstokensecret"))
                .setTweetModeExtended(true);

        TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
        Twitter twitter = twitterFactory.getInstance();
        return twitter;
    }

    public static boolean checkTwitterExists(String twitterUser){
        Twitter twitter = getTwitter();
        boolean userExists = true;
        try {
            twitter.showUser(twitterUser);
        }catch (Exception e){
            userExists = false;
        }

        return userExists;
    }

    public static String getTwitterName(String twitterUser){
        Twitter twitter = getTwitter();
        try {
            return twitter.users().showUser(twitterUser).getName();
        }catch (Exception e){
            return null;
        }
    }

    public static List<String> getTimeToLaunchData(Launch thisLaunch){
        Date date = new Date();
        Instant now = ZonedDateTime.now(ZoneOffset.UTC).toInstant();
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = simpleDateFormat.parse(thisLaunch.net);
        }catch (Exception e){
            e.printStackTrace();
        }
        Duration timeToLaunch = Duration.between(now, date.toInstant());
        Long days = timeToLaunch.toDays();
        Long hours = timeToLaunch.toHours() - days * 24;
        Long minutes = timeToLaunch.toMinutes() - days * 24 * 60 - hours * 60;

        List<String> timeToLaunchList = new LinkedList<>();
        timeToLaunchList.add(String.valueOf(days));
        timeToLaunchList.add(String.valueOf(hours));
        timeToLaunchList.add(String.valueOf(minutes));
        timeToLaunchList.add(date.toString());

        return timeToLaunchList;
    }
}
