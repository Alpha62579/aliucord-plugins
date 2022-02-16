package com.alphapython.reverse;

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
public class Reverse extends Plugin {
    public static SettingsAPI pluginSettings;
    public static Logger logger = new Logger("Reverse");

    public Reverse() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    private String reverse(String toReverse) {
        StringBuilder buffer = new StringBuilder(toReverse);
        buffer.reverse();
        return buffer.toString();
    }

    private void registerCommands() {
        var options = Collections.singletonList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "text", "The message whose markdown needs to be escaped.", null, true)
        );
        commands.registerCommand(
                "reverse",
                "Reverses the text.",
                options,
                ctx -> {
                    String toReverse = ctx.getStringOrDefault("text", "");
                    if (toReverse.equals("")) {
                        return new CommandsAPI.CommandResult("The message is empty!", null, false);
                    }
                    return new CommandsAPI.CommandResult(reverse(toReverse), null, true);
                }
        );

        commands.registerCommand(
                "togglereverse",
                "Toggles whether all the sent messages must be escaped or not",
                ctx -> {
                    if (pluginSettings.toggleBool("reverse", false)) {
                        return new CommandsAPI.CommandResult("All the messages that are sent from now on will be reversed.", null, false);
                    }
                    return new CommandsAPI.CommandResult("All the messages that are sent from now on will not be reversed.", null, false);
                }
        );
    }

    public void patchSendMessage() {
        try {
            var textField = MessageContent.class.getDeclaredField("textContent");
            textField.setAccessible(true);

            patcher.patch(ChatInputViewModel.class.getDeclaredMethod("sendMessage", Context.class, MessageManager.class, MessageContent.class, List.class, boolean.class, Function1.class), new PreHook(methodHookParam -> {
                if (!pluginSettings.getBool("reverse", false)) return;

                var messageContent = (MessageContent) methodHookParam.args[2];
                if (messageContent == null) return;

                try {
                    var content = (String) textField.get(messageContent);
                    if (content == null) return;
                    textField.set(messageContent, reverse(content));
                } catch (IllegalAccessException e) {
                    logger.error("Failed to either get the message content or to set the reversed text. Reverse patch failed.", e);
                }
            }));
        } catch (NoSuchFieldException e) {
            logger.error("Failed to get textField, reverse patch failed.", e);
        } catch (NoSuchMethodException e) {
            logger.error("Failed to get sendMessage method, reverse patch failed.", e);
        }

    }

    @Override
    public void start(Context context) {
        pluginSettings = settings;
        registerCommands();
        patchSendMessage();
        logger.info("All commands have been registered and patches have been applied.");
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
        patcher.unpatchAll();
        logger.info("All commands have been unregistered and patches have been removed.");
    }
}