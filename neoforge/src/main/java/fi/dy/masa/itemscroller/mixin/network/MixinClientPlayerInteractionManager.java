package fi.dy.masa.itemscroller.mixin.network;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fi.dy.masa.itemscroller.util.ClickPacketBuffer;
import fi.dy.masa.itemscroller.util.PacketUtils;

// NeoForge版 - patchマッピングにsendPacketの対応が無くrefmapが翻訳されないため、
// 実行時名を文字列で直接指定した差し替え版を置く
// MixinExtrasのWrapOperationが使えないため@Redirectを使用
@Mixin(MultiPlayerGameMode.class)
public class MixinClientPlayerInteractionManager
{
    @Inject(method = "handleInventoryButtonClick", at = @At("HEAD"), cancellable = true)
    private void cancelWindowClicksWhileReplayingBufferedPackets(CallbackInfo ci)
    {
        if (ClickPacketBuffer.shouldCancelWindowClicks())
        {
            ci.cancel();
        }
    }

    @Redirect(method = "handleInventoryButtonClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void bufferClickPacketsAndCancel(ClientPacketListener instance, Packet<?> packet)
    {
        if (ClickPacketBuffer.shouldBufferClickPackets())
        {
            ClickPacketBuffer.bufferPacket(packet);
            return;
        }

        PacketUtils.sendPacket(instance, packet);
    }
}
