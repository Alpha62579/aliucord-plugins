package com.alphapython.robdaplugins;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import com.aliucord.Constants;
import com.aliucord.Http;
import com.aliucord.PluginManager;
import com.aliucord.Utils;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.views.Button;
import com.aliucord.views.DangerButton;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public class Downloader extends SettingsPage {
    public Map<String, Object> plugins;

    public Downloader() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        setActionBarTitle("Download Robin's plugins.");

        var ctx = requireContext();

        var thread = new Thread(() -> {
            try {
                plugins = Http.simpleJsonGet("https://raw.githubusercontent.com/Alpha62579/aliucord-plugins/builds/updater.json", TypeToken.getParameterized(Map.class, String.class, Object.class).getType());
            } catch (IOException e) {
                Utils.showToast("Failed to get updater json...");
            }
        });

        thread.start();
        while (thread.isAlive()) {
            thread.getId();
        }

        plugins.forEach((name, info) ->
                DealWithMap(ctx, name, info, this::reRender)  // Android Studio was yelling at me that I couldn't just put code here so I made a function.
        );
    }

    public void DealWithMap(Context ctx, String name, Object info, Runnable callback) {
        RobDaPlugins.logger.info(String.valueOf(info));
        var plugin = new File(String.format("%s/%s.zip", Constants.PLUGINS_PATH, name));
        var isInstalled = plugin.exists();
        var button = isInstalled ? new DangerButton(ctx) : new Button(ctx);
        button.setText(String.format("%s%s", isInstalled ? "Uninstall " : "Install ", name));
        if (!isInstalled) {
            button.setOnClickListener(action -> Utils.threadPool.execute(() -> {
                try {
                    var response = new Http.Request(String.format("https://raw.githubusercontent.com/Alpha62579/aliucord-plugins/builds/%s.zip", name)).execute();
                    response.saveToFile(plugin);
                    PluginManager.loadPlugin(Utils.getAppContext(), plugin);
                    PluginManager.startPlugin(name);
                    Utils.showToast("Installed " + name);
                    if (Objects.requireNonNull(PluginManager.plugins.get(name)).requiresRestart()) {
                        Utils.promptRestart();
                    }
                    Utils.mainThread.post(callback);
                } catch (IOException e) {
                    RobDaPlugins.logger.error("An error has occurred while downloading the plugin.", e);
                }
            }));
        } else {
            button.setOnClickListener(action -> {
                var deleted = plugin.delete();
                Utils.showToast(deleted ? "Uninstalled  " + name : "Error when uninstalling plugin...");
                if (deleted) {
                    var thePlugin = PluginManager.plugins.get(name);
                    PluginManager.stopPlugin(name);
                    PluginManager.unloadPlugin(name);
                    assert thePlugin != null; // This will probably not happen. This is here so that Android Studio doesn't yell at me.
                    if (thePlugin.requiresRestart()) Utils.promptRestart();
                    Utils.mainThread.post(callback);
                }
            });
        }
        addView(button);
    }
}