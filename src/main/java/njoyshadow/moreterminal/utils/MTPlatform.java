package njoyshadow.moreterminal.utils;



import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.SortOrder;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IMaterials;
import appeng.api.features.AEFeature;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.cells.ICellInventoryHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.stats.AeStats;
import appeng.hooks.ticking.TickHandler;
import appeng.integration.abstraction.JEIFacade;
import appeng.me.GridAccessException;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.BlockUpdate;
import appeng.util.helpers.ItemComparisonHelper;
import appeng.util.helpers.P2PHelper;
import appeng.util.item.AEItemStack;
import appeng.util.prioritylist.IPartitionList;
import com.blakebr0.extendedcrafting.api.crafting.ITableRecipe;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;

public class MTPlatform{
    public static final int DEF_OFFSET = 16;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final WeakHashMap<World, PlayerEntity> FAKE_PLAYERS = new WeakHashMap();
    private static final ItemComparisonHelper ITEM_COMPARISON_HELPER = new ItemComparisonHelper();
    private static final P2PHelper P2P_HELPER = new P2PHelper();

    public MTPlatform() {
    }

    public static ItemComparisonHelper itemComparisons() {
        return ITEM_COMPARISON_HELPER;
    }

    public static P2PHelper p2p() {
        return P2P_HELPER;
    }

    public static Random getRandom() {
        return RANDOM_GENERATOR;
    }

    public static float getRandomFloat() {
        return RANDOM_GENERATOR.nextFloat();
    }

    public static String formatPowerLong(long n, boolean isRate) {
        return formatPower((double)n / 100.0D, isRate);
    }

    public static String formatPower(double p, boolean isRate) {
        PowerUnits displayUnits = AEConfig.instance().getSelectedPowerUnit();
        p = PowerUnits.AE.convertTo(displayUnits, p);
        String[] preFixes = new String[]{"k", "M", "G", "T", "P", "T", "P", "E", "Z", "Y"};
        String unitName = displayUnits.name();
        String level = "";

        for(int offset = 0; p > 1000.0D && offset < preFixes.length; ++offset) {
            p /= 1000.0D;
            level = preFixes[offset];
        }

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(p) + ' ' + level + unitName + (isRate ? "/t" : "");
    }

    public static Direction crossProduct(Direction forward, Direction up) {
        int west_x = forward.getYOffset() * up.getZOffset() - forward.getZOffset() * up.getYOffset();
        int west_y = forward.getZOffset() * up.getXOffset() - forward.getXOffset() * up.getZOffset();
        int west_z = forward.getXOffset() * up.getYOffset() - forward.getYOffset() * up.getXOffset();
        switch(west_x + west_y * 2 + west_z * 3) {
            case -3:
                return Direction.NORTH;
            case -2:
                return Direction.DOWN;
            case -1:
                return Direction.WEST;
            case 0:
            default:
                return Direction.NORTH;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.UP;
            case 3:
                return Direction.SOUTH;
        }
    }

    public static boolean hasClientClasses() {
        return FMLEnvironment.dist.isClient();
    }

    public static boolean isClient() {
        return Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER;
    }

    public static boolean hasPermissions(DimensionalCoord dc, PlayerEntity player) {
        return !dc.isInWorld(player.world) ? false : player.world.isBlockModifiable(player, dc.getPos());
    }

    public static boolean checkPermissions(PlayerEntity player, Object accessInterface, SecurityPermissions requiredPermission, boolean notifyPlayer) {
        if (requiredPermission != null && accessInterface instanceof IActionHost) {
            IGridNode gn = ((IActionHost)accessInterface).getActionableNode();
            if (gn != null) {
                IGrid g = gn.getGrid();
                if (g != null) {
                    boolean requirePower = false;
                    ISecurityGrid sg = (ISecurityGrid)g.getCache(ISecurityGrid.class);
                    if (!sg.hasPermission(player, requiredPermission)) {
                        player.sendMessage((new TranslationTextComponent("appliedenergistics2.permission_denied")).mergeStyle(TextFormatting.RED), Util.DUMMY_UUID);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static ItemStack[] getBlockDrops(World w, BlockPos pos) {
        if (!(w instanceof ServerWorld)) {
            return new ItemStack[0];
        } else {
            ServerWorld serverWorld = (ServerWorld)w;
            BlockState state = w.getBlockState(pos);
            TileEntity tileEntity = w.getTileEntity(pos);
            List<ItemStack> out = Block.getDrops(state, serverWorld, pos, tileEntity);
            return (ItemStack[])out.toArray(new ItemStack[0]);
        }
    }

    public static void spawnDrops(World w, BlockPos pos, List<ItemStack> drops) {
        if (!w.isRemote()) {
            Iterator var3 = drops.iterator();

            while(var3.hasNext()) {
                ItemStack i = (ItemStack)var3.next();
                if (!i.isEmpty() && i.getCount() > 0) {
                    double offset_x = (double)((getRandomInt() % 32 - 16) / 82);
                    double offset_y = (double)((getRandomInt() % 32 - 16) / 82);
                    double offset_z = (double)((getRandomInt() % 32 - 16) / 82);
                    ItemEntity ei = new ItemEntity(w, 0.5D + offset_x + (double)pos.getX(), 0.5D + offset_y + (double)pos.getY(), 0.2D + offset_z + (double)pos.getZ(), i.copy());
                    w.addEntity(ei);
                }
            }
        }

    }

    public static boolean isServer() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }

    public static void assertServerThread() {
        if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
            throw new UnsupportedOperationException("This code can only be called server-side and this is most likely a bug.");
        }
    }

    public static int getRandomInt() {
        return Math.abs(RANDOM_GENERATOR.nextInt());
    }

    @OnlyIn(Dist.CLIENT)
    public static List<ITextComponent> getTooltip(Object o) {
        if (o == null) {
            return Collections.emptyList();
        } else {
            ItemStack itemStack = ItemStack.EMPTY;
            if (o instanceof AEItemStack) {
                AEItemStack ais = (AEItemStack)o;
                return ais.getToolTip();
            } else if (o instanceof ItemStack) {
                itemStack = (ItemStack)o;

                try {
                    TooltipFlags tooltipFlag = Minecraft.getInstance().gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL;
                    return itemStack.getTooltip(Minecraft.getInstance().player, tooltipFlag);
                } catch (Exception var3) {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    public static String getModId(IAEItemStack is) {
        if (is == null) {
            return "** Null";
        } else {
            String n = ((AEItemStack)is).getModID();
            return n == null ? "** Null" : n;
        }
    }

    public static String getModId(IAEFluidStack fs) {
        if (fs != null && !fs.getFluidStack().isEmpty()) {
            ResourceLocation n = ForgeRegistries.FLUIDS.getKey(fs.getFluidStack().getFluid());
            return n == null ? "** Null" : n.getNamespace();
        } else {
            return "** Null";
        }
    }

    public static String getModName(String modId) {
        return "" + TextFormatting.BLUE + TextFormatting.ITALIC + (String)ModList.get().getModContainerById(modId).map((mc) -> {
            return mc.getModInfo().getDisplayName();
        }).orElse((String) null);
    }

    public static ITextComponent getItemDisplayName(Object o) {
        if (o == null) {
            return new StringTextComponent("** Null");
        } else {
            ItemStack itemStack = ItemStack.EMPTY;
            if (o instanceof AEItemStack) {
                ITextComponent n = ((AEItemStack)o).getDisplayName();
                return (ITextComponent)(n == null ? new StringTextComponent("** Null") : n);
            } else if (o instanceof ItemStack) {
                itemStack = (ItemStack)o;

                try {
                    return itemStack.getDisplayName();
                } catch (Exception var5) {
                    try {
                        return new TranslationTextComponent(itemStack.getTranslationKey());
                    } catch (Exception var4) {
                        return new StringTextComponent("** Exception");
                    }
                }
            } else {
                return new StringTextComponent("**Invalid Object");
            }
        }
    }

    public static ITextComponent getFluidDisplayName(IAEFluidStack o) {
        if (o == null) {
            return new StringTextComponent("** Null");
        } else {
            FluidStack fluidStack = o.getFluidStack();
            return fluidStack.getDisplayName();
        }
    }

    public static boolean isChargeable(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        } else {
            Item it = i.getItem();
            if (it instanceof IAEItemPowerStorage) {
                return ((IAEItemPowerStorage)it).getPowerFlow(i) != AccessRestriction.READ;
            } else {
                return false;
            }
        }
    }

    public static PlayerEntity getPlayer(ServerWorld w) {
        Objects.requireNonNull(w);
        PlayerEntity wrp = (PlayerEntity)FAKE_PLAYERS.get(w);
        if (wrp != null) {
            return wrp;
        } else {
            PlayerEntity p = FakePlayerFactory.getMinecraft(w);
            FAKE_PLAYERS.put(w, p);
            return p;
        }
    }

    @Nullable
    public static <T> T pickRandom(Collection<T> outs) {
        if (outs.isEmpty()) {
            return null;
        } else {
            int index = RANDOM_GENERATOR.nextInt(outs.size());
            return (T) Iterables.get(outs, index, (Object)null);
        }
    }

    public static AEPartLocation rotateAround(AEPartLocation forward, AEPartLocation axis) {
        if (axis != AEPartLocation.INTERNAL && forward != AEPartLocation.INTERNAL) {
            switch(forward) {
                case DOWN:
                    switch(axis) {
                        case DOWN:
                            return forward;
                        case UP:
                            return forward;
                        case NORTH:
                            return AEPartLocation.EAST;
                        case SOUTH:
                            return AEPartLocation.WEST;
                        case EAST:
                            return AEPartLocation.NORTH;
                        case WEST:
                            return AEPartLocation.SOUTH;
                        default:
                            return forward;
                    }
                case UP:
                    switch(axis) {
                        case NORTH:
                            return AEPartLocation.WEST;
                        case SOUTH:
                            return AEPartLocation.EAST;
                        case EAST:
                            return AEPartLocation.SOUTH;
                        case WEST:
                            return AEPartLocation.NORTH;
                        default:
                            return forward;
                    }
                case NORTH:
                    switch(axis) {
                        case DOWN:
                            return AEPartLocation.EAST;
                        case UP:
                            return AEPartLocation.WEST;
                        case NORTH:
                        case SOUTH:
                        default:
                            return forward;
                        case EAST:
                            return AEPartLocation.UP;
                        case WEST:
                            return AEPartLocation.DOWN;
                    }
                case SOUTH:
                    switch(axis) {
                        case DOWN:
                            return AEPartLocation.WEST;
                        case UP:
                            return AEPartLocation.EAST;
                        case NORTH:
                        case SOUTH:
                        default:
                            return forward;
                        case EAST:
                            return AEPartLocation.DOWN;
                        case WEST:
                            return AEPartLocation.UP;
                    }
                case EAST:
                    switch(axis) {
                        case DOWN:
                            return AEPartLocation.SOUTH;
                        case UP:
                            return AEPartLocation.NORTH;
                        case NORTH:
                            return AEPartLocation.UP;
                        case SOUTH:
                            return AEPartLocation.DOWN;
                    }
                case WEST:
                    switch(axis) {
                        case DOWN:
                            return AEPartLocation.NORTH;
                        case UP:
                            return AEPartLocation.SOUTH;
                        case NORTH:
                            return AEPartLocation.DOWN;
                        case SOUTH:
                            return AEPartLocation.UP;
                    }
            }

            return forward;
        } else {
            return forward;
        }
    }

    public static Direction rotateAround(Direction forward, Direction axis) {
        switch(forward) {
            case DOWN:
                switch(axis) {
                    case DOWN:
                        return forward;
                    case UP:
                        return forward;
                    case NORTH:
                        return Direction.EAST;
                    case SOUTH:
                        return Direction.WEST;
                    case EAST:
                        return Direction.NORTH;
                    case WEST:
                        return Direction.SOUTH;
                    default:
                        return forward;
                }
            case UP:
                switch(axis) {
                    case NORTH:
                        return Direction.WEST;
                    case SOUTH:
                        return Direction.EAST;
                    case EAST:
                        return Direction.SOUTH;
                    case WEST:
                        return Direction.NORTH;
                    default:
                        return forward;
                }
            case NORTH:
                switch(axis) {
                    case DOWN:
                        return Direction.EAST;
                    case UP:
                        return Direction.WEST;
                    case NORTH:
                    case SOUTH:
                    default:
                        return forward;
                    case EAST:
                        return Direction.UP;
                    case WEST:
                        return Direction.DOWN;
                }
            case SOUTH:
                switch(axis) {
                    case DOWN:
                        return Direction.WEST;
                    case UP:
                        return Direction.EAST;
                    case NORTH:
                    case SOUTH:
                    default:
                        return forward;
                    case EAST:
                        return Direction.DOWN;
                    case WEST:
                        return Direction.UP;
                }
            case EAST:
                switch(axis) {
                    case DOWN:
                        return Direction.SOUTH;
                    case UP:
                        return Direction.NORTH;
                    case NORTH:
                        return Direction.UP;
                    case SOUTH:
                        return Direction.DOWN;
                }
            case WEST:
                switch(axis) {
                    case DOWN:
                        return Direction.NORTH;
                    case UP:
                        return Direction.SOUTH;
                    case NORTH:
                        return Direction.DOWN;
                    case SOUTH:
                        return Direction.UP;
                }
        }

        return forward;
    }

    public static <T extends IAEStack<T>> T poweredExtraction(IEnergySource energy, IMEInventory<T> cell, T request, IActionSource src) {
        return poweredExtraction(energy, cell, request, src, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T poweredExtraction(IEnergySource energy, IMEInventory<T> cell, T request, IActionSource src, Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(cell);
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);
        T possible = cell.extractItems(request.copy(), Actionable.SIMULATE, src);
        long retrieved = 0L;
        if (possible != null) {
            retrieved = possible.getStackSize();
        }

        double energyFactor = Math.max(1.0D, (double)cell.getChannel().transferFactor());
        double availablePower = energy.extractAEPower((double)retrieved / energyFactor, Actionable.SIMULATE, PowerMultiplier.CONFIG);
        long itemToExtract = Math.min((long)(availablePower * energyFactor + 0.9D), retrieved);
        if (itemToExtract > 0L) {
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower((double)retrieved / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                possible.setStackSize(itemToExtract);
                T ret = cell.extractItems(possible, Actionable.MODULATE, src);
                if (ret != null) {
                    src.player().ifPresent((player) -> {
                        AeStats.ItemsExtracted.addToPlayer(player, (int)ret.getStackSize());
                    });
                }

                return ret;
            } else {
                return possible.setStackSize(itemToExtract);
            }
        } else {
            return null;
        }
    }

    public static <T extends IAEStack<T>> T poweredInsert(IEnergySource energy, IMEInventory<T> cell, T input, IActionSource src) {
        return poweredInsert(energy, cell, input, src, Actionable.MODULATE);
    }

    public static <T extends IAEStack<T>> T poweredInsert(IEnergySource energy, IMEInventory<T> cell, T input, IActionSource src, Actionable mode) {
        Preconditions.checkNotNull(energy);
        Preconditions.checkNotNull(cell);
        Preconditions.checkNotNull(input);
        Preconditions.checkNotNull(src);
        Preconditions.checkNotNull(mode);
        T overflow = cell.injectItems(input.copy(), Actionable.SIMULATE, src);
        long transferAmount = input.getStackSize();
        if (overflow != null) {
            transferAmount -= overflow.getStackSize();
        }

        double energyFactor = Math.max(1.0D, (double)cell.getChannel().transferFactor());
        double availablePower = energy.extractAEPower((double)transferAmount / energyFactor, Actionable.SIMULATE, PowerMultiplier.CONFIG);
        long itemToAdd = Math.min((long)(availablePower * energyFactor + 0.9D), transferAmount);
        if (itemToAdd > 0L) {
            IAEStack ret;
            if (mode == Actionable.MODULATE) {
                energy.extractAEPower((double)transferAmount / energyFactor, Actionable.MODULATE, PowerMultiplier.CONFIG);
                if (itemToAdd < input.getStackSize()) {
                    long original = input.getStackSize();
                    T leftover = input.copy();
                    T split = input.copy();
                    leftover.decStackSize(itemToAdd);
                    split.setStackSize(itemToAdd);
                    leftover.add(cell.injectItems(split, Actionable.MODULATE, src));
                    src.player().ifPresent((player) -> {
                        long diff = original - leftover.getStackSize();
                        AeStats.ItemsInserted.addToPlayer(player, (int)diff);
                    });
                    return leftover;
                } else {
                    ret = cell.injectItems(input, Actionable.MODULATE, src);
                    src.player().ifPresent((player) -> {
                        long diff = ret == null ? input.getStackSize() : input.getStackSize() - ret.getStackSize();
                        AeStats.ItemsInserted.addToPlayer(player, (int)diff);
                    });
                    return (T) ret;
                }
            } else {
                ret = input.copy().setStackSize(input.getStackSize() - itemToAdd);
                return ret != null && ret.getStackSize() > 0L ? (T) ret : null;
            }
        } else {
            return input;
        }
    }

    public static void postChanges(IStorageGrid gs, ItemStack removed, ItemStack added, IActionSource src) {
        IStorageChannel chan;
        IItemList myChanges;
        for(Iterator var4 = Api.instance().storage().storageChannels().iterator(); var4.hasNext(); gs.postAlterationOfStoredItems(chan, myChanges, src)) {
            chan = (IStorageChannel)var4.next();
            myChanges = chan.createList();
            ICellInventoryHandler myInv;
            if (!removed.isEmpty()) {
                myInv = Api.instance().registries().cell().getCellInventory(removed, (ISaveProvider)null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                    Iterator var8 = myChanges.iterator();

                    while(var8.hasNext()) {
                        IAEStack is = (IAEStack)var8.next();
                        is.setStackSize(-is.getStackSize());
                    }
                }
            }

            if (!added.isEmpty()) {
                myInv = Api.instance().registries().cell().getCellInventory(added, (ISaveProvider)null, chan);
                if (myInv != null) {
                    myInv.getAvailableItems(myChanges);
                }
            }
        }

    }

    public static <T extends IAEStack<T>> void postListChanges(IItemList<T> before, IItemList<T> after, IMEMonitorHandlerReceiver<T> meMonitorPassthrough, IActionSource source) {
        List<T> changes = new ArrayList();
        Iterator var5 = before.iterator();

        IAEStack is;
        while(var5.hasNext()) {
            is = (IAEStack)var5.next();
            is.setStackSize(-is.getStackSize());
        }

        var5 = after.iterator();

        while(var5.hasNext()) {
            is = (IAEStack)var5.next();
            before.add((T) is);
        }

        var5 = before.iterator();

        while(var5.hasNext()) {
            is = (IAEStack)var5.next();
            if (is.getStackSize() != 0L) {
                changes.add((T) is);
            }
        }

        if (!changes.isEmpty()) {
            meMonitorPassthrough.postChange((IBaseMonitor)null, changes, source);
        }

    }

    public static boolean securityCheck(GridNode a, GridNode b) {
        if ((a.getLastSecurityKey() != -1L || b.getLastSecurityKey() != -1L) && a.getLastSecurityKey() != b.getLastSecurityKey()) {
            boolean a_isSecure = isPowered(a.getGrid()) && a.getLastSecurityKey() != -1L;
            boolean b_isSecure = isPowered(b.getGrid()) && b.getLastSecurityKey() != -1L;
            if (AEConfig.instance().isFeatureEnabled(AEFeature.LOG_SECURITY_AUDITS)) {
                AELog.info("Audit: Node A [isSecure=%b, key=%d, playerID=%d, location={%s}] vs Node B[isSecure=%b, key=%d, playerID=%d, location={%s}]", new Object[]{a_isSecure, a.getLastSecurityKey(), a.getPlayerID(), locationA, b_isSecure, b.getLastSecurityKey(), b.getPlayerID(), locationB});
            }

            if (a_isSecure && b_isSecure) {
                return false;
            } else if (!a_isSecure && b_isSecure) {
                return checkPlayerPermissions(b.getGrid(), a.getPlayerID());
            } else {
                return a_isSecure && !b_isSecure ? checkPlayerPermissions(a.getGrid(), b.getPlayerID()) : true;
            }
        } else {
            return true;
        }
    }

    private static boolean isPowered(IGrid grid) {
        if (grid == null) {
            return false;
        } else {
            IEnergyGrid eg = (IEnergyGrid)grid.getCache(IEnergyGrid.class);
            return eg.isNetworkPowered();
        }
    }

    private static boolean checkPlayerPermissions(IGrid grid, int playerID) {
        if (grid == null) {
            return true;
        } else {
            ISecurityGrid gs = (ISecurityGrid)grid.getCache(ISecurityGrid.class);
            if (gs == null) {
                return true;
            } else {
                return !gs.isAvailable() ? true : gs.hasPermission(playerID, SecurityPermissions.BUILD);
            }
        }
    }

    public static void configurePlayer(PlayerEntity player, AEPartLocation side, TileEntity tile) {
        float pitch = 0.0F;
        float yaw = 0.0F;
        switch(side) {
            case DOWN:
                pitch = 90.0F;
                break;
            case UP:
                pitch = 90.0F;
                break;
            case NORTH:
                yaw = 180.0F;
                break;
            case SOUTH:
                yaw = 0.0F;
                break;
            case EAST:
                yaw = -90.0F;
                break;
            case WEST:
                yaw = 90.0F;
            case INTERNAL:
        }

        player.setLocationAndAngles((double)tile.getPos().getX() + 0.5D, (double)tile.getPos().getY() + 0.5D, (double)tile.getPos().getZ() + 0.5D, yaw, pitch);
    }

    public static boolean canAccess(AENetworkProxy gridProxy, IActionSource src) {
        try {
            if (src.player().isPresent()) {
                return gridProxy.getSecurity().hasPermission((PlayerEntity)src.player().get(), SecurityPermissions.BUILD);
            } else if (src.machine().isPresent()) {
                IActionHost te = (IActionHost)src.machine().get();
                IGridNode n = te.getActionableNode();
                if (n == null) {
                    return false;
                } else {
                    int playerID = n.getPlayerID();
                    return gridProxy.getSecurity().hasPermission(playerID, SecurityPermissions.BUILD);
                }
            } else {
                return false;
            }
        } catch (GridAccessException var5) {
            return false;
        }
    }

    public static ItemStack extractItemsByRecipe(IEnergySource energySrc, IActionSource mySrc, IMEMonitor<IAEItemStack> src, World w, ITableRecipe Recipe, ItemStack output, IInventory ci, ItemStack providedTemplate, int slot, IItemList<IAEItemStack> items, Actionable realForFake, IPartitionList<IAEItemStack> filter) {
        if (energySrc.extractAEPower(1.0D, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.9D) {
            if (providedTemplate == null) {
                return ItemStack.EMPTY;
            }

            AEItemStack ae_req = AEItemStack.fromItemStack(providedTemplate);
            ae_req.setStackSize(1L);
            if (filter == null || filter.isListed(ae_req)) {
                IAEItemStack ae_ext = (IAEItemStack)src.extractItems(ae_req, realForFake, mySrc);
                if (ae_ext != null) {
                    ItemStack extracted = ae_ext.createItemStack();
                    if (!extracted.isEmpty()) {
                        energySrc.extractAEPower(1.0D, realForFake, PowerMultiplier.CONFIG);
                        return extracted;
                    }
                }
            }

            boolean checkFuzzy = providedTemplate.hasTag() || providedTemplate.isDamageable();
            if (items != null && checkFuzzy) {
                Iterator var21 = items.iterator();

                while(true) {
                    IAEItemStack x;
                    ItemStack sh;
                    do {
                        do {
                            if (!var21.hasNext()) {
                                return ItemStack.EMPTY;
                            }

                            x = (IAEItemStack)var21.next();
                            sh = x.getDefinition();
                        } while(!itemComparisons().isEqualItemType(providedTemplate, sh));
                    } while(ItemStack.areItemsEqual(sh, output));

                    ItemStack cp = sh.copy();
                    cp.setCount(1);
                    ci.setInventorySlotContents(slot, cp);
                    if (Recipe.matches(ci, w) && ItemStack.areItemsEqual(Recipe.getCraftingResult(ci), output)) {
                        IAEItemStack ax = x.copy();
                        ax.setStackSize(1L);
                        if (filter == null || filter.isListed(ax)) {
                            IAEItemStack ex = (IAEItemStack)src.extractItems(ax, realForFake, mySrc);
                            if (ex != null) {
                                energySrc.extractAEPower(1.0D, realForFake, PowerMultiplier.CONFIG);
                                return ex.createItemStack();
                            }
                        }
                    }

                    ci.setInventorySlotContents(slot, providedTemplate);
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack getContainerItem(ItemStack stackInSlot) {
        if (stackInSlot == null) {
            return ItemStack.EMPTY;
        } else {
            Item i = stackInSlot.getItem();
            if (i != null && i.hasContainerItem(stackInSlot)) {
                ItemStack ci = i.getContainerItem(stackInSlot.copy());
                if (!ci.isEmpty() && ci.isDamageable() && ci.getDamage() == ci.getMaxDamage()) {
                    ci = ItemStack.EMPTY;
                }

                return ci;
            } else if (stackInSlot.getCount() > 1) {
                stackInSlot.setCount(stackInSlot.getCount() - 1);
                return stackInSlot;
            } else {
                return ItemStack.EMPTY;
            }
        }
    }

    public static void notifyBlocksOfNeighbors(World world, BlockPos pos) {
        if (!world.isRemote) {
         //   TickHandler.instance().addCallable(world, new BlockUpdate(pos));
            System.out.println("Test notidyblocksofneighbors");
        }

    }

    public static boolean canRepair(AEFeature type, ItemStack a, ItemStack b) {
        if (!b.isEmpty() && !a.isEmpty()) {
            if (type == AEFeature.CERTUS_QUARTZ_TOOLS) {
                IItemDefinition certusQuartzCrystal = Api.instance().definitions().materials().certusQuartzCrystal();
                return certusQuartzCrystal.isSameAs(b);
            } else if (type == AEFeature.NETHER_QUARTZ_TOOLS) {
                return Items.QUARTZ == b.getItem();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isRecipePrioritized(ItemStack what) {
        IMaterials materials = Api.instance().definitions().materials();
        boolean isPurified = materials.purifiedCertusQuartzCrystal().isSameAs(what);
        isPurified |= materials.purifiedFluixCrystal().isSameAs(what);
        isPurified |= materials.purifiedNetherQuartzCrystal().isSameAs(what);
        return isPurified;
    }

    public static boolean isSortOrderAvailable(SortOrder order) {
        return true;
    }

    public static boolean isSearchModeAvailable(SearchBoxMode mode) {
        return mode.isRequiresJei() ? JEIFacade.instance().isEnabled() : true;
    }
}

