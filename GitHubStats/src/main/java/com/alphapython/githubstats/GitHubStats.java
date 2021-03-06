package com.alphapython.githubstats;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.NetworkOnMainThreadException;
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
import com.aliucord.patcher.PreHook;
import com.aliucord.utils.DimenUtils;
import com.aliucord.utils.ReflectUtils;
import com.discord.databinding.WidgetUserSheetBinding;
import com.discord.widgets.user.profile.UserProfileConnectionsView;
import com.discord.widgets.user.usersheet.WidgetUserSheet;
import com.discord.widgets.user.usersheet.WidgetUserSheetViewModel;
import com.google.gson.reflect.TypeToken;
import com.lytefast.flexinput.R;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
@AliucordPlugin
public class GitHubStats extends Plugin {
    public static SettingsAPI pluginsettings;
    public static Logger logger = new Logger("GitHubStats");
    public static int viewID = View.generateViewId();

    public GitHubStats() {
        settingsTab = new SettingsTab(Settings.class, SettingsTab.Type.BOTTOM_SHEET).withArgs(settings);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void start(Context context) throws NoSuchMethodException {
        pluginsettings = settings;

        patcher.patch(WidgetUserSheet.class.getDeclaredMethod("configureConnectionsSection", WidgetUserSheetViewModel.ViewState.Loaded.class), new Hook(methodHookParam -> {
            var _this = (WidgetUserSheet) methodHookParam.thisObject;
            var connectedAccountItems = ((WidgetUserSheetViewModel.ViewState.Loaded) methodHookParam.args[0]).getConnectionsViewState().getConnectedAccountItems();
            for (UserProfileConnectionsView.ConnectedAccountItem item :
                    connectedAccountItems) {
                if (item.getConnectedAccount().g().equals("github")) {
                    try {
                        // Get stats
                        var stats = getStats(item.getConnectedAccount().d());

                        // API yielded null
                        if (stats == null) return;

                        // Get binding and the user sheet
                        WidgetUserSheetBinding binding = (WidgetUserSheetBinding) ReflectUtils.invokeMethod(WidgetUserSheet.class, _this, "getBinding");
                        assert binding != null;
                        var sheetContent = (LinearLayout) binding.getRoot().findViewById(Utils.getResId("user_sheet_content", "id"));
                        var ctx = sheetContent.getContext();

                        // The view already exists, yeet it
                        if (sheetContent.findViewById(viewID) != null)
                            sheetContent.removeView(sheetContent.findViewById(viewID));

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
                            stars.setText("Total Stars Earned: " + stats.get("directStars"));
                            stars.setLayoutParams(itemParams);
                            layout.addView(stars);
                        }


                        if (pluginsettings.getBool("commits", true)) {
                            TextView commits = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                            commits.setText("Total Commits: " + stats.get("commits"));
                            commits.setLayoutParams(itemParams);
                            layout.addView(commits);
                        }

                        if (pluginsettings.getBool("prs", true)) {
                            TextView prs = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                            prs.setText("Total PRs: " + stats.get("pullRequests"));
                            prs.setLayoutParams(itemParams);
                            layout.addView(prs);
                        }

                        if (pluginsettings.getBool("issues", true)) {
                            TextView issues = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                            issues.setText("Total Issues: " + stats.get("issues"));
                            issues.setLayoutParams(itemParams);
                            layout.addView(issues);
                        }

                        if (pluginsettings.getBool("contributions", true)) {
                            TextView contributions = new TextView(ctx, null, 0, R.i.UiKit_TextAppearance_Semibold);
                            contributions.setText("Contributed to: " + stats.get("contributedToNotOwnerRepositories"));
                            contributions.setLayoutParams(itemParams);
                            layout.addView(contributions);
                        }

                        // Add view if any one setting is true
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
                }
            }
        }));
    }

    @Override
    public void stop(Context context) {
        patcher.unpatchAll();
    }

    public Map<String, String> getStats(String username) {
        AtomicReference<Map<String, String>> data = new AtomicReference<>();
        var thread = new Thread(() -> {
            try {
                data.set(Http.simpleJsonGet(
                        String.format("https://awesome-github-stats.azurewebsites.net/user-stats/%s/stats", username),
                        TypeToken.getParameterized(Map.class, String.class, String.class).getType()
                ));
            } catch (Exception e) {
                logger.error(e);
            }
        });
        try {
            thread.start();
            while (thread.isAlive()) thread.getId();
        } catch (NetworkOnMainThreadException e) {
            try {
                thread.start(); // Retry one last time
                while (thread.isAlive()) thread.getId();
            } catch (NetworkOnMainThreadException a) {
                logger.error("Error getting stats...", a);
                // EA sports
            }
        }
        return data.get();
    }
}
