package com.alphapython.githubstats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliucord.Http;
import com.aliucord.Logger;
import com.aliucord.Utils;
import com.aliucord.annotations.AliucordPlugin;
import com.aliucord.api.SettingsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.patcher.Hook;
import com.aliucord.utils.DimenUtils;
import com.aliucord.utils.ReflectUtils;
import com.discord.api.connectedaccounts.ConnectedAccount;
import com.discord.databinding.WidgetUserSheetBinding;
import com.discord.widgets.user.profile.UserProfileConnectionsView;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.lytefast.flexinput.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
@AliucordPlugin
public class GitHubStats extends Plugin {
    public static final Pattern dataPattern = Pattern.compile("([0-9])+<", Pattern.CASE_INSENSITIVE);
    public static SettingsAPI pluginsettings;
    public static Logger logger = new Logger("GitHubStats");
    public static int viewID = View.generateViewId();

    public GitHubStats() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void start(Context context) throws NoSuchMethodException {
        pluginsettings = settings;
        AtomicReference<ArrayList<String>> statHolder = new AtomicReference<>();

        patcher.patch(UserProfileConnectionsView.ViewHolder.class.getDeclaredMethod("onConfigure", int.class, UserProfileConnectionsView.ConnectedAccountItem.class), new Hook(methodHookParam -> {
            // Get connected account
            var account = (ConnectedAccount) ((UserProfileConnectionsView.ConnectedAccountItem) methodHookParam.args[1]).getConnectedAccount();
            if (account.g().equals("github")) {  // ConnectedAccount.g() is the lowercase connection name
                statHolder.set(getStats(account.d()));
            }
        }));

        patcher.patch(WidgetUserSheet.class.getDeclaredMethod("configureGuildSection", WidgetUserSheetViewModel.ViewState.Loaded.class), new Hook(methodHookParam -> {
            var _this = (WidgetUserSheet) methodHookParam.thisObject;
            if (statHolder.get() == null) return;
            var stats = statHolder.get();
            statHolder.set(null);
            try {
                // Get binding and the user sheet
                WidgetUserSheetBinding binding = (WidgetUserSheetBinding) ReflectUtils.invokeMethod(WidgetUserSheet.class, _this, "getBinding");
                assert binding != null;
                var sheetContent = (LinearLayout) binding.getRoot().findViewById(Utils.getResId("user_sheet_content", "id"));
                var ctx = sheetContent.getContext();

                // The view already exists, no need for duplicates.
                if (sheetContent.findViewById(viewID) != null) return;

                // Define container for stats
                LinearLayout layout = new LinearLayout(ctx);
                layout.setId(viewID);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(DimenUtils.dpToPx(16), 0, DimenUtils.dpToPx(16), 0);
                layout.setLayoutParams(params);

                // Header
                TextView header = new TextView(ctx, null, 0, R.i.UserProfile_Section_Header);
                header.setText("GITHUB");
                LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                viewParams.setMargins(0, 0, 0, DimenUtils.dpToPx(16) / 2);
                header.setLayoutParams(viewParams);
                layout.addView(header);

                var itemParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                itemParams.setMargins(DimenUtils.dpToPx(16), 0, 0, 0);

                // Hardcode montage
                // All the stats
                if (pluginsettings.getBool("stars", true)) {
                    TextView stars = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                    stars.setText("Total Stars Earned: " + stats.get(0));
                    stars.setLayoutParams(itemParams);
                    layout.addView(stars);
                }


                if (pluginsettings.getBool("commits", true)) {
                    TextView commits = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                    commits.setText("Total Commits" + (pluginsettings.getBool("yearly", true) ? String.format("(%s)", Calendar.getInstance().get(Calendar.YEAR)) : "") + ": " + stats.get(1));
                    commits.setLayoutParams(itemParams);
                    layout.addView(commits);
                }

                if (pluginsettings.getBool("prs", true)) {
                    TextView prs = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                    prs.setText("Total PRs: " + stats.get(2));
                    prs.setLayoutParams(itemParams);
                    layout.addView(prs);
                }

                if (pluginsettings.getBool("issues", true)) {
                    TextView issues = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                    issues.setText("Total Issues: " + stats.get(3));
                    issues.setLayoutParams(itemParams);
                    layout.addView(issues);
                }

                if (pluginsettings.getBool("contributions", true)) {
                    TextView contributions = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                    contributions.setText("Contributed to: " + stats.get(4));
                    contributions.setLayoutParams(itemParams);
                    layout.addView(contributions);
                }

                // Add view if any one setting is trues
                for (String key :
                        pluginsettings.getAllKeys()) {
                    if (pluginsettings.getBool(key, true)) {
                        sheetContent.addView(layout, sheetContent.indexOfChild(sheetContent.findViewById(Utils.getResId("rich_presence_container", "id"))));
                        break;
                    }
                }


            } catch (Exception e) {
                logger.error(e);
            }
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    public ArrayList<String> getStats(String username) {
        // Get data and parse
        AtomicReference<String> rawHTML = new AtomicReference<>();
        var thread = new Thread(() -> {
            try {
                rawHTML.set(Http.simpleGet("https://github-readme-stats.vercel.app/api?username=" + username));
            } catch (Exception e) {
                logger.error(e);
            }
        });
        thread.start();
        while (thread.isAlive()) thread.getId();
        if (rawHTML.get() != null) {
            var matcher = dataPattern.matcher(rawHTML.get());
            var matches = new ArrayList<String>();
            while (matcher.find()) {
                matches.add(matcher.group().replace("<", ""));
            }
            return matches;
        }
        return new ArrayList<>();
    }
}
