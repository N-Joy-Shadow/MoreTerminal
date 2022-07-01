package njoyshadow.moreterminal.network.handler;

import appeng.core.AELog;
import appeng.core.sync.network.IPacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.world.entity.player.Player;
import njoyshadow.moreterminal.network.packet.MTBasePacket;

public class MTClientPacketHandler extends MTBasePacketHandler implements IPacketHandler {

    @Override
    public void onPacketData(final PacketListener handler, final FriendlyByteBuf packet, final Player player) {
        try {
            final int packetType = packet.readInt();
            final MTBasePacket pack = PacketTypes.getPacket(packetType).parsePacket(packet);
            pack.clientPacketData(Minecraft.getInstance().player);
        } catch (final IllegalArgumentException e) {
            AELog.debug(e);
        }
    }
}