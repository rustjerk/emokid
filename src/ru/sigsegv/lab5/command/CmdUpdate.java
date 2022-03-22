package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;

import java.util.Set;

/**
 * `update` command handler
 */
public class CmdUpdate implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Long id = CmdUtils.parseLongArg(ctx, args, 1);
        if (id == null) return;

        MusicBand band = CmdUtils.enterMusicBand(ctx);
        if (band == null) return;

        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        set.removeIf(b -> b.getId() == id);
        band.setId(id);
        set.add(band);
    }

    @Override
    public String getDescription() {
        return "Updates a music band by id.";
    }

    @Override
    public String getHelpMessage() {
        return "Updates a music band by id.\nSyntax: update <id>";
    }
}
