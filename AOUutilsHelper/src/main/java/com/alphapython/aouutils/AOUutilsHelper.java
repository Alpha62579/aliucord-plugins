package com.alphapython.aouutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.CommandsAPI;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.discord.api.commands.ApplicationCommandType;
import com.discord.api.permission.Permission;
import com.discord.api.role.GuildRole;
import com.discord.stores.StoreStream;
import com.discord.utilities.permissions.PermissionUtils;
import com.discord.widgets.chat.input.AppFlexInputViewModel;
import com.discord.widgets.chat.list.actions.WidgetChatListActions;
import com.lytefast.flexinput.R;
import com.lytefast.flexinput.fragment.FlexInputFragment$c;
import com.lytefast.flexinput.widget.FlexEditText;

import java.util.Arrays;

import c.b.a.e.a;

// This class is never used so your IDE will likely complain. Let's make it shut up!
@SuppressWarnings("unused")
@AliucordPlugin
public class AOUutilsHelper extends Plugin {
    public static SettingsAPI pluginsettings;
    private static FlexEditText textInput;
    private static AppFlexInputViewModel textBox;
    private final int viewID = View.generateViewId();

    public AOUutilsHelper() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @Override
    // Called when your plugin is started. This is the place to register command, add patches, etc
    public void start(Context context) {
        pluginsettings = settings;
        patchContext();
        registerCommands();
    }

    @SuppressLint("SetTextI18n")
    private void patchContext() {
        try {
            patcher.patch(FlexEditText.class.getDeclaredMethod("onCreateInputConnection", EditorInfo.class), new Hook(methodHookParam -> {
                textInput = (FlexEditText) methodHookParam.thisObject;
            }));

            patcher.patch(FlexInputFragment$c.class.getDeclaredMethod("invoke", Object.class), new Hook(methodHookParam -> {
                textInput = ((a) methodHookParam.getResult()).getRoot().findViewById(R.f.text_input);
            }));

            patcher.patch(WidgetChatListActions.class, "configureUI", new Class<?>[]{WidgetChatListActions.Model.class}, new Hook(methodHookParam -> {
                var _this = (WidgetChatListActions) methodHookParam.thisObject;
                var ogView = (NestedScrollView) _this.requireView();
                var layout = (LinearLayout) ogView.getChildAt(0);
                if (layout == null || layout.findViewById(viewID) != null) return;
                var ctx = layout.getContext();
                var msg = ((WidgetChatListActions.Model) methodHookParam.args[0]).getMessage();
                var me = ((WidgetChatListActions.Model) methodHookParam.args[0]).getMe();
                var userID = msg.component4().i();
                var guild = ((WidgetChatListActions.Model) methodHookParam.args[0]).getGuild();
                var view = new TextView(ctx, null, 0, R.i.UiKit_Settings_Item_Icon);
                view.setId(viewID);
                view.setText("Softban user");
                var icon = ContextCompat.getDrawable(ctx, R.e.ic_ban_red_24dp);
                if (icon != null) {
                    icon = icon.mutate();
                    view.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                }
                view.setOnClickListener(view1 -> {
                    _this.dismiss();
                    textInput.setText(String.format(
                            "%ssoftban %s Nitro/Steam Scam; Your account may be hacked, please change your password. " +
                                    "You may rejoin at <https://discord.gg/S8waxK7QXd> after securing your account.",
                            pluginsettings.getString("prefix", "aou "),
                            userID
                    ));
                    textInput.setSelection(textInput.getSelectionEnd());
                });
                var hasPerms = false;
                var roleList = StoreStream.getGuilds().getRoles().get(guild.getId());
                var memberMe = StoreStream.getGuilds().getMember(guild.getId(), me.getId());
                var memberTarget = StoreStream.getGuilds().getMember(guild.getId(), msg.component4().i());
                for (long roleID : memberMe.getRoles()) {
                    assert roleList != null;
                    var role = roleList.get(roleID);
                    if (role == null) return;
                    var perms = role.h();
                    if (PermissionUtils.can(Permission.BAN_MEMBERS, perms)) {
                        hasPerms = true;
                    }
                }
                GuildRole highestRoleMe;
                GuildRole highestRoleTarget;
                assert roleList != null;
                assert memberTarget.getRoles().size() != 0;
                assert memberMe.getRoles().size() != 0;
                highestRoleMe = roleList.get(memberMe.getRoles().get(memberMe.getRoles().size() - 1));
                highestRoleTarget = roleList.get(memberTarget.getRoles().get(memberTarget.getRoles().size() - 1));
                if (userID != me.getId() && guild.getId() == Long.parseLong("794950428756410429") && hasPerms) {
                    assert highestRoleMe != null;
                    assert highestRoleTarget != null;
                    if (highestRoleMe.i() > highestRoleTarget.i()) layout.addView(view, 1);
                }
            }));
        } catch (Exception e) {
            logger.error("Patching failed!", e);
        }
    }

    private void registerCommands() {
        var options = Arrays.asList(
                Utils.createCommandOption(ApplicationCommandType.STRING, "User ID", "The ID of the user to be softbanned.", null, true),
                Utils.createCommandOption(ApplicationCommandType.STRING, "Prefix", "The prefix that you use with the bot. Use this if AOUutils is temporarily offline.", null, false)
        );

        commands.registerCommand(
                "scam",
                "Softban a user due to a nitro scam.",
                options,
                ctx -> {
                    // get argument passed to the world option or fall back to Earth if not specified
                    String id = ctx.getStringOrDefault("User ID", "nothing");
                    String prefix = ctx.getStringOrDefault("Prefix", "aou ");

                    if (id.length() == 18) {
                        try {
                            Long.parseLong(id);
                        } catch (Exception e) {
                            return new CommandsAPI.CommandResult("Invalid ID", null, false);
                        }
                        if (prefix.equals("aou "))
                            prefix = pluginsettings.getString("prefix", "aou ");
                        return new CommandsAPI.CommandResult(String.format("%ssoftban %s Nitro/Steam Scam; Your account may be hacked, please change your password. You may rejoin at <https://discord.gg/S8waxK7QXd> after securing your account.", prefix, id), null, true);
                    } else {
                        return new CommandsAPI.CommandResult("Invalid ID. A user ID is of 18 characters!", null, false);
                    }
                }
        );
    }

    @Override
    // Called when your plugin is stopped
    public void stop(Context context) {
        patcher.unpatchAll();
        commands.unregisterAll();
    }
}
