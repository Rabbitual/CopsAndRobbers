package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.StringJoiner;

@CommandAlias("candr")
@Subcommand("admin|a")
public class AdminItemDebugCommand extends BaseCommand {

    @Subcommand("itemdebug|item")
    @CommandPermission("candr.admin.itemdebug")
    @Description("Serializes your currently held item stack and sends it as a copyable message for config use")
    public boolean onItemDebug(@NotNull Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not currently holding an item");
            return true;
        }

        String serializedConfigSafe = getSerializableAsString(heldItem);
        TextComponent component = new TextComponent(serializedConfigSafe);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, serializedConfigSafe));
        player.spigot().sendMessage(component);
        return true;
    }

    @NotNull
    private String getSerializableAsString(ConfigurationSerializable serializable) {
        StringJoiner joiner = new StringJoiner("\n");
        appendSerializableAsString(joiner, serializable, 0);
        return joiner.toString();
    }

    private void appendSerializableAsString(StringJoiner joiner, ConfigurationSerializable serializable, int indentLevel) {
        String indent = " ".repeat(Math.max(0, indentLevel));
        joiner.add(indent + "==: " + ConfigurationSerialization.getAlias(serializable.getClass()));
        appendSerializableAsString(joiner, serializable.serialize(), indentLevel);
    }

    private void appendSerializableAsString(StringJoiner joiner, Map<?, ?> serialized, int indentLevel) {
        String indent = " ".repeat(Math.max(0, indentLevel));
        serialized.forEach((key, value) -> {
            if (value instanceof ConfigurationSerializable) {
                joiner.add(indent + key + ":");
                appendSerializableAsString(joiner, (ConfigurationSerializable)value, indentLevel + 2);
            } else if (value instanceof Map<?, ?>) {
                joiner.add(indent + key + ":");
                ((Map<?, ?>)value).forEach((key1, value1) -> joiner.add(indent + "  " + key1 + ": " + value1));
            } else {
                joiner.add(indent + key + ": " + value);
            }
        });
    }

}
