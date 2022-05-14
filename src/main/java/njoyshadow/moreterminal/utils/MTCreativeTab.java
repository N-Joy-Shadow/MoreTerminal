package njoyshadow.moreterminal.utils;

import appeng.core.AEItemGroup;

public class MTCreativeTab {
    public static MTItemGroup INSTANCE;

    public MTCreativeTab() {
    }

    public static void init() {
        INSTANCE = new MTItemGroup("moreterminal");
    }
}
