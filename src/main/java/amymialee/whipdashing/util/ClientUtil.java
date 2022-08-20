package amymialee.whipdashing.util;

import amymialee.whipdashing.Whipdashing;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class ClientUtil {
    public static void whipJump() {
        ClientPlayNetworking.send(Whipdashing.whipdashJump, PacketByteBufs.empty());
    }
}