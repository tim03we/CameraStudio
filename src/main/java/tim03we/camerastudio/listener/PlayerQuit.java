package tim03we.camerastudio.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerQuitEvent;
import tim03we.camerastudio.CameraStudio;

public class PlayerQuit implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        CameraStudio.points.remove(event.getPlayer().getUniqueId());
    }
}
