package me.confuser.staffactivity.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

@AllArgsConstructor
public class LocationBuilder {
  @Getter
  private double x;
  @Getter
  private double y;
  @Getter
  private double z;
  @Getter
  private float yaw;
  @Getter
  private float pitch;
  @Getter
  private String world;

  public LocationBuilder(Location location) {
    x = location.getX();
    y = location.getY();
    z = location.getZ();
    yaw = location.getYaw();
    pitch = location.getPitch();
    world = location.getWorld().getName();
  }

  public boolean equalsLocation(Location loc) {
    return loc.getX() == getX() && loc.getY() == getY() && loc.getZ() == getZ() && loc.getWorld().getName().equals(world);
  }

  public Location toLocation() {
    return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
  }
}
