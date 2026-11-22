package com.alfgoto.rune.visuals.codex;

import com.alfgoto.rune.foundations.module.RuneContext;
import com.alfgoto.rune.foundations.module.RuneModule;
import com.alfgoto.rune.gameplay.items.Codex;
import com.alfgoto.rune.gameplay.items.ItemsModule;
import com.alfgoto.rune.gameplay.jobs.JobProgress;
import com.alfgoto.rune.gameplay.jobs.JobRegistry;
import com.alfgoto.rune.gameplay.jobs.JobsModule;
import com.alfgoto.rune.gameplay.quests.QuestBook;
import com.alfgoto.rune.gameplay.quests.QuestsModule;
import com.alfgoto.rune.gameplay.runes.PlayerCapacity;
import com.alfgoto.rune.gameplay.runes.PlayerLoadout;
import com.alfgoto.rune.gameplay.runes.RuneRegistry;
import com.alfgoto.rune.gameplay.runes.RunesModule;
import com.alfgoto.rune.visuals.menu.MenuModule;
import com.google.auto.service.AutoService;
import java.util.Set;

/** The Rune Codex menus: clicking the Codex opens the main menu, which leads to the Jobs page. */
@AutoService(RuneModule.class)
public final class CodexModule implements RuneModule {

    @Override
    public void enable(RuneContext ctx) {
        CodexMenus menus = new CodexMenus(
                ctx.get(JobRegistry.class).registry(),
                ctx.get(JobProgress.class),
                ctx.get(PlayerCapacity.class),
                ctx.get(QuestBook.class),
                ctx.config(),
                ctx.get(PlayerLoadout.class),
                ctx.get(RuneRegistry.class));
        ctx.listen(new CodexClickListener(ctx.get(Codex.class), menus));
        ctx.listen(new QuestsMenuRefresher());
    }

    @Override
    public Set<Class<? extends RuneModule>> dependsOn() {
        return Set.of(ItemsModule.class, JobsModule.class, QuestsModule.class, RunesModule.class, MenuModule.class);
    }
}
