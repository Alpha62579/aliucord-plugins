package com.alphapython.enchanter;

import android.content.Context;
import android.widget.TextView;

import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.PreHook;

import java.util.Hashtable;
import java.util.Objects;

@SuppressWarnings("unused")
@AliucordPlugin
public class Enchanter extends Plugin {
    public static Hashtable<String, String> mappings = new Hashtable<>() {{
        put("A", "ᔑ");
        put("B", "ʖ");
        put("C", "ᓵ");
        put("D", "↸");
        put("E", "ᒷ");
        put("F", "⎓");
        put("G", "⊣");
        put("H", "⍑");
        put("I", "╎");
        put("J", "⋮");
        put("K", "ꖌ");
        put("L", "ꖎ");
        put("M", "ᒲ");
        put("N", "リ");
        put("O", "𝙹");
        put("P", "!¡");
        put("Q", "ᑑ");
        put("R", "∷");
        put("S", "ᓭ");
        put("T", "ℸ ̣");
        put("U", "⚍");
        put("V", "⍊");
        put("W", "∴");
        put("X", "̇/");
        put("Y", "||");
        put("Z", "⨅");
    }};

    private String enchant(String toEnchant) {
        toEnchant = toEnchant.toUpperCase();
        for (char ch :
                toEnchant.toCharArray()) {
            toEnchant = toEnchant.replace(String.valueOf(ch), Objects.requireNonNull(mappings.getOrDefault(String.valueOf(ch), String.valueOf(ch))));
        }
        return toEnchant;
    }

    @Override
    public void start(Context context) throws NoSuchMethodException {
        patcher.patch(TextView.class.getDeclaredMethod("setText", CharSequence.class), new PreHook(hook -> {
            String text;
            try {
                text = (String) hook.args[0];
            } catch (ClassCastException e) {
                text = hook.args[0].toString();
            }
            if (text != null) hook.args[0] = enchant(text);
        }));
        patcher.patch(TextView.class.getDeclaredMethod("setText", CharSequence.class, TextView.BufferType.class), new PreHook(hook -> {
            String text;
            try {
                text = (String) hook.args[0];
            } catch (ClassCastException e) {
                text = hook.args[0].toString();
            }
            if (text != null) hook.args[0] = enchant(text);
        }));
        patcher.patch(TextView.class.getDeclaredMethod("setHint", CharSequence.class), new PreHook(hook -> {
            String text;
            try {
                text = (String) hook.args[0];
            } catch (ClassCastException e) {
                text = hook.args[0].toString();
            }
            if (text != null) hook.args[0] = enchant(text);
        }));
    }

    @Override
    public boolean requiresRestart() {
        return true;
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }
}
