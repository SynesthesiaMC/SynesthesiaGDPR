package services.synesthesia.synesthesia_gdpr;

import org.bukkit.ChatColor;

public class Utils {
	
    public static String chat(final String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
	
    public static String colorUnColor(String s) {
        s = ChatColor.translateAlternateColorCodes('&', s);
        s = ChatColor.stripColor(s);
        return s;
    }
    
    public static String formatTime(int secs) {
        int remainder = secs % 86400;

        int days 	= secs / 86400;
        int hours 	= remainder / 3600;
        int minutes	= (remainder / 60) - (hours * 60);
        int seconds	= (remainder % 3600) - (minutes * 60);

        String fDays 	= (days > 0 	? " " + days + " day" 		+ (days > 1 ? "s" : "") 	: "");
        String fHours 	= (hours > 0 	? " " + hours + " hour" 	+ (hours > 1 ? "s" : "") 	: "");
        String fMinutes = (minutes > 0 	? " " + minutes + " minute"	+ (minutes > 1 ? "s" : "") 	: "");
        String fSeconds = (seconds > 0 	? " " + seconds + " second"	+ (seconds > 1 ? "s" : "") 	: "");

        return new StringBuilder().append(fDays).append(fHours)
                .append(fMinutes).append(fSeconds).toString();
    }
	
}
