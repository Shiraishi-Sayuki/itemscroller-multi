package fi.dy.masa.itemscroller.util;

import fi.dy.masa.itemscroller.ItemScroller;
import javax.annotation.Nullable;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;

// パケット送信ヘルパー - ビルド時のリマップで両ローダー対応するので直接呼びでよい
// (旧NeoForge用のリフレクションは1.20.1では不要かつSRG環境で失敗する)
public class PacketUtils
{
    public static boolean sendPacket(@Nullable ClientPlayNetworkHandler networkHandler, Packet<?> packet)
    {
        if (networkHandler == null)
        {
            return false;
        }

        try
        {
            networkHandler.sendPacket(packet);
            return true;
        }
        catch (Exception e)
        {
            ItemScroller.LOGGER.warn("PacketUtils#sendPacket: failed to send packet [{}]", packet.getClass().getName(), e);
        }

        return false;
    }

    // インスタンス化させない - staticだけのクラスだから
    private PacketUtils() {}
}
