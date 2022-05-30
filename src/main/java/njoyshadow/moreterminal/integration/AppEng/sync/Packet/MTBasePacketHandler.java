package njoyshadow.moreterminal.integration.AppEng.sync.Packet;

import net.minecraft.network.PacketBuffer;
import njoyshadow.moreterminal.integration.AppEng.sync.JEIExtendedRecipePacket;
import njoyshadow.moreterminal.integration.AppEng.sync.MTBasePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MTBasePacketHandler {

    public MTBasePacketHandler() {
    }
    private static final Map<Class<? extends MTBasePacket>, MTBasePacketHandler.MTPacketTypes> REVERSE_LOOKUP = new HashMap();

    public static enum MTPacketTypes {
        EXTENDEDJEI_RECIPE(JEIExtendedRecipePacket.class, JEIExtendedRecipePacket::new);

        private Function<PacketBuffer, MTBasePacket> factory;

        private MTPacketTypes(Class<? extends MTBasePacket> packetClass, Function<PacketBuffer, MTBasePacket> factory) {
            this.factory = factory;
            MTBasePacketHandler.REVERSE_LOOKUP.put(packetClass, this);
        }

        public static MTBasePacketHandler.MTPacketTypes getPacket(int id) {
            return values()[id];
        }

        public int getPacketId() {
            return this.ordinal();
        }

        public static MTBasePacketHandler.MTPacketTypes getID(Class<? extends MTBasePacket> c) {
            return (MTBasePacketHandler.MTPacketTypes) MTBasePacketHandler.REVERSE_LOOKUP.get(c);
        }

        public MTBasePacket parsePacket(PacketBuffer in) throws IllegalArgumentException {
            return (MTBasePacket) this.factory.apply(in);
        }
    }
}
