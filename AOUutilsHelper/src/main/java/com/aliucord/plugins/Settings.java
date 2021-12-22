package com.aliucord.plugins;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.aliucord.api.SettingsAPI;
import com.aliucord.utils.DimenUtils;
import com.aliucord.views.TextInput;
import com.aliucord.widgets.BottomSheet;

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
        addInput(ctx, "Your prefix for AOUutils.", "prefix", "aou ", false, input -> input != null && !input.equals(""));

    }

    private void addInput(Context ctx, String title, String setting, String def, boolean isInt, Function1<String, Boolean> validate) {
        var input = new TextInput(ctx);
        var params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DimenUtils.getDefaultPadding(), DimenUtils.getDefaultPadding() / 2, DimenUtils.getDefaultPadding(), DimenUtils.getDefaultPadding() / 2);
        input.setLayoutParams(params);
        input.setHint(title);
        var editText = input.getEditText();
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
