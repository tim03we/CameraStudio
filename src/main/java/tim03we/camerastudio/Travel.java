package tim03we.camerastudio;

import cn.nukkit.AdventureSettings;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.network.protocol.MovePlayerPacket;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class Travel {

    public static double round(double unrounded, int precision) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, 4);
        return rounded.doubleValue();
    }

    public static void travel(final Player player, List<Location> locations, int time) {
        List<Double> diffs = new ArrayList<>();
        List<Integer> travelTimes = new ArrayList<>();
        double totalDiff = 0.0D;
        for (int i = 0; i < locations.size() - 1; i++) {
            Location s = locations.get(i);
            Location n = locations.get(i + 1);
            double diff = positionDifference(s, n);
            totalDiff += diff;
            diffs.add(diff);
        }
        for (double d : diffs) {
            travelTimes.add((int) (d / totalDiff * time));
        }

        final List<Location> tps = new ArrayList<>();

        Level w = player.getLevel();

        for (int i = 0; i < locations.size() - 1; i++) {
            Location s = locations.get(i);
            Location n = locations.get(i + 1);
            int t = travelTimes.get(i);

            double moveX = n.getX() - s.getX();
            double moveY = n.getY() - s.getY();
            double moveZ = n.getZ() - s.getZ();
            double movePitch = n.getPitch() - s.getPitch();

            double yawDiff = Math.abs(n.getYaw() - s.getYaw());
            double c;

            if (yawDiff <= 180.0D) {
                if (s.getYaw() < n.getYaw()) {
                    c = yawDiff;
                } else {
                    c = -yawDiff;
                }
            } else if (s.getYaw() < n.getYaw()) {
                c = -(360.0D - yawDiff);
            } else {
                c = 360.0D - yawDiff;
            }

            double d = c / t;
            for (int x = 0; x < t; x++) {
                Location l = new Location(s.getX() + moveX / t * x, s.getY() + moveY / t * x,
                        s.getZ() + moveZ / t * x, (float) (s.getYaw() + d * x),
                        (float) (s.getPitch() + movePitch / t * x), w);
                tps.add(l);
            }

        }

        try {
            final boolean hadFlight = player.getAllowFlight();
            final boolean wasFlying = player.getAdventureSettings().get(AdventureSettings.Type.FLYING);
            player.setAllowFlight(true);
            player.teleport(tps.get(0));
            player.getAdventureSettings().set(AdventureSettings.Type.FLYING, true);
            CameraStudio.travelling.add(player.getUniqueId());
            CameraStudio.getInstance().getServer().getScheduler().scheduleDelayedTask(CameraStudio.getInstance(), new Runnable() {
                private int ticks = 0;

                public void run() {
                    if (this.ticks < tps.size()) {
                        player.sendPosition(tps.get(this.ticks), tps.get(this.ticks).getYaw(), tps.get(this.ticks).getPitch(), MovePlayerPacket.MODE_RESET);

                        if (!CameraStudio.stopping.contains(player.getUniqueId())) {
                            CameraStudio.getInstance().getServer().getScheduler().scheduleDelayedTask(CameraStudio.getInstance(), this, 1, true);
                        } else {
                            CameraStudio.stopping.remove(player.getUniqueId());
                            CameraStudio.travelling.remove(player.getUniqueId());
                        }

                        this.ticks += 1;
                    } else {
                        CameraStudio.travelling.remove(player.getUniqueId());
                        player.getAdventureSettings().set(AdventureSettings.Type.FLYING, wasFlying);
                        player.setAllowFlight(hadFlight);
                        player.sendMessage(Language.get("travelling.finished"));
                    }
                }
            }, 1, true);
        } catch (Exception e) {
            player.sendMessage(Language.get("travelling-error"));
        }
    }

    public static int parseTimeString(String timeString) throws java.text.ParseException {
        Date length;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("mm'm'ss's'");
            length = formatter.parse(timeString);
        } catch (Exception e) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat("m'm'ss's'");
                length = formatter.parse(timeString);
            } catch (Exception e1) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("m'm's's'");
                    length = formatter.parse(timeString);
                } catch (Exception e2) {
                    try {
                        SimpleDateFormat formatter = new SimpleDateFormat("mm'm's's'");
                        length = formatter.parse(timeString);
                    } catch (Exception e3) {
                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("mm'm'");
                            length = formatter.parse(timeString);
                        } catch (Exception e4) {
                            try {
                                SimpleDateFormat formatter = new SimpleDateFormat("m'm'");
                                length = formatter.parse(timeString);
                            } catch (Exception e5) {
                                try {
                                    SimpleDateFormat formatter = new SimpleDateFormat("s's'");
                                    length = formatter.parse(timeString);
                                } catch (Exception e6) {
                                    SimpleDateFormat formatter = new SimpleDateFormat("ss's'");
                                    length = formatter.parse(timeString);
                                }
                            }
                        }
                    }
                }
            }
        }

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(length);

        return (cal.get(12) * 60 + cal.get(13)) * 20;
    }

    public static double positionDifference(Location cLoc, Location eLoc) {
        double cX = cLoc.getX();
        double cY = cLoc.getY();
        double cZ = cLoc.getZ();

        double eX = eLoc.getX();
        double eY = eLoc.getY();
        double eZ = eLoc.getZ();

        double dX = eX - cX;
        if (dX < 0.0D) {
            dX = -dX;
        }
        double dZ = eZ - cZ;
        if (dZ < 0.0D) {
            dZ = -dZ;
        }
        double dXZ = Math.hypot(dX, dZ);

        double dY = eY - cY;
        if (dY < 0.0D) {
            dY = -dY;
        }

        return Math.hypot(dXZ, dY);
    }

    public static boolean isTravelling(UUID PlayerUUID) {
        return CameraStudio.travelling.contains(PlayerUUID);
    }

    public static void stop(final UUID playerUUID) {
        CameraStudio.stopping.add(playerUUID);
        CameraStudio.getInstance().getServer().getScheduler().scheduleDelayedTask(CameraStudio.getInstance(), () -> CameraStudio.stopping.remove(playerUUID), 2);
    }
}
