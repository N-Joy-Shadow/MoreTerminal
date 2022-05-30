package njoyshadow.moreterminal.integration.AppEng.sync.Packet;

import appeng.core.AELog;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.IPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;
import njoyshadow.moreterminal.integration.AppEng.sync.MTBasePacket;

public class MTClientPacketHandler extends MTBasePacketHandler implements IPacketHandler {
    public MTClientPacketHandler() {
    }

    public void onPacketData(INetworkInfo manager, INetHandler handler, PacketBuffer packet, PlayerEntity player) {
        try {
            int packetType = packet.readInt();

            //Think..
            MTBasePacket pack = MTPacketTypes.getPacket(packetType).parsePacket(packet);
            pack.clientPacketData(manager, Minecraft.getInstance().player);
        } catch (IllegalArgumentException var7) {
            AELog.debug(var7);
        }

    }
}
