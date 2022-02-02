package com.alphapython.icheck;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.aliucord.Utils;
import com.aliucord.api.SettingsAPI;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.TextInput;
import com.aliucord.widgets.BottomSheet;
import com.discord.views.CheckedSetting;

import java.util.concurrent.atomic.AtomicInteger;

import kotlin.jvm.functions.Function1;

public class Settings extends BottomSheet {
    private final SettingsAPI settings;

    public Settings(SettingsAPI settings) {
        this.settings = settings;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        var ctx = requireContext();
        AtomicInteger id = new AtomicInteger(1);
        Icheck.text.forEach((key, array) -> {
            id.set(1);
            addCheckedSetting(
                    ctx,
                    key.toUpperCase() + " check",
                    String.format(
                            "Checks if the message starts with %s in %s %s channel before sending it.",
                            key.toUpperCase(),
                            key.matches("[aeiou]") ? "an" : "a",
                            key.toUpperCase()),
                    "check" + key.toUpperCase()
            );
            try {
                Long.parseLong(array.get(id.get()));
            } catch (NumberFormatException e) {
                id.set(0);
            }
            addInput(
                    ctx,
                    key.toUpperCase() + " Channel ID",
                    key + "Channel",
                    array.get(id.get()),
                    false,
                    input -> {
                        if (input.length() == 18) {
                            try {
                                Long.parseLong(input);
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        }
                        return input == null || input.equals("");
                    }
            );
        });
    }

    private void addCheckedSetting(Context ctx, String title, String subtitle, String setting) {
        var cs = Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle);
        cs.setChecked(settings.getBool(setting, false));
        cs.setOnCheckedListener(checked -> settings.setBool(setting, checked));
        addView(cs);
    }

    private void addInput(Context ctx, String title, String setting, String def, boolean isInt, Function1<String, Boolean> validate) {
        var input = new TextInput(ctx);
        var params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DimenUtils.getDefaultPadding(), DimenUtils.getDefaultPadding() / 2, DimenUtils.getDefaultPadding(), DimenUtils.getDefaultPadding() / 2);
        input.setLayoutParams(params);
        input.setHint(title);
        var editText = input.getEditText();
        if (settings.getString(setting, "nope").equals("nope")) {
            if (isInt) {
                settings.setInt(setting, Integer.parseInt(def));
            } else {
                settings.setString(setting, def);
            }
        }
        editText.setText(isInt ? Integer.toString(settings.getInt(setting, Integer.parseInt(def))) : settings.getString(setting, def));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable e) {
                var s = e.toString();
                if (!validate.invoke(s)) input.setHint(title + " [INVALID]");
                else {
                    if (isInt) {
                        settings.setInt(setting, Integer.parseInt(s));
                    } else {
                        settings.setString(setting, s);
                    }
                    input.setHint(title);
                }
            }
        });
        addView(input);
    }
}
