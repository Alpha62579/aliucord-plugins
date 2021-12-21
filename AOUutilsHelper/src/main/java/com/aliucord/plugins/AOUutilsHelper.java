package com.aliucord.plugins;

import android.content.Context;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.Utils;
import com.discord.api.commands.ApplicationCommandType;

import java.util.Arrays;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class AOUutilsHelper extends Plugin {
    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {
        var options = Arrays.asList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "User ID", "The ID of the user to be softbanned.", null, true),
                Utils.createCommandOption(ApplicationCommandType.STRING, "Prefix", "The prefix that you use with the bot. Required if you use something other than the default.", null, false)
        );

        commands.registerCommand(
                "scam",
                "Softban a user due to a nitro scam.",
                options,
                ctx -> {
                    // get argument passed to the world option or fall back to Earth if not specified
                    String id = ctx.getStringOrDefault("User ID", "nothing");
                    String prefix = ctx.getStringOrDefault("Prefix", "aou ");

                    if (id.length() == 18) {
                        try {
                            Long.parseLong(id);
                        } catch (Exception e) {
                            return new CommandsAPI.CommandResult("Invalid ID", null, false);
                        }
                        return new CommandsAPI.CommandResult(String.format("%ssoftban %s Nitro/Steam Scam; Your account may be hacked, please change your password. You may rejoin at <https://discord.gg/S8waxK7QXd> after securing your account.", prefix, id), null, true);
                    } else {
                        return new CommandsAPI.CommandResult("Invalid ID. A user ID is of 18 characters!", null, false);
                    }
                }
        );
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        // Unregisters all commands
        commands.unregisterAll();
    }
}
