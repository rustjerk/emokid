package ru.sigsegv.lab5.command;

import ru.sigsegv.lab5.model.MusicBand;
import ru.sigsegv.lab5.model.Studio;

import java.util.*;
import java.util.stream.Collectors;

/**
 * `print_field_ascending_studio` command handler
 */
public class CmdPrintFieldAscendingStudio implements Command {
    @Override
    public void execute(CommandCtx ctx, String[] args) {
        Set<MusicBand> set = ctx.getDatabase().getMusicBandSet();
        List<Studio> studios = set.stream()
                .map(MusicBand::getStudio)
                .filter(Objects::nonNull)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        for (Studio studio : studios) {
            ctx.println(studio.toString());
        }
    }

    @Override
    public String getDescription() {
        return "Prints the studios in ascending order.";
    }
}
