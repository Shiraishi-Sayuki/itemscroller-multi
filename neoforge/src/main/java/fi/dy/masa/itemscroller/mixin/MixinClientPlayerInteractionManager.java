package fi.dy.masa.itemscroller.mixin;

import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.network.packet.Packet;
import fi.dy.masa.itemscroller.util.ClickPacketBuffer;
import fi.dy.masa.itemscroller.util.PacketUtils;

// NeoForge版 - patchマッピングにsendPacketの対応が無くrefmapが翻訳されないため、
// 実行時名(handleInventoryMouseClick/send)を文字列で直接指定した差し替え版を置く
// 文字列はrefmapを通らないのでそのままランタイム名で一致する、送信はリフレクションで行う
@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager
{
    @Inject(method = "handleInventoryMouseClick", at = @At("HEAD"), cancellable = true)
    private void cancelWindowClicksWhileReplayingBufferedPackets(CallbackInfo ci)
    {
        if (ClickPacketBuffer.shouldCancelWindowClicks())
        {
            ci.cancel();
        }
    }

    @Redirect(method = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleInventoryMouseClick(IIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void bufferClickPacketsAndCancel(ClientPlayNetworkHandler netHandler, Packet<?> packet)
    {
        if (packet instanceof ClickSlotC2SPacket)
        {
            // デバッグ用 - 必要になったら有効化する
        }

        if (ClickPacketBuffer.shouldBufferClickPackets())
        {
            ClickPacketBuffer.bufferPacket(packet);
            return;
        }

        PacketUtils.sendPacket(netHandler, packet);
    }
}
