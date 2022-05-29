package njoyshadow.moreterminal.integration.AppEng.sync;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.IPacketHandler;
import appeng.core.sync.network.ServerPacketHandler;
import appeng.core.sync.network.TargetPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;
import njoyshadow.moreterminal.network.MTClientPacketHandler;
import njoyshadow.moreterminal.network.MTServerPacketHandler;

public class MTNetworkHandler{
    private static MTNetworkHandler instance;

    private final ResourceLocation myChannelName;

    private final IPacketHandler clientHandler;
    private final IPacketHandler serverHandler;

    public MTNetworkHandler(ResourceLocation channelName) {
        EventNetworkChannel ec = NetworkRegistry.ChannelBuilder.named(myChannelName = channelName)
                .networkProtocolVersion(() -> "1").clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true)
                .eventNetworkChannel();
        ec.registerObject(this);

        this.clientHandler =  DistExecutor.unsafeRunForDist(() -> MTClientPacketHandler::new, () -> () -> null);
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
            return (IPacketHandler) new MTServerPacketHandler();
        } catch (final Throwable t) {
            return null;
        }
    }

    @SubscribeEvent
    public void serverPacket(final NetworkEvent.ClientCustomPayloadEvent ev) {
        if (this.serverHandler != null) {
            try {
                NetworkEvent.Context ctx = ev.getSource().get();
                ServerPlayNetHandler netHandler = (ServerPlayNetHandler) ctx.getNetworkManager().getNetHandler();
                ctx.setPacketHandled(true);
                ctx.enqueueWork(
                        () -> this.serverHandler.onPacketData(null, netHandler, ev.getPayload(), netHandler.player));

            } catch (final ThreadQuickExitException ignored) {

            }
        }
    }

    @SubscribeEvent
    public void clientPacket(final NetworkEvent.ServerCustomPayloadEvent ev) {
        if (ev instanceof NetworkEvent.ServerCustomPayloadLoginEvent) {
            return;
        }
        if (this.clientHandler != null) {
            try {
                NetworkEvent.Context ctx = ev.getSource().get();
                INetHandler netHandler = ctx.getNetworkManager().getNetHandler();
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> this.clientHandler.onPacketData(null, netHandler, ev.getPayload(), null));
            } catch (final ThreadQuickExitException ignored) {

            }
        }
    }

    public ResourceLocation getChannel() {
        return this.myChannelName;
    }

    public void sendToAll(final MTBasePacket message) {
        getServer().getPlayerList().sendPacketToAllPlayers(message.toPacket(NetworkDirection.PLAY_TO_CLIENT));
    }

    public void sendTo(final MTBasePacket message, final ServerPlayerEntity player) {
        player.connection.sendPacket(message.toPacket(NetworkDirection.PLAY_TO_CLIENT));
    }

    public void sendToAllAround(final MTBasePacket message, final TargetPoint point) {
        IPacket<?> pkt = message.toPacket(NetworkDirection.PLAY_TO_CLIENT);
        getServer().getPlayerList().sendToAllNearExcept(point.excluded, point.x, point.y, point.z, point.r2,
                point.world.getDimensionKey(), pkt);
    }

    public void sendToServer(final MTBasePacket message) {
        Minecraft.getInstance().getConnection().sendPacket(message.toPacket(NetworkDirection.PLAY_TO_SERVER));
    }

    private MinecraftServer getServer() {
        return LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
    }
}