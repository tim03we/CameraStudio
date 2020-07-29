package tim03we.camerastudio;

import java.util.*;

import cn.nukkit.event.Listener;
import cn.nukkit.level.Location;
import cn.nukkit.plugin.PluginBase;
import tim03we.camerastudio.commands.CameraCommand;
import tim03we.camerastudio.listener.PlayerQuit;

public class CameraStudio extends PluginBase implements Listener {

	private static CameraStudio instance;
	public static HashSet<UUID> travelling = new HashSet<>();
	public static HashSet<UUID> stopping = new HashSet<>();
	public static HashMap<UUID, List<Location>> points = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;
		Language.init();
		registerCommands();
		registerListeners();
	}

	public static CameraStudio getInstance() {
		return instance;
	}

	private void registerCommands() {
		getServer().getCommandMap().register("camerastudio", new CameraCommand());
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new PlayerQuit(), this);
	}
}