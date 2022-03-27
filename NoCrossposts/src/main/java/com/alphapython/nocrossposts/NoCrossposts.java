package com.alphapython.nocrossposts;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.MessageEmbedBuilder;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.ReflectUtils;
import com.discord.databinding.WidgetChatListBinding;
import com.discord.models.domain.ModelMessageDelete;
import com.discord.stores.StoreStream;
import com.discord.widgets.chat.MessageContent;
import com.discord.widgets.chat.MessageManager;
import com.discord.widgets.chat.input.ChatInputViewModel;
import com.discord.widgets.chat.list.WidgetChatList;
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage;
import com.discord.widgets.chat.list.entries.ChatListEntry;
import com.discord.widgets.chat.list.entries.MessageEntry;

import java.util.List;
import java.util.Objects;

import kotlin.jvm.functions.Function1;

@SuppressWarnings("unused")
@AliucordPlugin
public class NoCrossposts extends Plugin {
    public static SettingsAPI pluginSettings;

    public NoCrossposts() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @Override
    public void start(Context context) {
        pluginSettings = settings;
        patchCrossposts();
    }

    private void patchCrossposts() {
        try
        {
            patcher.patch(WidgetChatListAdapterItemMessage.class.getDeclaredMethod("onConfigure", int.class, ChatListEntry.class), new Hook(methodHookParam -> {
                var entry = (MessageEntry) methodHookParam.args[1];
                if (entry.getMessage().isLoading()) return;
                if (entry.getMessage().isCrosspost() && settings.getBool("removeCrossposts", false)) StoreStream.getMessages().handleMessageDelete(new ModelMessageDelete(entry.getMessage().getChannelId(), entry.getMessage().getId()));
            }));
        }
        catch (NoSuchMethodException e)
        {
            logger.error("Failed to patch message", e);
        }
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
