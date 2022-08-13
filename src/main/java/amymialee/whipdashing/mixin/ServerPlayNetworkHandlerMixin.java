package amymialee.whipdashing.mixin;

import amymialee.whipdashing.Whipdashing;
import amymialee.whipdashing.items.WhipdashItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getStackInHand(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        ItemStack itemStack = this.player.getStackInHand(Hand.OFF_HAND);
        if (itemStack.getItem() instanceof WhipdashItem) {
            itemStack.getOrCreateNbt().putBoolean("wd:light", !itemStack.getOrCreateNbt().getBoolean("wd:light"));
            player.world.playSound(null, player.getX(), player.getY(), player.getZ(), Whipdashing.WHIPDASH_SWAP, SoundCategory.PLAYERS, 0.1f, 4f);
            ci.cancel();
        }
    }
}