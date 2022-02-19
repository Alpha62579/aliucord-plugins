package com.alphapython.robdaplugins;

import android.content.Context;

import com.aliucord.Logger;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;

@SuppressWarnings("unused")
@AliucordPlugin
public class RobDaPlugins extends Plugin {
    public static Logger logger = new Logger("RobDaPlugins");

    public RobDaPlugins() {
        settingsTab = new SettingsTab(Downloader.class, SettingsTab.Type.PAGE);
    }

    // Dummy plugin, I just need the Settings button.
    @Override
    public void start(Context context) {
    }

    @Override
    public void stop(Context context) {
    }
}
