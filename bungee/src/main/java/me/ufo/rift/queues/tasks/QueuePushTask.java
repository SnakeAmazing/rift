package me.ufo.rift.queues.tasks;

import me.ufo.rift.Rift;
import me.ufo.rift.queues.QueuePlayer;
import me.ufo.rift.queues.RiftQueue;
import me.ufo.rift.server.RiftServer;
import me.ufo.rift.server.RiftServerStatus;
import net.md_5.bungee.api.ProxyServer;

public final class QueuePushTask implements Runnable {

  private final Rift plugin;

  public QueuePushTask(final Rift plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for (final RiftQueue queue : RiftQueue.getQueues()) {
      if (this.plugin.debug()) {
        this.plugin.info(queue.toString());
      }

      if (!queue.hasDestinationServer()) {
        continue;
      }

      if (!queue.isQueuing()) {
        continue;
      }

      final RiftServer riftServer = RiftServer.fromName(queue.getName());
      if (riftServer == null) {
        continue;
      }

      if (!riftServer.isOnline()) {
        continue;
      }

      if (riftServer.getServerStatus() != RiftServerStatus.ONLINE) {
        continue;
      }

      final QueuePlayer player = queue.getPriorityQueue().poll();
      if (player != null) {

        ProxyServer.getInstance().getPlayer(player.getUuid())
          .connect(ProxyServer.getInstance().getServerInfo(player.getDestination()));

        player.destroy();
      }
    }
  }

}
