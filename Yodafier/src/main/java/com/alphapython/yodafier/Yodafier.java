package com.alphapython.yodafier;

import android.content.Context;
import android.os.NetworkOnMainThreadException;

import com.aliucord.Http;
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
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
@AliucordPlugin
public class Yodafier extends Plugin {
    public static SettingsAPI pluginSettings;
    public static Logger logger = new Logger("Yodafier");

    public Yodafier() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    private String yodaify(String toYoda) {
        AtomicReference<Map<String, String>> data = new AtomicReference<>();
        var thread = new Thread(() -> {
            try {
                data.set(Http.simpleJsonGet(
                        String.format("https://eu-gb.functions.appdomain.cloud/api/v1/web/744fbaa5-8b07-4b92-b30d-8b6b22960a0a/default/Yodafier.json?text=%s", URLEncoder.encode(toYoda, "UTF-8")),
                        TypeToken.getParameterized(Map.class, String.class, String.class).getType()
                ));
            } catch (Exception e) {
                logger.error(e);
            }
        });
        try {
            thread.start();
            while (thread.isAlive()) thread.getId();
        } catch (NetworkOnMainThreadException e) {
            return "";
        }
        logger.info(String.valueOf(data.get()));
        return data.get().get("result");
    }

    private void registerCommands() {
        var options = Collections.singletonList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "text", "The text to be converted to yoda speak.", null, true)
        );
        commands.registerCommand(
                "yodafier",
                "Converts the text to Yoda speak.",
                options,
                ctx -> {
                    String toYoda = ctx.getStringOrDefault("text", "");
                    if (toYoda.equals("")) {
                        return new CommandsAPI.CommandResult("The message is empty!", null, false);
                    }
                    return new CommandsAPI.CommandResult(yodaify(toYoda), null, true);
                }
        );

        commands.registerCommand(
                "toggleyodaify",
                "Toggles whether all the sent messages must be converted to yoda speak.",
                ctx -> {
                    if (pluginSettings.toggleBool("yodafier", false)) {
                        return new CommandsAPI.CommandResult("All the messages that are sent from now on will be converted to yoda speak.", null, false);
                    }
                    return new CommandsAPI.CommandResult("All the messages that are sent from now on will not be converted to yoda speak.", null, false);
                }
        );
    }

    public void patchSendMessage() {
        try {
            var textField = MessageContent.class.getDeclaredField("textContent");
            textField.setAccessible(true);

            patcher.patch(ChatInputViewModel.class.getDeclaredMethod("sendMessage", Context.class, MessageManager.class, MessageContent.class, List.class, boolean.class, Function1.class), new PreHook(methodHookParam -> {
                if (!pluginSettings.getBool("yodafier", false)) return;

                var messageContent = (MessageContent) methodHookParam.args[2];
                if (messageContent == null) return;

                try {
                    var content = (String) textField.get(messageContent);
                    if (content == null) return;
                    textField.set(messageContent, yodaify(content));
                } catch (IllegalAccessException e) {
                    logger.error("Failed to either get the message content or to convert to Yoda speak. Yodafier patch failed.", e);
                }
            }));
        } catch (NoSuchFieldException e) {
            logger.error("Failed to get textField, Yodafier patch failed.", e);
        } catch (NoSuchMethodException e) {
            logger.error("Failed to get sendMessage method, Yodafier patch failed.", e);
        }

    }

    @Override
    public void start(Context context) {
        pluginSettings = settings;
        registerCommands();
        patchSendMessage();
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
        patcher.unpatchAll();
    }
}