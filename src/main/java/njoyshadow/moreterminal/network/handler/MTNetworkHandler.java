package njoyshadow.moreterminal.network.handler;

import appeng.core.AppEng;
import appeng.core.sync.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import njoyshadow.moreterminal.network.packet.MTBasePacket;

public class MTNetworkHandler {
    private static MTNetworkHandler instance;

    private final ResourceLocation myChannelName;

    private final IPacketHandler clientHandler;
    private final IPacketHandler serverHandler;

    public MTNetworkHandler(final ResourceLocation channelName) {
        EventNetworkChannel ec = NetworkRegistry.ChannelBuilder.named(myChannelName = channelName)
                .networkProtocolVersion(() -> "1").clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true)
                .eventNetworkChannel();
        ec.registerObject(this);

        this.clientHandler = DistExecutor.unsafeRunForDist(() -> MTClientPacketHandler::new, () -> () -> null);
        this.serverHandler = this.createServerSide();
    }

    public static void init(final ResourceLocation channelName) {
        instance = new MTNetworkHandler(channelName);
    }

    public static MTNetworkHandler instance() {
        return instance;
    }

    private IPacketHandler createServerSide() {
        try {
            return new MTServerPacketHandler();
        } catch (final Throwable t) {
            return null;
        }
    }

    @SubscribeEvent
    public void serverPacket(final NetworkEvent.ClientCustomPayloadEvent ev) {
        if (this.serverHandler != null) {
            try {
                NetworkEvent.Context ctx = ev.getSource().get();
                ServerGamePacketListenerImpl netHandler = (ServerGamePacketListenerImpl) ctx.getNetworkManager()
                        .getPacketListener();
                ctx.setPacketHandled(true);
                ctx.enqueueWork(
                        () -> this.serverHandler.onPacketData(netHandler, ev.getPayload(), netHandler.player));

            } catch (final RunningOnDifferentThreadException ignored) {

            }
        }
    }

    @SubscribeEvent
    public void clientPacket(NetworkEvent.ServerCustomPayloadEvent ev) {
        if (ev instanceof NetworkEvent.ServerCustomPayloadLoginEvent) {
            return;
        }
        if (this.clientHandler != null) {
            try {
                NetworkEvent.Context ctx = ev.getSource().get();
                PacketListener netHandler = ctx.getNetworkManager().getPacketListener();
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> this.clientHandler.onPacketData(netHandler, ev.getPayload(), null));
            } catch (RunningOnDifferentThreadException ignored) {

            }
        }
    }

    public ResourceLocation getChannel() {
        return this.myChannelName;
    }

    public void sendToAll(MTBasePacket message) {
        var server = AppEng.instance().getCurrentServer();
        if (server != null) {
            server.getPlayerList().broadcastAll(message.toPacket(NetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public void sendTo(MTBasePacket message, ServerPlayer player) {
        player.connection.send(message.toPacket(NetworkDirection.PLAY_TO_CLIENT));
    }

    public void sendToAllAround(MTBasePacket message, TargetPoint point) {
        var server = AppEng.instance().getCurrentServer();
        if (server != null) {
            Packet<?> pkt = message.toPacket(NetworkDirection.PLAY_TO_CLIENT);
            server.getPlayerList().broadcast(point.excluded, point.x, point.y, point.z, point.r2,
                    point.level.dimension(), pkt);
        }
    }

    public void sendToServer(MTBasePacket message) {
        Minecraft.getInstance().getConnection().send(message.toPacket(NetworkDirection.PLAY_TO_SERVER));
    }
}
