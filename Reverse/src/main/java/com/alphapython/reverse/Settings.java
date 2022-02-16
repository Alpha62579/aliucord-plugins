package com.alphapython.reverse;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.widgets.BottomSheet;
import com.discord.views.CheckedSetting;

public class Settings extends BottomSheet {
    private final SettingsAPI settings;

    public Settings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        var ctx = requireContext();
        addCheckedSetting(ctx, "Reverse text", "Toggle this setting to turn all the messages that is sent reversed.", "reverse");
    }

    private void addCheckedSetting(Context ctx, String title, String subtitle, String setting) {
        var cs = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle);
        cs.setChecked(settings.getBool(setting, false));
        cs.setOnCheckedListener(checked -> settings.setBool(setting, checked));
        addView(cs);
    }
}
