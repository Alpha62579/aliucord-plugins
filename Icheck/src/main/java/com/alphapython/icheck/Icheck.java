package com.alphapython.icheck;

import android.content.Context;

import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;
import com.discord.stores.StoreStream;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.MessageManager;
import com.discord.widgets.chat.input.ChatInputViewModel;

import java.util.List;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
@AliucordPlugin
public class Icheck extends Plugin {
    public static SettingsAPI pluginSettings;
    public static Logger logger = new Logger("Icheck");

    public Icheck() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @Override
    public void start(Context context) {
        pluginSettings = settings;
        addIPatch();
    }

    private void addIPatch() {
        try {
            var textField = MessageContent.class.getDeclaredField("textContent");
            textField.setAccessible(true);

            patcher.patch(ChatInputViewModel.class.getDeclaredMethod("sendMessage", Context.class, MessageManager.class, MessageContent.class, List.class, boolean.class, Function1.class), new PreHook(methodHookParam -> {
                if (!pluginSettings.getBool("checkI", false) || !pluginSettings.getString("iChannel", "nani wrong channel!").equals(String.valueOf(StoreStream.getChannelsSelected().getId())))
                    return;
                var messageContent = (MessageContent) methodHookParam.args[2];
                if (messageContent == null) return;
                try {
                    var content = (String) textField.get(messageContent);
                    if (content == null) return;
                    if (!content.toLowerCase().startsWith("i")) {
                        textField.set(messageContent, "I forgot to add an I, but " + content);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Unable to get content from textField. Icheck patch failed.", e);
                }
            }));
        } catch (NoSuchFieldException e) {
            logger.error("Couldn't get textField. Icheck patch failed.", e);
        } catch (NoSuchMethodException e) {
            logger.error("Failed to patch sendMessage function. Icheck patch failed.", e);
        } catch (Exception e) {
            logger.error("An unknown error has occurred.", e);
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
