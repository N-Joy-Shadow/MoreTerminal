package njoyshadow.moreterminal.network;

import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.IPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

public class ServerPacketHandler extends MTBasePacketHandler implements IPacketHandler {
    public ServerPacketHandler() {
    }

    public void onPacketData(INetworkInfo manager, INetHandler handler, PacketBuffer packet, PlayerEntity player) {
        try {
            int packetType = packet.readInt();
            BasePacket pack = MTPacketTypes.getPacket(packetType).parsePacket(packet);
            pack.serverPacketData(manager, player);
        } catch (IllegalArgumentException var7) {
            AELog.warn(var7);
        }

    }
}
