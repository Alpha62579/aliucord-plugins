package com.alphapython.massmoderation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.aliucord.Constants;
import com.discord.models.user.User;
import com.discord.utilities.color.ColorCompat;
import com.discord.utilities.images.MGImages;
import com.facebook.drawee.view.SimpleDraweeView;
import com.lytefast.flexinput.R;

@SuppressLint("ViewConstructor")
public class MemberView extends LinearLayout {
    public SimpleDraweeView avatar;
    public TextView name;
    public ImageButton kick;
    public ImageButton ban;
    public ImageButton remove;

    public MemberView(Context context, User user, long guildID, ModPage settingPage) {
        super(context);
        setId((int) user.getId());
        setOrientation(HORIZONTAL);
        setGravity(Gravity.LEFT);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Member avatar
        var avatarParams = new LayoutParams(64, 64);
        avatar = new SimpleDraweeView(context);
        avatar.setLayoutParams(avatarParams);
        MGImages.setRoundingParams(avatar, 20f, false, null, null, 0f);
        avatar.setImageURI(String.format("https://cdn.discordapp.com/avatars/%s/%s.png", user.getId(), user.getAvatar()));

        // Member name
        name = new TextView(context);
        name.setMaxWidth(380);
        name.setPadding(50, 20, 0, 20);
        name.setText(String.format("%s#%s", user.getUsername(), user.getDiscriminator()));
        name.setTypeface(ResourcesCompat.getFont(context, Constants.Fonts.whitney_semibold));
        name.setTextColor(Color.WHITE);

        // Actions (Kick, ban, remove from list).
        // Creating a new layout was probably useless, but anyway.
        var actions = new LinearLayout(context);
        actions.setPadding(10, 20, 0, 20);

        // Kick button
        var drawable = ContextCompat.getDrawable(context, R.e.ic_remove_friend_red_24dp);
        drawable.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
        kick = new ImageButton(context);
        kick.setImageDrawable(drawable);
        kick.setBackground(null);
        kick.setOnClickListener(event -> {
            if (ModPage.kickUser(user, guildID)) settingPage.removeAndUpdateList(user);
        });

        // Ban button
        drawable = ContextCompat.getDrawable(context, R.e.ic_ban_red_24dp);
        drawable.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
        ban = new ImageButton(context);
        ban.setImageDrawable(drawable);
        ban.setBackground(null);
        ban.setOnClickListener(event -> {
            if (ModPage.banUser(user, guildID)) settingPage.removeAndUpdateList(user);
        });

        // Remove from list button
        drawable = ContextCompat.getDrawable(context, R.e.ic_x_red_24dp);
        drawable.setTint(ColorCompat.getThemedColor(context, R.b.colorInteractiveNormal));
        remove = new ImageButton(context);
        remove.setImageDrawable(drawable);
        remove.setBackground(null);
        remove.setOnClickListener(event -> settingPage.removeAndUpdateList(user));

        // Add views.
        actions.addView(kick);
        actions.addView(ban);
        actions.addView(remove);

        addView(avatar);
        addView(name);
        addView(actions);
    }
}
