package njoyshadow.moreterminal.network.handler;

import appeng.core.sync.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import njoyshadow.moreterminal.network.packet.ExtendedCraftingPacket.ExtendedCraftingPacket;
import njoyshadow.moreterminal.network.packet.MTBasePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MTBasePacketHandler {
    private static final Map<Class<? extends MTBasePacket>, PacketTypes> REVERSE_LOOKUP = new HashMap<>();


    public enum PacketTypes{
        EXTENDED_CRAFTING(ExtendedCraftingPacket.class,ExtendedCraftingPacket::new);

        private final Function<FriendlyByteBuf, MTBasePacket> factory;

        PacketTypes(Class<? extends MTBasePacket> packetClass, Function<FriendlyByteBuf, MTBasePacket> factory) {
            this.factory = factory;

            REVERSE_LOOKUP.put(packetClass, this);
        }

        public static PacketTypes getPacket(int id) {
            return values()[id];
        }

        public int getPacketId() {
            return ordinal();
        }

        public static PacketTypes getID(Class<? extends MTBasePacket> c) {
            return REVERSE_LOOKUP.get(c);
        }

        public MTBasePacket parsePacket(FriendlyByteBuf in) throws IllegalArgumentException {
            return this.factory.apply(in);
        }
    }
}
