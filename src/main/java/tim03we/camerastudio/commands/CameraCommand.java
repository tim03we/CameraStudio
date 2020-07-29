package tim03we.camerastudio.commands;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Location;
import tim03we.camerastudio.CameraStudio;
import tim03we.camerastudio.Language;
import tim03we.camerastudio.Travel;

public class CameraCommand extends Command {

	public CameraCommand() {
		super("camerastudio", "Camera Command");
		setAliases(new String[]{"camera", "cam"});
		setPermission("camerastudio.use");
	}

	@Override
	public boolean execute(CommandSender sender, String s, String[] args) {
		if(!testPermission(sender)) {
			return false;
		}
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("add")) {
					List<Location> locs = CameraStudio.points.get(player.getUniqueId());
					if (locs == null) {
						locs = new ArrayList<>();
					}
					locs.add(player.getLocation());
					CameraStudio.points.put(player.getUniqueId(), locs);

					player.sendMessage(Language.getAndReplace("point.added", locs.size()));

					return true;
				} else if (args[0].equalsIgnoreCase("remove")) {
					List<Location> locs = CameraStudio.points.get(player.getUniqueId());
					if (locs == null) {
						locs = new ArrayList<>();
					}

					if (args.length > 1) {
						try {
							int pos = Integer.parseInt(args[1]);
							if (locs.size() >= pos) {
								locs.remove(pos - 1);
								player.sendMessage(Language.getAndReplace("point.removed", pos));
							} else {
								player.sendMessage(Language.getAndReplace("no.point.size", args[1]));
							}
						} catch (Exception e) {
							player.sendMessage(Language.getAndReplace("not.a.number", args[1]));
						}
					}
					CameraStudio.points.put(player.getUniqueId(), locs);
				} else if (args[0].equalsIgnoreCase("list")) {
					List<Location> locs = CameraStudio.points.get(player.getUniqueId());
					if ((locs == null) || (locs.size() == 0)) {
						player.sendMessage(Language.get("no.points"));
						return true;
					}
					int i = 1;
					player.sendMessage(Language.get("point.list.title"));
					for (Location loc : locs) {
						player.sendMessage(Language.getAndReplaceNoPrefix("point.list", i, Travel.round(loc.getX(), 1), Travel.round(loc.getY(), 1), Travel.round(loc.getZ(), 1), Travel.round(loc.getYaw(), 1), Travel.round(loc.getPitch(), 1)));
						i++;
					}
				} else if (args[0].equalsIgnoreCase("reset")) {
					CameraStudio.points.remove(player.getUniqueId());
					player.sendMessage(Language.get("points.reset"));
					return true;
				} else if (args[0].equalsIgnoreCase("tp")) {
					if (args.length > 1) {
						try {
							int pos = Integer.parseInt(args[1]);
							List<Location> locs = CameraStudio.points.get(player.getUniqueId());
							if ((locs != null) && (locs.size() >= pos)) {
								player.teleport(locs.get(pos - 1));
								player.sendMessage(Language.getAndReplace("point.teleport", pos));
								return true;
							}
							if (locs == null) {
								player.sendMessage(Language.get("no.points"));
								return true;
							}
							player.sendMessage(Language.getAndReplace("no.point.size", args[1]));
						} catch (Exception e) {
							player.sendMessage(Language.getAndReplace("not.a.number", args[1]));
						}
					} else {
						player.sendMessage(Language.get("specify.number"));
					}
				} else if (args[0].equalsIgnoreCase("stop")) {
					Travel.stop(player.getUniqueId());
					player.sendMessage(Language.get("travel.cancelled"));
				} else if ((args[0].equalsIgnoreCase("start"))) {
					if(args.length > 1) {
						List<Location> listOfLocs = new ArrayList<>();
						if (CameraStudio.points.get(player.getUniqueId()) != null)
							listOfLocs.addAll(CameraStudio.points.get(player.getUniqueId()));

						if (Travel.isTravelling(player.getUniqueId())) {
							player.sendMessage(Language.get("already.travelling"));
							return true;
						}
						if (listOfLocs.isEmpty() || listOfLocs.size() <= 1) {
							player.sendMessage(Language.get("not.enough.points"));
							return true;
						}
						try {
							Travel.travel(player, listOfLocs, Travel.parseTimeString(args[1]));
							player.sendMessage(Language.get("travelling.started"));
						} catch (ParseException e) {
							player.sendMessage(Language.get("start.example"));
							e.printStackTrace();
						}
					} else {
						player.sendMessage(Language.get("start.example"));
					}
				} else {
					player.sendMessage(Language.getNoPrefix("help"));
				}
			} else {
				player.sendMessage(Language.getNoPrefix("help"));
			}
		} else {
			sender.sendMessage("Run this command in-game.");
		}
		return false;
	}
}
