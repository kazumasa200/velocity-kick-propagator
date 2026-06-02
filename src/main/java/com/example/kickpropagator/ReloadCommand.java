package com.example.kickpropagator;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ReloadCommand {

    private final KickPropagatorPlugin plugin;

    public ReloadCommand(KickPropagatorPlugin plugin) {
        this.plugin = plugin;
    }

    public BrigadierCommand build() {
        LiteralArgumentBuilder<CommandSource> node = BrigadierCommand.literalArgumentBuilder("kickpropagator")
            .requires(source -> source.hasPermission("kickpropagator.reload"))
            .then(BrigadierCommand.literalArgumentBuilder("reload")
                .executes(ctx -> {
                    CommandSource source = ctx.getSource();
                    try {
                        plugin.loadConfig();
                        source.sendMessage(Component.text(
                            "[KickPropagator] 設定をリロードしました。対象サーバー: " + plugin.getTargetServers(),
                            NamedTextColor.GREEN
                        ));
                    } catch (Exception e) {
                        source.sendMessage(Component.text(
                            "[KickPropagator] リロードに失敗しました: " + e.getMessage(),
                            NamedTextColor.RED
                        ));
                    }
                    return Command.SINGLE_SUCCESS;
                })
            )
            .executes(ctx -> {
                ctx.getSource().sendMessage(Component.text(
                    "使い方: /kickpropagator reload",
                    NamedTextColor.YELLOW
                ));
                return Command.SINGLE_SUCCESS;
            });

        return new BrigadierCommand(node);
    }
}
