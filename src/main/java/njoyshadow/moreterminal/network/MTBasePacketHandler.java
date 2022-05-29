package njoyshadow.moreterminal.network;

import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;
import net.minecraft.network.PacketBuffer;
import njoyshadow.moreterminal.integration.AppEng.sync.JEIExtendedRecipePacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class MTBasePacketHandler {

    public MTBasePacketHandler() {
    }
    private static final Map<Class<? extends BasePacket>, MTBasePacketHandler.MTPacketTypes> REVERSE_LOOKUP = new HashMap();

    public static enum MTPacketTypes {
        EXTENDEDJEI_RECIPE(JEIExtendedRecipePacket.class, JEIExtendedRecipePacket::new);

        private Function<PacketBuffer, BasePacket> factory;

        private MTPacketTypes(Class<? extends BasePacket> packetClass, Function<PacketBuffer, BasePacket> factory) {
            this.factory = factory;
            MTBasePacketHandler.REVERSE_LOOKUP.put(packetClass, this);
        }

        public static MTBasePacketHandler.MTPacketTypes getPacket(int id) {
            return values()[id];
        }

        public int getPacketId() {
            return this.ordinal();
        }

        static MTBasePacketHandler.MTPacketTypes getID(Class<? extends BasePacket> c) {
            return (MTBasePacketHandler.MTPacketTypes) MTBasePacketHandler.REVERSE_LOOKUP.get(c);
        }

        public BasePacket parsePacket(PacketBuffer in) throws IllegalArgumentException {
            return (BasePacket) this.factory.apply(in);
        }
    }
}
