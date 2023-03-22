package xyz.mauwh.candr.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.mauwh.candr.engine.CopsAndRobbersEngine;
import xyz.mauwh.candr.engine.configuration.EngineSettings;

import java.util.Map;
import java.util.StringJoiner;

@CommandAlias("candr")
@Subcommand("admin|a item")
public class AdminItemCommand extends BaseCommand {

    private final CopsAndRobbersEngine engine;

    public AdminItemCommand(@NotNull CopsAndRobbersEngine engine) {
        this.engine = engine;
    }

    @Subcommand("debug")
    @CommandPermission("copsandrobbers.admin.item.debug")
    @Description("Serializes your currently held item stack and sends it as a copyable message for config use")
    public void onItemDebug(@NotNull Player player) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not currently holding an item");
            return;
        }

        String serializedConfigSafe = getSerializableAsString(heldItem);
        TextComponent component = new TextComponent(serializedConfigSafe);
        component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, serializedConfigSafe));
        player.spigot().sendMessage(component);
    }


    @Subcommand("add")
    @CommandPermission("copsandrobbers.admin.item.add")
    @Syntax("[cops|robbers]")
    @CommandCompletion("cops|robbers")
    @Description("Adds your currently held item stack to the specified list of items")
    public void onAddItem(@NotNull Player player, String listName) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "You are not currently holding an item");
            return;
        }

        EngineSettings settings = engine.getSettings();
        switch(listName.toLowerCase()) {
            case "robbers" -> settings.getRobberItems().add(heldItem.clone());
            case "cops" -> settings.getCopItems().add(heldItem.clone());
            default -> throw new InvalidCommandArgument(true);
        }
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
