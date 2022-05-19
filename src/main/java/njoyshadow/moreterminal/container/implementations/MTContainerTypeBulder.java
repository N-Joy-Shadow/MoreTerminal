package njoyshadow.moreterminal.container.implementations;

import appeng.api.config.SecurityPermissions;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;
import com.google.common.base.Preconditions;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.network.NetworkHooks;
import njoyshadow.moreterminal.Moreterminal;

import javax.annotation.Nullable;
import java.util.function.Function;

public class MTContainerTypeBulder <C extends AEBaseContainer, I> {
    private final Class<I> hostInterface;
    private final MTContainerTypeBulder.ContainerFactory<C, I> factory;
    private Function<I, ITextComponent> containerTitleStrategy = this::getDefaultContainerTitle;
    @Nullable
    private SecurityPermissions requiredPermission;
    @Nullable
    private MTContainerTypeBulder.InitialDataSerializer<I> initialDataSerializer;
    @Nullable
    private MTContainerTypeBulder.InitialDataDeserializer<C, I> initialDataDeserializer;
    private ContainerType<C> containerType;

    private MTContainerTypeBulder(Class<I> hostInterface, MTContainerTypeBulder.TypedContainerFactory<C, I> typedFactory) {
        this.hostInterface = hostInterface;
        this.factory = (windowId, playerInv, accessObj) -> {
            return (C) typedFactory.create(this.containerType, windowId, playerInv, accessObj);
        };
    }

    private MTContainerTypeBulder(Class<I> hostInterface, MTContainerTypeBulder.ContainerFactory<C, I> factory) {
        this.hostInterface = hostInterface;
        this.factory = factory;
    }

    public static <C extends AEBaseContainer, I> MTContainerTypeBulder<C, I> create(MTContainerTypeBulder.ContainerFactory<C, I> factory, Class<I> hostInterface) {
        return new MTContainerTypeBulder(hostInterface, factory);
    }

    public static <C extends AEBaseContainer, I> MTContainerTypeBulder<C, I> create(MTContainerTypeBulder.TypedContainerFactory<C, I> factory, Class<I> hostInterface) {
        return new MTContainerTypeBulder(hostInterface, factory);
    }

    public MTContainerTypeBulder<C, I> requirePermission(SecurityPermissions permission) {
        this.requiredPermission = permission;
        return this;
    }

    public MTContainerTypeBulder<C, I> withContainerTitle(Function<I, ITextComponent> containerTitleStrategy) {
        this.containerTitleStrategy = containerTitleStrategy;
        return this;
    }

    public MTContainerTypeBulder<C, I> withInitialData(MTContainerTypeBulder.InitialDataSerializer<I> initialDataSerializer, MTContainerTypeBulder.InitialDataDeserializer<C, I> initialDataDeserializer) {
        this.initialDataSerializer = initialDataSerializer;
        this.initialDataDeserializer = initialDataDeserializer;
        return this;
    }

    private C fromNetwork(int windowId, PlayerInventory inv, PacketBuffer packetBuf) {
        I host = this.getHostFromLocator(inv.player, ContainerLocator.read(packetBuf));
        if (host != null) {
            C container = (C) this.factory.create(windowId, inv, host);
            if (this.initialDataDeserializer != null) {
                this.initialDataDeserializer.deserializeInitialData(host, container, packetBuf);
            }

            return container;
        } else {
            return null;
        }
    }

    private boolean open(PlayerEntity player, ContainerLocator locator) {
        if (!(player instanceof ServerPlayerEntity)) {
            return false;
        } else {
            I accessInterface = this.getHostFromLocator(player, locator);
            if (accessInterface == null) {
                return false;
            } else if (!this.checkPermission(player, accessInterface)) {
                return false;
            } else {
                ITextComponent title = (ITextComponent)this.containerTitleStrategy.apply(accessInterface);
                INamedContainerProvider container = new SimpleNamedContainerProvider((wnd, p, pl) -> {
                    C c = (C)this.factory.create(wnd, p, accessInterface);
                    c.setLocator(locator);
                    return c;
                }, title);
                NetworkHooks.openGui((ServerPlayerEntity)player, container, (buffer) -> {
                    locator.write(buffer);
                    if (this.initialDataSerializer != null) {
                        this.initialDataSerializer.serializeInitialData(accessInterface, buffer);
                    }

                });
                return true;
            }
        }
    }

    private I getHostFromLocator(PlayerEntity player, ContainerLocator locator) {
        if (locator.hasItemIndex()) {
            return this.getHostFromPlayerInventory(player, locator);
        } else if (!locator.hasBlockPos()) {
            return null;
        } else {
            TileEntity tileEntity = player.world.getTileEntity(locator.getBlockPos());
            if (this.hostInterface.isInstance(tileEntity)) {
                return this.hostInterface.cast(tileEntity);
            } else if (!locator.hasSide()) {
                return null;
            } else if (tileEntity instanceof IPartHost) {
                IPartHost partHost = (IPartHost)tileEntity;
                IPart part = partHost.getPart(locator.getSide());
                if (part == null) {
                    return null;
                } else if (this.hostInterface.isInstance(part)) {
                    return this.hostInterface.cast(part);
                } else {
                    AELog.debug("Trying to open a container @ %s for a %s, but the container requires %s", new Object[]{locator, part.getClass(), this.hostInterface});
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private I getHostFromPlayerInventory(PlayerEntity player, ContainerLocator locator) {
        ItemStack it = player.inventory.getStackInSlot(locator.getItemIndex());
        if (it.isEmpty()) {
            AELog.debug("Cannot open container for player %s since they no longer hold the item in slot %d", new Object[]{player, locator.hasItemIndex()});
            return null;
        } else {
            if (it.getItem() instanceof IGuiItem) {
                IGuiItem guiItem = (IGuiItem)it.getItem();
                BlockPos blockPos = locator.hasBlockPos() ? locator.getBlockPos() : null;
                IGuiItemObject guiObject = guiItem.getGuiObject(it, locator.getItemIndex(), player.world, blockPos);
                if (this.hostInterface.isInstance(guiObject)) {
                    return this.hostInterface.cast(guiObject);
                }
            }

            if (this.hostInterface.isAssignableFrom(WirelessTerminalGuiObject.class)) {
                IWirelessTermHandler wh = Api.instance().registries().wireless().getWirelessTerminalHandler(it);
                if (wh != null) {
                    return this.hostInterface.cast(new WirelessTerminalGuiObject(wh, it, player, locator.getItemIndex()));
                }
            }

            return null;
        }
    }

    public ContainerType<C> build(String id) {
        Preconditions.checkState(this.containerType == null, "build was already called");
        this.containerType = IForgeContainerType.create(this::fromNetwork);
        this.containerType.setRegistryName(Moreterminal.MOD_ID, id);
        ContainerOpener.addOpener(this.containerType, this::open);
        return this.containerType;
    }

    private boolean checkPermission(PlayerEntity player, Object accessInterface) {
        return this.requiredPermission != null ? Platform.checkPermissions(player, accessInterface, this.requiredPermission, true) : true;
    }

    private ITextComponent getDefaultContainerTitle(I accessInterface) {
        if (accessInterface instanceof ICustomNameObject) {
            ICustomNameObject customNameObject = (ICustomNameObject)accessInterface;
            if (customNameObject.hasCustomInventoryName()) {
                return customNameObject.getCustomInventoryName();
            }
        }

        return StringTextComponent.EMPTY;
    }

    @FunctionalInterface
    public interface InitialDataDeserializer<C, I> {
        void deserializeInitialData(I var1, C var2, PacketBuffer var3);
    }

    @FunctionalInterface
    public interface InitialDataSerializer<I> {
        void serializeInitialData(I var1, PacketBuffer var2);
    }

    @FunctionalInterface
    public interface TypedContainerFactory<C extends Container, I> {
        C create(ContainerType<C> var1, int var2, PlayerInventory var3, I var4);
    }

    @FunctionalInterface
    public interface ContainerFactory<C, I> {
        C create(int var1, PlayerInventory var2, I var3);
    }
}