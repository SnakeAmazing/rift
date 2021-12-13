package me.ufo.rift;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import me.ufo.rift.commands.RiftCommand;
import me.ufo.rift.commands.WhitelistCommand;
import me.ufo.rift.config.RiftConfig;
import me.ufo.rift.listeners.RiftInboundListener;
import me.ufo.rift.listeners.RiftServerListener;
import me.ufo.rift.queues.tasks.QueuePositionTask;
import me.ufo.rift.queues.tasks.QueuePushTask;
import me.ufo.rift.redis.Redis;
import me.ufo.rift.server.RiftServer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public final class Rift extends Plugin {

  private static Rift instance;

  private final RiftConfig config;
  private Redis redis;
  private ScheduledTask queuePushTask;
  private ScheduledTask queuePositionTask;

  private Map<UUID, String> whitelistedPlayers;
  private AtomicBoolean whitelisted;

  private boolean debug;

  public Rift() throws IOException {
    this.config = new RiftConfig(this);
    this.whitelisted = new AtomicBoolean(this.config.isWhitelisted());
    this.whitelistedPlayers = new HashMap<>(this.config.getWhitelistedPlayers());
  }

  @Override
  public void onLoad() {
    this.getLogger().info(
      "\n     _  __ _   \n" +
      "      (_)/ _| |  \n" +
      "  _ __ _| |_| |_ \n" +
      " | '__| |  _| __|    BUNGEE SERVER\n" +
      " | |  | | | | |_ \n" +
      " |_|  |_|_|  \\__|\n"
    );

    this.redis = new Redis(this);
  }

  @Override
  public void onEnable() {
    instance = this;

    // Register commands & listeners
    final PluginManager pm = this.getProxy().getPluginManager();
    pm.registerCommand(this, new RiftCommand(this));
    pm.registerCommand(this, new WhitelistCommand(this));
    pm.registerListener(this, new RiftInboundListener(this));
    pm.registerListener(this, new RiftServerListener(this));

    // Register tasks
    // TODO: editable times
    this.queuePushTask = this.getProxy().getScheduler().schedule(
      this, new QueuePushTask(this), 1, 500, TimeUnit.MILLISECONDS);

    this.queuePositionTask = this.getProxy().getScheduler().schedule(
      this, new QueuePositionTask(this), 1, 5, TimeUnit.SECONDS);
  }

  @Override
  public void onDisable() {
    this.config().saveWhitelistedPlayers();

    this.queuePositionTask.cancel();
    this.queuePushTask.cancel();
    this.redis.close();
  }

  public void info(final String message) {
    this.getLogger().info(message);
  }

  public void severe(final String message) {
    this.getLogger().severe(message);
  }

  public ServerInfo getLeastPopulatedHub() {
    ServerInfo out = null;
    for (final RiftServer server : RiftServer.getServers()) {
      if (server.isHubServer()) {

        ServerInfo info = ProxyServer.getInstance().getServerInfo(server.getName());

        if (info != null) {
          if (!this.isServerOnline(info)) {
            continue;
          }

          if (out == null) {
            out = info;
            continue;
          }

          if (out.getPlayers().size() > info.getPlayers().size()) {
            out = info;
          }
        }
      }
    }

    return out;
  }

  private boolean isServerOnline(final ServerInfo serverInfo) {
    try (final Socket socket = new Socket()) {
      socket.connect(serverInfo.getSocketAddress());
      return true;
    } catch (final IOException ignored) {
    }
    return false;
  }

  public RiftConfig config() {
    return this.config;
  }

  public Redis redis() {
    return this.redis;
  }

  public Map<UUID, String> getWhitelistedPlayers() {
    return whitelistedPlayers;
  }

  public boolean isWhitelisted() {
    return this.whitelisted.get();
  }

  public void setWhitelisted(final boolean whitelisted) {
    this.whitelisted.set(whitelisted);
    this.config.setWhitelisted(whitelisted);
  }

  public boolean debug() {
    return this.debug;
  }

  public boolean toggleDebug() {
    this.debug = !this.debug;
    return this.debug;
  }

  public static Rift instance() {
    return instance;
  }

}
