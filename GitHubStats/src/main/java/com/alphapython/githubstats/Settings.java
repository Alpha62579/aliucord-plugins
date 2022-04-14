package com.alphapython.githubstats;

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
        addCheckedSetting(ctx, "Show total stars earned", null, "stars");
        addCheckedSetting(ctx, "Show commits", null, "commits");
        // addCheckedSetting(ctx, "Show only this year's commits", null, "yearly");
        addCheckedSetting(ctx, "Show total PRs", null, "prs");
        addCheckedSetting(ctx, "Show total issues", null, "issues");
        addCheckedSetting(ctx, "Show total contributions", null, "contributions");
    }

    private void addCheckedSetting(Context ctx, String title, String subtitle, String setting) {
        var cs = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle);
        cs.setChecked(settings.getBool(setting, true));
        cs.setOnCheckedListener(checked -> settings.setBool(setting, checked));
        addView(cs);
    }
}
