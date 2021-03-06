package CoreModules;

import Config.ConfigNotifications;
import Config.ConfigReader;
import MessageHandler.MessageConstructor;
import Permissions.PermissionLevel;
import Permissions.PermissionsChecker;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.List;

public class CommandsNotifications extends ListenerAdapter{
    ConfigReader configReader = new ConfigReader();

    private String prefix = configReader.getPrefix();

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContent().split(" ");
        MessageConstructor constructor = new MessageConstructor(event.getJDA());


        if(!command[0].startsWith(prefix)){
            return;
        }

        if(command[0].equalsIgnoreCase(prefix + "notifyMe")){
            ConfigNotifications configNotifications = new ConfigNotifications();
            Long channelID = event.getChannel().getIdLong();

            if(command.length > 1) {
                String agency = command[1].toLowerCase();
                if (configNotifications.addUserNotifications(agency, event.getMember().getUser())) {
                    constructor.setAuthor("You will be notified of future launches!");
                    constructor.setSuccessColor();
                } else {
                    constructor.setAuthor("You are already registered to receive notifications or agency isn't " +
                            "supported!");
                    constructor.setFailColor();
                }
            }else{
                constructor.setAuthor("You must supply an agency or 'all' arguments");
                constructor.appendDescription("Available agencies: " + configNotifications.agencyLSP.keySet());
                constructor.setFailColor();
            }
            constructor.sendMessage(channelID);
        }

        if(command[0].equalsIgnoreCase(prefix + "notifyStop")){
            ConfigNotifications configNotifications = new ConfigNotifications();
            Long channelID = event.getChannel().getIdLong();

            if(command.length > 1){
                String agency = command[1].toLowerCase();
                if(configNotifications.removeUserNotifications(agency, event.getMember().getUser())){
                    constructor.setAuthor("You will no longer be notified!");
                    constructor.setSuccessColor();
                }else {
                    constructor.setAuthor("You are not registered to receive notifications for this agency or this " +
                            "agency isn't supported");
                    constructor.setFailColor();
                }
            }else{
                constructor.setAuthor("You must supply an agency or 'all' arguments");
                constructor.setFailColor();
            }
            constructor.sendMessage(channelID);
        }

        if(command[0].equalsIgnoreCase(prefix + "getNotifs")){
            ConfigNotifications configNotifications = new ConfigNotifications();
            String notifs = configNotifications.getUserNotification(event.getMember().getUser());
            constructor.setAuthor("Currently subscribed to: " + notifs);
            constructor.sendMessage(event.getChannel().getIdLong());
        }

        if(command[0].equalsIgnoreCase(prefix + "getAgencies")){
            ConfigNotifications configNotifications = new ConfigNotifications();
            constructor.setAuthor("Available agencies: " + configNotifications.agencyLSP.keySet().toString());
            constructor.sendMessage(event.getChannel().getIdLong());
        }

//        if(command[0].equalsIgnoreCase(prefix + "notifymeall")){
//            ConfigNotifications configNotifications = new ConfigNotifications();
//            Long channelID = event.getChannel().getIdLong();
//            if(configNotifications.addUserNotifications("all", event.getMember().getUser())){
//                constructor.setAuthor("You will be notified of future launches!");
//                constructor.setSuccessColor();
//            }else{
//                constructor.setAuthor("You are already registered to receive notifications!");
//                constructor.setFailColor();
//            }
//            constructor.sendMessage(channelID);
//        }
//
//        if(command[0].equalsIgnoreCase(prefix + "stopNotifyAll")){
//            ConfigNotifications configNotifications = new ConfigNotifications();
//            Long channelID = event.getChannel().getIdLong();
//            if(configNotifications.removeUserNotifications("all", event.getMember().getUser())){
//                constructor.setAuthor("You will no longer be notified");
//                constructor.setSuccessColor();
//            }else{
//                constructor.setAuthor("You are not registered to receive notifications!");
//                constructor.setFailColor();
//            }
//            constructor.sendMessage(channelID);
//        }
    }
}
