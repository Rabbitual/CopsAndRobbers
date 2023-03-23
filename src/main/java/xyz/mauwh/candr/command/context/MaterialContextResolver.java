package xyz.mauwh.candr.command.context;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.PlayerInventory;

public class MaterialContextResolver implements IssuerAwareContextResolver<Material, BukkitCommandExecutionContext> {

    @Override
    public Material getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        BukkitCommandIssuer issuer = context.getIssuer();
        boolean isPlayer = issuer.isPlayer();
        boolean hasArg = context.getFirstArg() != null;
        if (!isPlayer && !hasArg) {
            throw new InvalidCommandArgument(true);
        }

        Material material;
        if (hasArg) {
            String materialName = context.popFirstArg();
            try {
                material = Material.valueOf(materialName);
            } catch (IllegalArgumentException err) {
                issuer.sendMessage(ChatColor.RED + "Invalid material name: '" + materialName + "'");
                throw new InvalidCommandArgument();
            }
        } else {
            PlayerInventory inventory = issuer.getPlayer().getInventory();
            material = inventory.getItemInMainHand().getType();
        }

        if (material.isAir() || !material.isBlock()) {
            throw new InvalidCommandArgument();
        }

        return material;
    }

}
