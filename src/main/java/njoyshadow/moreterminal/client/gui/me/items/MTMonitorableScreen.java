package njoyshadow.moreterminal.client.gui.me.items;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import appeng.api.config.*;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.ActionKey;
import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.Repo;
import appeng.client.gui.me.items.RepoSlot;
import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.AETextField;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.Scrollbar;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.container.SlotSemantic;
import appeng.container.me.common.GridInventoryEntry;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInteractionPacket;
import appeng.core.sync.packets.SwitchGuisPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.abstraction.JEIFacade;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.prioritylist.IPartitionList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.InputMappings.Input;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import njoyshadow.moreterminal.client.gui.me.style.MTScreenStyle;
import njoyshadow.moreterminal.client.gui.me.style.MTTerminalStyle;
import njoyshadow.moreterminal.client.gui.me.style.utils.MTBlitter;
import njoyshadow.moreterminal.client.gui.widget.MTScrollbar;
import njoyshadow.moreterminal.client.gui.widget.MTUpgradePanel;
import njoyshadow.moreterminal.utils.MTPlatform;

public abstract class MTMonitorableScreen<T extends IAEStack<T>, C extends MEMonitorableContainer<T>> extends MTBaseScreen<C> implements ISortSource, IConfigManagerHost {
    private static final int MIN_ROWS = 3;
    private static String memoryText = "";
    private final MTTerminalStyle style;
    protected final Repo<T> repo;
    private final List<ItemStack> currentViewCells = new ArrayList();
    private final IConfigManager configSrc;
    private final boolean supportsViewCells;
    private TabButton craftingStatusBtn;
    private AETextField searchField;
    private int rows = 0;
    private SettingToggleButton<ViewItems> viewModeToggle;
    private SettingToggleButton<SortOrder> sortByToggle;
    private final SettingToggleButton<SortDir> sortDirToggle;
    private boolean isAutoFocus = false;
    private int currentMouseX = 0;
    private int currentMouseY = 0;
    private final MTScrollbar scrollbar;

    public MTMonitorableScreen(C container, PlayerInventory playerInventory, ITextComponent title, MTScreenStyle style) {
        super(container, playerInventory, title, style);
        this.style = style.getTerminalStyle();
        if (this.style == null) {
            throw new IllegalStateException("Cannot construct screen " + this.getClass() + " without a terminalStyles setting");
        } else {
            this.scrollbar = this.widgets.addScrollBar("scrollbar");
            this.repo = this.createRepo(this.scrollbar);
            container.setClientRepo(this.repo);
            this.repo.setUpdateViewListener(this::updateScrollbar);
            this.updateScrollbar();
            this.xSize = this.style.getScreenWidth();
            this.ySize = this.style.getScreenHeight(0);
            this.configSrc = ((IConfigurableObject)this.container).getConfigManager();
            ((MEMonitorableContainer)this.container).setGui(this);
            List<Slot> viewCellSlots = container.getSlots(SlotSemantic.VIEW_CELL);
            this.supportsViewCells = !viewCellSlots.isEmpty();
            if (this.supportsViewCells) {
                List<ITextComponent> tooltip = Collections.singletonList(GuiText.TerminalViewCellsTooltip.text());
                this.widgets.add("viewCells", new MTUpgradePanel(viewCellSlots, () -> {
                    return tooltip;
                }));
            }

            if (this.style.isSupportsAutoCrafting()) {
                this.craftingStatusBtn = new TabButton(Icon.PERMISSION_CRAFT, GuiText.CraftingStatus.text(), this.itemRenderer, (btn) -> {
                    this.showCraftingStatus();
                });
                this.craftingStatusBtn.setHideEdge(true);
                this.widgets.add("craftingStatus", this.craftingStatusBtn);
            }

            if (this.style.isSortable()) {

                //    this.sortByToggle = (SettingToggleButton)this.addToLeftToolbar(
                //           new SettingToggleButton(Settings.SORT_BY, this.getSortBy(), Platform::isSortOrderAvailable, this::toggleServerSetting));
            }

            if (this.style.isSupportsAutoCrafting()) {
                this.viewModeToggle = (SettingToggleButton)this.addToLeftToolbar(new SettingToggleButton(Settings.VIEW_MODE, this.getSortDisplay(), this::toggleServerSetting));
            }

            this.addToLeftToolbar(this.sortDirToggle = new SettingToggleButton(Settings.SORT_DIRECTION, this.getSortDir(), this::toggleServerSetting));
            SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();

            //TODO FIX ME
            //this.addToLeftToolbar(
            //        new SettingToggleButton(Settings.SEARCH_MODE, searchMode, Platform::isSearchModeAvailable, this::toggleTerminalSearchMode));
            if (this.style.getMaxRows() == null) {
                TerminalStyle terminalStyle = AEConfig.instance().getTerminalStyle();
                this.addToLeftToolbar(new SettingToggleButton(Settings.TERMINAL_STYLE, terminalStyle, this::toggleTerminalStyle));
            }

        }
    }

    protected abstract Repo<T> createRepo(IScrollSource var1);

    @Nullable
    protected abstract IPartitionList<T> createPartitionList(List<ItemStack> var1);

    protected abstract void renderGridInventoryEntry(MatrixStack var1, int var2, int var3, GridInventoryEntry<T> var4);

    protected abstract void handleGridInventoryEntryMouseClick(@Nullable GridInventoryEntry<T> var1, int var2, ClickType var3);

    private void updateScrollbar() {
        this.scrollbar.setHeight(this.rows * this.style.getRow().getSrcHeight() - 2);
        int totalRows = (this.repo.size() + this.getSlotsPerRow() - 1) / this.getSlotsPerRow();
        this.scrollbar.setRange(0, totalRows - this.rows, Math.max(1, this.rows / 6));
    }

    private void showCraftingStatus() {
        NetworkHandler.instance().sendToServer(new SwitchGuisPacket(CraftingStatusContainer.TYPE));
    }

    private int getSlotsPerRow() {
        return this.style.getSlotsPerRow();
    }

    public void init() {
        this.getMinecraft().keyboardListener.enableRepeatEvents(true);
        this.rows = MathHelper.clamp(this.style.getPossibleRows(this.height), 3, this.getMaxRows());
        this.ySize = this.style.getScreenHeight(this.rows);
        List<Slot> slots = ((MEMonitorableContainer)this.container).inventorySlots;
        slots.removeIf((slot) -> {
            return slot instanceof RepoSlot;
        });
        int repoIndex = 0;

        for(int row = 0; row < this.rows; ++row) {
            for(int col = 0; col < this.style.getSlotsPerRow(); ++col) {
                Point pos = this.style.getSlotPos(row, col);
                slots.add(new RepoSlot(this.repo, repoIndex++, pos.getX(), pos.getY()));
            }
        }

        super.init();
        Rectangle2d searchFieldRect = this.style.getSearchFieldRect();
        this.searchField = new AETextField(this.font, this.guiLeft + searchFieldRect.getX(), this.guiTop + searchFieldRect.getY(), searchFieldRect.getWidth(), searchFieldRect.getHeight());
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(16777215);
        this.searchField.setSelectionColor(-16744448);
        this.searchField.setVisible(true);
        SearchBoxMode searchMode = AEConfig.instance().getTerminalSearchMode();
        this.isAutoFocus = SearchBoxMode.AUTOSEARCH == searchMode || SearchBoxMode.JEI_AUTOSEARCH == searchMode || SearchBoxMode.AUTOSEARCH_KEEP == searchMode || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode;
        boolean isKeepFilter = SearchBoxMode.AUTOSEARCH_KEEP == searchMode || SearchBoxMode.JEI_AUTOSEARCH_KEEP == searchMode || SearchBoxMode.MANUAL_SEARCH_KEEP == searchMode || SearchBoxMode.JEI_MANUAL_SEARCH_KEEP == searchMode;
        boolean isJEIEnabled = SearchBoxMode.JEI_AUTOSEARCH == searchMode || SearchBoxMode.JEI_MANUAL_SEARCH == searchMode;
        this.searchField.setFocused2(this.isAutoFocus);
        if (this.searchField.isFocused()) {
            this.setListener(this.searchField);
        }

        if (isJEIEnabled) {
            memoryText = JEIFacade.instance().getSearchText();
        }

        if (isKeepFilter && memoryText != null && !memoryText.isEmpty()) {
            this.searchField.setText(memoryText);
            this.searchField.selectAll();
            this.repo.setSearchString(memoryText);
            this.repo.updateView();
        }

        this.updateScrollbar();
    }

    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (!this.title.getString().isEmpty()) {
            this.setTextContent("dialog_title", this.title);
        } else if (((MEMonitorableContainer)this.container).getTarget() instanceof IMEChest) {
            this.setTextContent("dialog_title", GuiText.Chest.text());
        }

    }

    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
        this.currentMouseX = mouseX;
        this.currentMouseY = mouseY;
        if (this.craftingStatusBtn != null && ((MEMonitorableContainer)this.container).activeCraftingJobs != -1) {
            int x = this.craftingStatusBtn.x + (this.craftingStatusBtn.getWidth() - 16) / 2;
            int y = this.craftingStatusBtn.y + (this.craftingStatusBtn.getHeight() - 16) / 2;
            this.style.getStackSizeRenderer().renderSizeLabel(this.font, (float)(x - this.guiLeft), (float)(y - this.guiTop), String.valueOf(((MEMonitorableContainer)this.container).activeCraftingJobs));
        }

    }

    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        if (this.searchField.mouseClicked(xCoord, yCoord, btn)) {
            return true;
        } else if (this.searchField.isMouseOver(xCoord, yCoord) && btn == 1) {
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
            this.updateScrollbar();
            return true;
        } else {
            return super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        if (wheelDelta != 0.0D && hasShiftDown()) {
            Slot slot = this.getSlot((int)x, (int)y);
            RepoSlot<T> repoSlot = RepoSlot.tryCast(this.repo, slot);
            if (repoSlot != null) {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                long serial = entry != null ? entry.getSerial() : -1L;
                InventoryAction direction = wheelDelta > 0.0D ? InventoryAction.ROLL_DOWN : InventoryAction.ROLL_UP;
                int times = (int)Math.abs(wheelDelta);

                for(int h = 0; h < times; ++h) {
                    MEInteractionPacket p = new MEInteractionPacket(((MEMonitorableContainer)this.container).windowId, serial, direction);
                    NetworkHandler.instance().sendToServer(p);
                }

                return true;
            }
        }

        return super.mouseScrolled(x, y, wheelDelta);
    }

    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        RepoSlot<T> repoSlot = RepoSlot.tryCast(this.repo, slot);
        if (repoSlot != null) {
            this.handleGridInventoryEntryMouseClick(repoSlot.getEntry(), mouseButton, clickType);
        } else {
            super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
        }
    }

    public void onClose() {
        super.onClose();
        this.getMinecraft().keyboardListener.enableRepeatEvents(false);
        memoryText = this.searchField.getText();
    }

    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        this.style.getHeader().dest(offsetX, offsetY).blit(matrixStack, this.getBlitOffset());
        this.style.getHeader().dest(offsetX, offsetY).blit(matrixStack, this.getBlitOffset());
        int y = offsetY + this.style.getHeader().getSrcHeight();
        int rowsToDraw = Math.max(2, this.rows);

        for(int x = 0; x < rowsToDraw; ++x) {
            MTBlitter row = this.style.getRow();
            if (x == 0) {
                row = this.style.getFirstRow();
            } else if (x + 1 == rowsToDraw) {
                row = this.style.getLastRow();
            }

            row.dest(offsetX, y).blit(matrixStack, this.getBlitOffset());
            y += this.style.getRow().getSrcHeight();
        }

        this.style.getBottom().dest(offsetX, y).blit(matrixStack, this.getBlitOffset());
        if (this.searchField != null) {
            this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        }

    }
    @Override
    protected void moveItems(MatrixStack matrices, Slot s) {
        RepoSlot<T> repoSlot = RepoSlot.tryCast(this.repo, s);
        if (repoSlot == null) {
            super.moveItems(matrices, s);
        } else {
            if (!this.repo.hasPower()) {
                fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 1712394513);
            } else {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                if (entry != null) {
                    try {
                        this.renderGridInventoryEntry(matrices, s.xPos, s.yPos, entry);
                    } catch (Exception var8) {
                        AELog.warn("[AppEng] AE prevented crash while drawing slot: " + var8, new Object[0]);
                    }

                    long storedAmount = entry.getStoredAmount();
                    boolean craftable = entry.isCraftable();
                    if (this.isViewOnlyCraftable() && craftable) {
                        this.style.getStackSizeRenderer().renderStackSize(this.font, 0L, true, s.xPos, s.yPos);
                    } else {
                        this.style.getStackSizeRenderer().renderStackSize(this.font, storedAmount, craftable, s.xPos, s.yPos);
                    }
                }
            }

        }
    }

    protected final boolean isViewOnlyCraftable() {
        return this.viewModeToggle != null && this.viewModeToggle.getCurrentValue() == ViewItems.CRAFTABLE;
    }

    protected void renderHoveredTooltip(MatrixStack matrixStack, int x, int y) {
        if (this.style.isShowTooltipsWithItemInHand() || this.getPlayer().inventory.getItemStack().isEmpty()) {
            RepoSlot<T> repoSlot = RepoSlot.tryCast(this.repo, this.hoveredSlot);
            if (repoSlot != null) {
                GridInventoryEntry<T> entry = repoSlot.getEntry();
                if (entry != null) {
                    this.renderGridInventoryEntryTooltip(matrixStack, entry, x, y);
                }

                return;
            }
        }

        super.renderHoveredTooltip(matrixStack, x, y);
    }

    protected void renderGridInventoryEntryTooltip(MatrixStack matrices, GridInventoryEntry<T> entry, int x, int y) {
        int bigNumber = AEConfig.instance().isUseLargeFonts() ? 999 : 9999;
        ItemStack stack = entry.getStack().asItemStackRepresentation();
        List<ITextComponent> currentToolTip = this.getTooltipFromItem(stack);
        long storedAmount = entry.getStoredAmount();
        if (storedAmount > (long)bigNumber || storedAmount > 1L && stack.isDamaged()) {
            String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(storedAmount);
            currentToolTip.add(ButtonToolTips.ItemsStored.text(new Object[]{formattedAmount}).mergeStyle(TextFormatting.GRAY));
        }

        long requestableAmount = entry.getRequestableAmount();
        if (requestableAmount > 0L) {
            String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(requestableAmount);
            currentToolTip.add(ButtonToolTips.ItemsRequestable.text(new Object[]{formattedAmount}));
        }

        if (Minecraft.getInstance().gameSettings.advancedItemTooltips) {
            currentToolTip.add((new StringTextComponent("Serial: " + entry.getSerial())).mergeStyle(TextFormatting.DARK_GRAY));
        }

        this.renderWrappedToolTip(matrices, currentToolTip, x, y, this.font);
    }

    private int getMaxRows() {
        Integer maxRows = this.style.getMaxRows();
        if (maxRows != null) {
            return maxRows;
        } else {
            return AEConfig.instance().getTerminalStyle() == TerminalStyle.SMALL ? 6 : 2147483647;
        }
    }

    public boolean charTyped(char character, int p_charTyped_2_) {
        if (character == ' ' && this.searchField.getText().isEmpty()) {
            return true;
        } else {
            if (this.isAutoFocus && !this.searchField.isFocused() && this.isHovered()) {
                this.setFocusedDefault(this.searchField);
            }

            if (this.searchField.isFocused() && this.searchField.charTyped(character, p_charTyped_2_)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.updateScrollbar();
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int p_keyPressed_3_) {
        Input input = InputMappings.getInputByCode(keyCode, scanCode);
        if (keyCode != 256 && !this.checkHotbarKeys(input)) {
            if (AppEng.proxy.isActionKey(ActionKey.TOGGLE_FOCUS, input)) {
                this.searchField.setFocused2(!this.searchField.isFocused());
                if (this.searchField.isFocused()) {
                    this.setListener(this.searchField);
                }

                return true;
            }

            if (this.searchField.isFocused()) {
                if (keyCode == 257) {
                    this.searchField.setFocused2(false);
                    this.setListener((IGuiEventListener)null);
                    return true;
                }

                if (this.searchField.keyPressed(keyCode, scanCode, p_keyPressed_3_)) {
                    this.repo.setSearchString(this.searchField.getText());
                    this.repo.updateView();
                    this.updateScrollbar();
                }

                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, p_keyPressed_3_);
    }

    private boolean isHovered() {
        return this.isPointInRegion(0, 0, this.xSize, this.ySize, (double)this.currentMouseX, (double)this.currentMouseY);
    }

    public void tick() {
        this.repo.setPower(((MEMonitorableContainer)this.container).isPowered());
        if (this.supportsViewCells) {
            List<ItemStack> viewCells = ((MEMonitorableContainer)this.container).getViewCells();
            if (!this.currentViewCells.equals(viewCells)) {
                this.currentViewCells.clear();
                this.currentViewCells.addAll(viewCells);
                this.repo.setPartitionList(this.createPartitionList(viewCells));
            }
        }

        super.tick();
    }

    public SortOrder getSortBy() {
        return (SortOrder)this.configSrc.getSetting(Settings.SORT_BY);
    }

    public SortDir getSortDir() {
        return (SortDir)this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    public ViewItems getSortDisplay() {
        return (ViewItems)this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        if (this.sortByToggle != null) {
            this.sortByToggle.set(this.getSortBy());
        }

        if (this.sortDirToggle != null) {
            this.sortDirToggle.set(this.getSortDir());
        }

        if (this.viewModeToggle != null) {
            this.viewModeToggle.set(this.getSortDisplay());
        }

        this.repo.updateView();
    }

    private void toggleTerminalSearchMode(SettingToggleButton<SearchBoxMode> btn, boolean backwards) {
        SearchBoxMode next = (SearchBoxMode)btn.getNextValue(backwards);
        AEConfig.instance().setTerminalSearchMode(next);
        btn.set(next);
        this.reinitalize();
    }

    //TODO hm..
    private void toggleTerminalStyle(SettingToggleButton<TerminalStyle> btn, boolean backwards) {
        TerminalStyle next = (TerminalStyle)btn.getNextValue(backwards);
        AEConfig.instance().setTerminalStyle(next);
        btn.set(next);
        this.reinitalize();
    }

    private <SE extends Enum<SE>> void toggleServerSetting(SettingToggleButton<SE> btn, boolean backwards) {
        SE next = btn.getNextValue(backwards);
        NetworkHandler.instance().sendToServer(new ConfigValuePacket(btn.getSetting().name(), next.name()));
        btn.set(next);
    }

    private void reinitalize() {
        this.children.removeAll(this.buttons);
        this.buttons.clear();
        this.init();
    }
}
