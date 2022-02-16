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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
@AliucordPlugin
public class Icheck extends Plugin {
    public static SettingsAPI pluginSettings;
    public static Logger logger = new Logger("Icheck");
    public static HashMap<String, ArrayList<String>> text = new HashMap<>() {{
        put("i", new ArrayList<>() {{
            add("911357751547023391");
            add("I forgot to put an I, but ");
        }});
        put("a", new ArrayList<>() {{
            add("929774476713938964");
            add("accidentally forgot to start my message with an \"a\", but anyways ");
        }});
        put("b", new ArrayList<>() {{
            add("934903182797176882");
            add("bruh, I didn't add a b, anyways ");
        }});
        put("h", new ArrayList<>() {{
            add("934547364797120614");
            add("hi, I forgot to add a h, anyways ");
        }});
        put("e", new ArrayList<>() {{
            add("935759398310129684");
            add("electro pog, anyways ");
        }});
       put("s", new ArrayList<>() {{
            add("938515745325850674");
            add("sucks to me be because i forgot the 's', anyways ");
        }});
       put("r", new ArrayList<>() {{
            add("942503083886411827");
            add("r/therewasanattempttosendamessagethatdidnotstartwithr, anyways ");
        }});
    }};

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
                var channel = "";
                for (String key :
                        pluginSettings.getAllKeys()) {
                    if (key.replace("Channel", "").length() == 1 && pluginSettings.getString(key, "Electrowo, why?").equals(String.valueOf(StoreStream.getChannelsSelected().getId())) && key.contains("Channel")) {
                        channel = key.replace("Channel", "");
                        break;
                    }
                }
                if (channel.equals("") || !pluginSettings.getBool("check" + channel.toUpperCase(), false))
                    return;
                var messageContent = (MessageContent) methodHookParam.args[2];
                if (messageContent == null) return;
                try {
                    var content = (String) textField.get(messageContent);
                    if (content == null) return;
                    if (!content.toLowerCase().startsWith(channel)) {
                        textField.set(messageContent, text.get(channel).get(1) + content);
                    }
                } catch (IllegalAccessException e) {
                    logger.error("Unable to get content from textField. Icheck patch failed.", e);
                } catch (NullPointerException e) {
                    logger.error("Failed to get message from Array. This is odd...", e);
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
