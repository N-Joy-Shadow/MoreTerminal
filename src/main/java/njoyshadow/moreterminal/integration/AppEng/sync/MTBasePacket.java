package njoyshadow.moreterminal.integration.AppEng.sync;

import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkDirection;
import njoyshadow.moreterminal.network.MTBasePacketHandler;
import org.apache.commons.lang3.tuple.Pair;

public class MTBasePacket {
    public static final int MAX_STRING_LENGTH = 32767;
    private PacketBuffer p;

    public MTBasePacket() {
    }

    public void serverPacketData(INetworkInfo manager, PlayerEntity player) {
        throw new UnsupportedOperationException("This packet ( " + this.getPacketID() + " does not implement a server side handler.");
    }

    public final int getPacketID() {
        return MTBasePacketHandler.MTPacketTypes.getID(this.getClass()).ordinal();
    }

    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        throw new UnsupportedOperationException("This packet ( " + this.getPacketID() + " does not implement a client side handler.");
    }

    protected void configureWrite(PacketBuffer data) {
        data.capacity(data.readableBytes());
        this.p = data;
    }

    public IPacket<?> toPacket(NetworkDirection direction) {
        if (this.p.array().length > 2097152) {
            throw new IllegalArgumentException("Sorry AE2 made a " + this.p.array().length + " byte packet by accident!");
        } else {
            if (AEConfig.instance().isFeatureEnabled(AEFeature.PACKET_LOGGING)) {
                AELog.info(this.getClass().getName() + " : " + this.p.readableBytes(), new Object[0]);
            }

            return direction.buildPacket(Pair.of(this.p, 0), NetworkHandler.instance().getChannel()).getThis();
        }
    }
}