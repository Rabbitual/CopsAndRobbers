package xyz.mauwh.message;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageHandler {

    private final BukkitAudiences audiences;
    private final MiniMessage miniMessage;
    private final Map<Message, String> messages;
    private final Map<String, Integer> intToString;
    private final Pattern placeholderPattern = Pattern.compile("\\{(\\d+)}+?");

    public MessageHandler(@NotNull BukkitAudiences audiences, @NotNull MiniMessage miniMessage) {
        this.audiences = audiences;
        this.miniMessage = miniMessage;
        this.messages = new HashMap<>();
        this.intToString = new HashMap<>();
    }

    @NotNull
    public BukkitAudiences getAudiences() {
        return audiences;
    }

    /**
     * Loads all configured messages supported by the {@link xyz.mauwh.message.Message} enum
     * @param configuration - the configuration to load messages from
     */
    public void loadMessages(@NotNull YamlConfiguration configuration) {
        for (Message message : Message.values()) {
            String value = configuration.getString(message.getPath());
            if (value != null) {
                messages.put(message, value);
            }
        }
    }

    /**
     * Gets a configured message by its enum counterpart, and formats any additional args into it
     * @param message - the configured message to get
     * @param prefixed - whether to append the plugin prefix before the message
     * @param args - any additional args for formatting
     * @return the fully formatted component message
     */
    @NotNull
    public Component getMessage(@NotNull Message message, boolean prefixed, Object... args) {
        String messageValue = messages.get(message);
        Matcher matcher = placeholderPattern.matcher(messageValue);
        String replaced = matcher.replaceAll(matchResult -> {
            String strReplacementIndex = matchResult.group(1);
            int replacementIndex = intToString.computeIfAbsent(strReplacementIndex, Integer::parseInt);
            if (args.length <= replacementIndex) {
                return matchResult.group(0);
            }
            return args[replacementIndex].toString();
        });

        Component result = miniMessage.deserialize(replaced);
        if (prefixed) {
            result = Component.text().append(getMessage(Message.PREFIX, false)).append(result).build();
        }
        return result;
    }

}
