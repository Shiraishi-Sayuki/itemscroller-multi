package fi.dy.masa.itemscroller.util;

import java.lang.reflect.Method;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;

// NeoForge用パケット送信ユーティリティ - リフレクションでsendを呼ぶ
// patchマッピングにsendPacketの対応が無いため
public class PacketUtils
{
    private static Method sendMethod;

    public static void sendPacket(ClientPacketListener handler, Packet<?> packet)
    {
        try
        {
            if (sendMethod == null)
            {
                sendMethod = ClientPacketListener.class.getDeclaredMethod("send", Packet.class);
                sendMethod.setAccessible(true);
            }
            sendMethod.invoke(handler, packet);
        }
        catch (Exception e)
        {
            fi.dy.masa.itemscroller.ItemScroller.LOGGER.error("Failed to send packet via reflection", e);
        }
    }
}
