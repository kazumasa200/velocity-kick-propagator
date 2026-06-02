package com.example.kickpropagator;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Plugin(
    id = "kick-propagator",
    name = "KickPropagator",
    version = "1.0.0",
    description = "特定のバックエンドサーバーからキックされた場合、Velocity ネットワークからも強制切断するプラグイン",
    authors = {"kazumasa200"}
)
public class KickPropagatorPlugin {

    private static final List<Pattern> CONNECTION_ERROR_PATTERNS = List.of(
        Pattern.compile("(?i).*connection (reset|closed|timed? ?out).*"),
        Pattern.compile("(?i).*read timed? ?out.*"),
        Pattern.compile("(?i).*end of stream.*"),
        Pattern.compile("(?i).*broken pipe.*"),
        Pattern.compile("(?i).*server closed.*"),
        Pattern.compile("(?i).*IOException.*")
    );

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;

    // volatile で KickedFromServerEvent スレッドから安全に参照できるようにする
    private volatile Set<String> targetServers = new HashSet<>();
    private volatile boolean useTargetServers = true;

    @Inject
    public KickPropagatorPlugin(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        loadConfig();
        logCurrentConfig();

        CommandManager cm = proxy.getCommandManager();
        cm.register(cm.metaBuilder("kickpropagator").plugin(this).build(),
            new ReloadCommand(this).build());
    }

    public Set<String> getTargetServers() {
        return Collections.unmodifiableSet(targetServers);
    }

    public boolean isUseTargetServers() {
        return useTargetServers;
    }

    private void logCurrentConfig() {
        if (useTargetServers) {
            logger.info("KickPropagator が有効化されました。対象サーバー: {}", targetServers);
        } else {
            logger.info("KickPropagator が有効化されました。全サーバーからのキックをネットワーク切断します。");
        }
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onKickedFromServer(KickedFromServerEvent event) {
        String serverName = event.getServer().getServerInfo().getName();

        // 対象サーバー絞り込みが有効な場合、リストに含まれないサーバーはスキップ
        if (useTargetServers && !targetServers.contains(serverName)) {
            return;
        }

        Component reason = event.getServerKickReason().orElse(Component.empty());
        String plainReason = PlainTextComponentSerializer.plainText().serialize(reason);

        // 接続エラーはフォールバックに任せる
        if (isConnectionError(plainReason)) {
            logger.debug("接続エラーと判定（フォールバック許可）: {} @ {} - {}",
                event.getPlayer().getUsername(), serverName, plainReason);
            return;
        }

        logger.info("ネットワークキック実行: {} がサーバー {} からキックされました。理由: {}",
            event.getPlayer().getUsername(),
            serverName,
            plainReason.isEmpty() ? "(理由なし)" : plainReason);

        Component disconnectMessage = reason.equals(Component.empty())
            ? Component.text("サーバーからキックされました。")
            : reason;

        event.setResult(KickedFromServerEvent.DisconnectPlayer.create(disconnectMessage));
    }

    private boolean isConnectionError(String reason) {
        if (reason.isEmpty()) return true;
        for (Pattern pattern : CONNECTION_ERROR_PATTERNS) {
            if (pattern.matcher(reason).matches()) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void loadConfig() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve("config.yml");
            if (!Files.exists(configFile)) {
                // デフォルト設定ファイルをコピー
                try (InputStream in = getClass().getResourceAsStream("/config.yml");
                     OutputStream out = Files.newOutputStream(configFile)) {
                    if (in != null) in.transferTo(out);
                }
                logger.info("config.yml を生成しました: {}", configFile);
            }

            try (InputStream in = Files.newInputStream(configFile)) {
                Map<String, Object> config = new Yaml().load(in);
                if (config == null) return;

                Object useTarget = config.get("use-target-servers");
                if (useTarget instanceof Boolean) {
                    useTargetServers = (Boolean) useTarget;
                }

                Object servers = config.get("target-servers");
                if (servers instanceof List) {
                    targetServers = new HashSet<>((List<String>) servers);
                }
            }

        } catch (IOException e) {
            logger.error("config.yml の読み込みに失敗しました。デフォルト設定を使用します。", e);
        }
    }
}
