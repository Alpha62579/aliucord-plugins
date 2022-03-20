package com.alphapython.massmoderation;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Constants;
import com.aliucord.Utils;
import com.aliucord.fragments.ConfirmDialog;
import com.aliucord.fragments.SettingsPage;
import com.aliucord.utils.RxUtils;
import com.discord.models.member.GuildMember;
import com.discord.models.user.User;
import com.discord.restapi.RestAPIParams;
import com.discord.stores.StoreStream;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.rest.RestAPI;
import com.lytefast.flexinput.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModPage extends SettingsPage {
    public ArrayList<User> memberList = new ArrayList<>();
    public int kickHeader;
    public int banHeader;
    private View modView;
    private long guildID;

    public ModPage(Map<Long, GuildMember> members, long guild, String query) {
        // Convert all members to the users that aren't you and does contain the search query in username
        for (GuildMember member :
                members.values()) {
            var user = StoreStream.getUsers().getUsers().get(member.getUserId());
            assert user != null;
            if ((user.getUsername() + "#" + user.getDiscriminator()).toLowerCase().contains(query) && user.getId() != StoreStream.getUsers().getMe().getId())
                memberList.add(user);
        }
        guildID = guild;
    }

    // Ban and kick functions.
    public static boolean banUser(User user, long guildID) {
        AtomicBoolean banned = new AtomicBoolean(true);
        new Thread(() -> RxUtils.subscribe(
                RestAPI.api.banGuildMember(guildID, user.getId(), new RestAPIParams.BanGuildMember(0), null),
                RxUtils.createActionSubscriber(
                        onNext -> {
                        },
                        onError -> {
                            Utils.showToast("Failed to ban " + user.getUsername());
                            banned.set(false);
                        }
                ))).start();
        return banned.get();
    }

    public static boolean kickUser(User user, long guildID) {
        AtomicBoolean kicked = new AtomicBoolean(true);
        new Thread(() -> RxUtils.subscribe(
                RestAPI.api.kickGuildMember(guildID, user.getId(), null),
                RxUtils.createActionSubscriber(
                        onNext -> {
                        },
                        onError -> {
                            Utils.showToast("Failed to kick " + user.getUsername());
                            kicked.set(false);
                        }
                ))).start();
        return kicked.get();
    }

    public void removeAndUpdateList(User user) {
        memberList.remove(user);
        removeView(modView.findViewById((int) user.getId()));
        ((TextView) modView.findViewWithTag("MemberCount")).setText(String.format("%d member(s)", memberList.size()));
        if (memberList.size() == 0) {
            removeHeaderButton(kickHeader);
            removeHeaderButton(banHeader);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewBound(View view) {
        super.onViewBound(view);
        modView = view;
        var ctx = getContext();
        setActionBarTitle("Mass Moderation");

        // No members matching criteria found. Tell that to the user and return
        if (memberList.isEmpty()) {
            var tempView = new TextView(ctx);
            tempView.setText("No members found.");
            tempView.setPadding(0, 20, 0, 20);
            tempView.setTextColor(Color.WHITE);
            tempView.setTextSize(20f);
            tempView.setTypeface(ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold));
            addView(tempView);
            return;
        }

        // Members matching criteria found.
        // Kick header button
        var drawable = ContextCompat.getDrawable(ctx, R.e.ic_remove_friend_red_24dp);
        drawable.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal));
        kickHeader = addHeaderButton("Kick ALL", drawable, onclick -> {
            var dialog = new ConfirmDialog();
            dialog.setTitle("Mass-kick members?");
            dialog.setDescription(String.format("Are you sure that you want to kick %s member(s)?", (long) memberList.size()));
            dialog.setIsDangerous(true);
            dialog.setOnOkListener(view1 -> {
                var kicked = new ArrayList<User>();
                dialog.dismiss();
                memberList.forEach(user -> {
                    if (kickUser(user, guildID)) kicked.add(user);
                });
                kicked.forEach(this::removeAndUpdateList);
            });
            dialog.setOnCancelListener(view1 -> dialog.dismiss());
            dialog.show(getParentFragmentManager(), "Mass-kick");
            return true;
        });

        // Ban header button
        drawable = ContextCompat.getDrawable(ctx, R.e.ic_ban_red_24dp);
        drawable.setTint(ColorCompat.getThemedColor(ctx, R.b.colorInteractiveNormal));
        banHeader = addHeaderButton("Ban ALL", drawable, onclick -> {
            var dialog = new ConfirmDialog();
            dialog.setTitle("Mass-ban members?");
            dialog.setDescription(String.format("Are you sure that you want to ban %s member(s)?", (long) memberList.size()));
            dialog.setIsDangerous(true);
            dialog.setOnOkListener(view1 -> {
                var banned = new ArrayList<User>();
                dialog.dismiss();
                memberList.forEach(user -> {
                    if (banUser(user, guildID)) banned.add(user);
                });
                banned.forEach(this::removeAndUpdateList);
            });
            dialog.setOnCancelListener(view1 -> dialog.dismiss());
            dialog.show(getParentFragmentManager(), "Mass-ban");
            return true;
        });

        // Member count
        var tempView = new TextView(ctx);
        tempView.setTag("MemberCount");
        tempView.setPadding(0, 20, 0, 20);
        tempView.setText(String.format("%s member(s)", (long) memberList.size()));
        tempView.setTextColor(Color.WHITE);
        tempView.setTextSize(20f);
        tempView.setTypeface(ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_bold));
        addView(tempView);
        memberList.forEach(user -> addView(new MemberView(ctx, user, guildID, this)));
    }
}
