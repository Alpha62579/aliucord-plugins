package com.alphapython.massmoderation;

import android.content.Context;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.permission.Permission;
import com.discord.stores.StoreStream;
import com.discord.utilities.permissions.PermissionUtils;

import java.util.Collections;
import java.util.Objects;

@AliucordPlugin
public class MassModeration extends Plugin {
    @Override
    public void start(Context context) {

        // Create command.
        var options = Collections.singletonList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "Query", "The name of the members to search for.", null, true)
        );

        commands.registerCommand(
                "massmod",
                "Mass-ban/kick members",
                options,
                ctx -> {
                    String query = ctx.getStringOrDefault("Query", "");

                    if (query.equals(""))
                        return new CommandsAPI.CommandResult("No query provided...", null, true);

                    // Permission check
                    var roleList = StoreStream.getGuilds().getRoles().get(ctx.getCurrentChannel().getGuildId());
                    var memberMe = StoreStream.getGuilds().getMember(ctx.getCurrentChannel().getGuildId(), ctx.getMe().getId());
                    for (long roleID : memberMe.getRoles()) {
                        if (roleList == null)
                            return new CommandsAPI.CommandResult("An error has occurred. You may be using this command in DM or a Group DM.");
                        var role = roleList.get(roleID);
                        if (role == null)
                            return new CommandsAPI.CommandResult("Something that would never happen just happened.");
                        var perms = role.h();
                        if (PermissionUtils.can(Permission.BAN_MEMBERS, perms) && PermissionUtils.can(Permission.KICK_MEMBERS, perms)) {
                            Utils.openPageWithProxy(ctx.getContext(), new ModPage(Objects.requireNonNull(StoreStream.getGuilds().getMembers().get(ctx.getCurrentChannel().getGuildId())), ctx.getCurrentChannel().getGuildId(), query.toLowerCase()));
                            return new CommandsAPI.CommandResult();
                        }
                        return new CommandsAPI.CommandResult("You do not have the required permissions.", null, true);
                    }
                    return new CommandsAPI.CommandResult();
                }
        );
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
    }
}
