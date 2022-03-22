package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;

import java.util.Set;

/**
 * `remove_by_id` command handler
 */
public class CmdRemoveById implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Long id = CmdUtils.parseLongArg(ctx, args, 1);
        if (id == null) return;

        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        if (set.removeIf(b -> b.getId() == id))
            ctx.printf("Removed element %d%n", id);
        else
            ctx.printf("No element with id %d%n", id);
    }

    @Override
    public String getDescription() {
        return "Removes a music band by id.";
    }

    @Override
    public String getHelpMessage() {
        return "Removes a music band by id.\nSyntax: remove_by_id <id>";
    }
}
