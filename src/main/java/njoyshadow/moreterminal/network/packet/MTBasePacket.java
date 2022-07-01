package njoyshadow.moreterminal.network.packet;

import appeng.core.AEConfig;
import appeng.core.AELog;

import appeng.core.sync.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import njoyshadow.moreterminal.network.handler.MTBasePacketHandler;
import njoyshadow.moreterminal.network.handler.MTNetworkHandler;
import njoyshadow.moreterminal.network.packet.ExtendedCraftingPacket.ExtendedCraftingPacket;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class MTBasePacket {
    private FriendlyByteBuf p;

    public void serverPacketData(ServerPlayer player) {
        throw new UnsupportedOperationException(
                "This packet ( " + this.getPacketID() + " does not implement a server side handler.");
    }

    public final int getPacketID() {
        return MTBasePacketHandler.PacketTypes.getID(this.getClass()).ordinal();
    }

    public void clientPacketData(Player player) {
        throw new UnsupportedOperationException(
                "This packet ( " + this.getPacketID() + " does not implement a client side handler.");
    }

    protected void configureWrite(FriendlyByteBuf data) {
        data.capacity(data.readableBytes());
        this.p = data;
    }

    public Packet<?> toPacket(NetworkDirection direction) {
        if (this.p.array().length > 2 * 1024 * 1024) // 2k walking room :)
        {
            throw new IllegalArgumentException(
                    "Sorry AE2 made a " + this.p.array().length + " byte packet by accident!");
        }

        if (AEConfig.instance().isPacketLogEnabled()) {
            AELog.info(this.getClass().getName() + " : " + p.readableBytes());
        }

        return direction.buildPacket(Pair.of(p, 0), MTNetworkHandler.instance().getChannel()).getThis();
    }
}
