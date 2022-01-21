package com.alphapython.rawify;

import android.content.Context;

import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.MessageManager;
import com.discord.widgets.chat.input.ChatInputViewModel;

import java.util.Collections;
import java.util.List;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
@AliucordPlugin
public class Rawify extends Plugin {
    public static SettingsAPI pluginSettings;
    public static Logger logger = new Logger("Rawify");

    public Rawify() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    private String rawify(String toEscape) {
        toEscape = toEscape.replace("\\\\", "\\\\");
        toEscape = toEscape.replace("`", "\\`");
        toEscape = toEscape.replace("*", "\\*");
        toEscape = toEscape.replace("_", "\\_");
        toEscape = toEscape.replace("~", "\\~");
        toEscape = toEscape.replace("|", "\\|");
        toEscape = toEscape.replace(">", "\\>");
        return toEscape;
    }

    @Override
    public void start(Context context) throws NoSuchFieldException {
        pluginSettings = settings;

        var options = Collections.singletonList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "Message", "The message whose markdown needs to be escaped.", null, true)
        );

        commands.registerCommand(
                "rawify",
                "Escapes the markdown in the message and sends it.",
                options,
                ctx -> {
                    String toEscape = ctx.getStringOrDefault("Message", "");

                    if (toEscape.equals("")) {
                        return new CommandsAPI.CommandResult("The message is empty!", null, false);
                    }
                    
                    String escaped = rawify(toEscape);
                    return new CommandsAPI.CommandResult(escaped, null, true);
                }
        );

        commands.registerCommand(
                "togglerawify",
                "Toggles whether all the sent messages must be escaped or not.",
                ctx -> {
                    pluginSettings.setBool("rawify", !pluginSettings.getBool("rawify", true));
                    if (pluginSettings.getBool("rawify", true)) {
                        return new CommandsAPI.CommandResult("All the messages that are sent from now on will be escaped!", null, false);
                    } else {
                        return new CommandsAPI.CommandResult("All the messages that are sent from now on will NOT be escaped!", null, false);
                    }
                }
        );

        try {
            var textfield = MessageContent.class.getDeclaredField("textContent");
            textfield.setAccessible(true);

            patcher.patch(ChatInputViewModel.class.getDeclaredMethod("sendMessage", Context.class, MessageManager.class, MessageContent.class, List.class, boolean.class, Function1.class), new PreHook(methodHookParam -> {
                if (!pluginSettings.getBool("rawify", true)) {
                    return;
                }
                var messageContent = (MessageContent) methodHookParam.args[2];
                if (messageContent == null) {
                    return;
                }
                try {
                    var content = (String) textfield.get(messageContent);
                    if (content == null) {
                        return;
                    }
                    content = rawify(content);
                    textfield.set(messageContent, content);
                } catch (IllegalAccessException e) {
                    logger.error("So Discord doesn't want me to get the damn content in the text field.", e);
                }
            }));
        } catch (NoSuchFieldException e) {
            logger.error("Couldn't patch message text field!", e);
        } catch (NoSuchMethodException e) {
            logger.error("Couldn't patch sendMessage function!", e);
        } catch (Exception e) {
            logger.error("Something went wrong while patching!", e);
        }
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
        patcher.unpatchAll();
    }
}
