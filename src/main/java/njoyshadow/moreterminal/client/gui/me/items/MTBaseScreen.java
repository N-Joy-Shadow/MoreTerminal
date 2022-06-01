package njoyshadow.moreterminal.client.gui.me.items;

import appeng.client.Point;
import appeng.client.gui.*;
import appeng.client.gui.layout.SlotGridLayout;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.Text;
import appeng.client.gui.widgets.CustomSlotWidget;
import appeng.client.gui.widgets.ITickingWidget;
import appeng.client.gui.widgets.ITooltip;
import appeng.client.gui.widgets.VerticalButtonBar;
import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.slot.*;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.core.sync.packets.SwapSlotsPacket;
import appeng.helpers.InventoryAction;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.gui.GuiUtils;
import njoyshadow.moreterminal.client.gui.me.style.MTScreenStyle;
import njoyshadow.moreterminal.client.gui.widget.MTVerticalButtonBar;
import njoyshadow.moreterminal.client.gui.widget.MTWidgetContainer;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MTBaseScreen<T extends AEBaseContainer> extends ContainerScreen<T> {
    private static final Point HIDDEN_SLOT_POS = new Point(-9999, -9999);
    public static final String TEXT_ID_DIALOG_TITLE = "dialog_title";
    private final MTVerticalButtonBar verticalToolbar;
    private final Set<Slot> drag_click = new HashSet();
    private boolean disableShiftClick = false;
    private Stopwatch dbl_clickTimer = Stopwatch.createStarted();
    private ItemStack dbl_whichItem;
    private Slot bl_clicked;
    private boolean handlingRightClick;
    private final List<CustomSlotWidget> guiSlots;
    private final ArrayListMultimap<SlotSemantic, CustomSlotWidget> guiSlotsBySemantic;
    private final Map<String, TextOverride> textOverrides;
    private final EnumSet<SlotSemantic> hiddenSlots;
    protected final MTWidgetContainer widgets;
    protected final MTScreenStyle style;
    private static final Style TOOLTIP_HEADER;
    private static final Style TOOLTIP_BODY;

    public MTBaseScreen(T container, PlayerInventory playerInventory, ITextComponent title, MTScreenStyle style) {
        super(container, playerInventory, title);
        this.dbl_whichItem = ItemStack.EMPTY;
        this.guiSlots = new ArrayList();
        this.guiSlotsBySemantic = ArrayListMultimap.create();
        this.textOverrides = new HashMap();
        this.hiddenSlots = EnumSet.noneOf(SlotSemantic.class);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
        this.font = Minecraft.getInstance().fontRenderer;
        this.style = (MTScreenStyle)Objects.requireNonNull(style, "style");
        this.widgets = new MTWidgetContainer(style);
        this.widgets.add("verticalToolbar", this.verticalToolbar = new MTVerticalButtonBar());
        if (style.getBackground() != null) {
            this.xSize = style.getBackground().getSrcWidth();
            this.ySize = style.getBackground().getSrcHeight();
        }

    }

    @OverridingMethodsMustInvokeSuper
    protected void init() {
        super.init();
        this.positionSlots(this.style);
        this.widgets.populateScreen(this::addButton, this.getBounds(true), this);
    }

    private void positionSlots(MTScreenStyle style) {
        Iterator var2 = style.getSlots().entrySet().iterator();

        while(true) {
            Map.Entry entry;
            List guiSlots;
            do {
                do {
                    if (!var2.hasNext()) {
                        return;
                    }

                    entry = (Map.Entry)var2.next();
                } while(this.hiddenSlots.contains(entry.getKey()));

                List<Slot> slots = ((AEBaseContainer)this.container).getSlots((SlotSemantic)entry.getKey());

                for(int i = 0; i < slots.size(); ++i) {
                    Slot slot = (Slot)slots.get(i);
                    Point pos = this.getSlotPosition((SlotPosition)entry.getValue(), i);
                    //TODO FIX ME
                    //slot.xPos = pos.getX();
                    //slot.yPos = pos.getY();
                }

                guiSlots = this.guiSlotsBySemantic.get((SlotSemantic) entry.getKey());
            } while(guiSlots == null);

            for(int i = 0; i < guiSlots.size(); ++i) {
                CustomSlotWidget guiSlot = (CustomSlotWidget)guiSlots.get(i);
                Point pos = this.getSlotPosition((SlotPosition)entry.getValue(), i);
                guiSlot.setPos(pos);
            }
        }
    }

    private Point getSlotPosition(SlotPosition position, int semanticIndex) {
        Point pos = position.resolve(this.getBounds(false));
        SlotGridLayout grid = position.getGrid();
        if (grid != null) {
            pos = grid.getPosition(pos.getX(), pos.getY(), semanticIndex);
        }

        return pos;
    }

    private Rectangle2d getBounds(boolean absolute) {
        return absolute ? new Rectangle2d(this.guiLeft, this.guiTop, this.xSize, this.ySize) : new Rectangle2d(0, 0, this.xSize, this.ySize);
    }

    private List<Slot> getInventorySlots() {
        return ((AEBaseContainer)this.container).inventorySlots;
    }

    protected final void addSlot(CustomSlotWidget slot, SlotSemantic semantic) {
        this.guiSlots.add(slot);
        this.guiSlotsBySemantic.put(semantic, slot);
    }

    @OverridingMethodsMustInvokeSuper
    protected void updateBeforeRender() {
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.updateBeforeRender();
        this.widgets.updateBeforeRender();
        super.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        matrixStack.push();
        matrixStack.translate((double)this.guiLeft, (double)this.guiTop, 0.0D);
        RenderSystem.enableDepthTest();
        Iterator var5 = this.guiSlots.iterator();

        CustomSlotWidget c;
        while(var5.hasNext()) {
            c = (CustomSlotWidget)var5.next();
            this.drawGuiSlot(matrixStack, c, mouseX, mouseY, partialTicks);
        }

        RenderSystem.disableDepthTest();
        var5 = this.guiSlots.iterator();

        while(var5.hasNext()) {
            c = (CustomSlotWidget)var5.next();
            Tooltip tooltip = c.getTooltip(mouseX, mouseY);
            if (tooltip != null) {
                this.drawTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        }

        matrixStack.pop();
        RenderSystem.enableDepthTest();
        this.renderTooltips(matrixStack, mouseX, mouseY);
        if (AEConfig.instance().isShowDebugGuiOverlays()) {
            List<Rectangle2d> exclusionZones = this.getExclusionZones();
            Iterator var9 = exclusionZones.iterator();

            while(var9.hasNext()) {
                Rectangle2d rectangle2d = (Rectangle2d)var9.next();
                this.fillRect(matrixStack, rectangle2d, 2130771712);
            }

            this.hLine(matrixStack, this.guiLeft, this.guiLeft + this.xSize - 1, this.guiTop, -1);
            this.hLine(matrixStack, this.guiLeft, this.guiLeft + this.xSize - 1, this.guiTop + this.ySize - 1, -1);
            this.vLine(matrixStack, this.guiLeft, this.guiTop, this.guiTop + this.ySize, -1);
            this.vLine(matrixStack, this.guiLeft + this.xSize - 1, this.guiTop, this.guiTop + this.ySize - 1, -1);
        }

    }

    private void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
        if (this.hoveredSlot == null || !this.hoveredSlot.getHasStack()) {
            Iterator var4 = this.buttons.iterator();

            while(var4.hasNext()) {
                Widget c = (Widget)var4.next();
                if (c instanceof ITooltip) {
                    Tooltip tooltip = ((ITooltip)c).getTooltip(mouseX, mouseY);
                    if (tooltip != null) {
                        this.drawTooltip(matrixStack, tooltip, mouseX, mouseY);
                    }
                }
            }

            Tooltip tooltip = this.widgets.getTooltip(mouseX - this.guiLeft, mouseY - this.guiTop);
            if (tooltip != null) {
                this.drawTooltip(matrixStack, tooltip, mouseX, mouseY);
            }

        }
    }

    protected void drawGuiSlot(MatrixStack matrixStack, CustomSlotWidget slot, int mouseX, int mouseY, float partialTicks) {
        if (slot.isSlotEnabled()) {
            int left = slot.getTooltipAreaX();
            int top = slot.getTooltipAreaY();
            int right = left + slot.getTooltipAreaWidth();
            int bottom = top + slot.getTooltipAreaHeight();
            slot.drawContent(matrixStack, this.getMinecraft(), mouseX, mouseY, partialTicks);
            if (this.isPointInRegion(left, top, slot.getTooltipAreaWidth(), slot.getTooltipAreaHeight(), (double)mouseX, (double)mouseY) && slot.canClick(this.getPlayer())) {
                RenderSystem.colorMask(true, true, true, false);
                this.fillGradient(matrixStack, left, top, right, bottom, -2130706433, -2130706433);
                RenderSystem.colorMask(true, true, true, true);
            }
        }

    }

    private void drawTooltip(MatrixStack matrixStack, Tooltip tooltip, int mouseX, int mouseY) {
        GuiUtils.drawHoveringText(matrixStack, tooltip.getContent(), mouseX, mouseY, this.width, this.height, 200, this.font);
    }

    public void drawTooltip(MatrixStack matrices, int x, int y, List<ITextComponent> lines) {
        if (!lines.isEmpty()) {
            List<ITextComponent> styledLines = new ArrayList(lines.size());

            for(int i = 0; i < lines.size(); ++i) {
                Style style = i == 0 ? TOOLTIP_HEADER : TOOLTIP_BODY;
                styledLines.add(((ITextComponent)lines.get(i)).deepCopy().modifyStyle((s) -> {
                    return style;
                }));
            }

            this.func_243308_b(matrices, styledLines, x, y);
        }
    }

    protected final void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        int ox = this.guiLeft;
        int oy = this.guiTop;
        this.widgets.drawForegroundLayer(matrixStack, this.getBlitOffset(), this.getBounds(false), new Point(x - ox, y - oy));
        this.drawFG(matrixStack, ox, oy, x, y);
        if (this.style != null) {
            Iterator var6 = this.style.getText().entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<String, Text> entry = (Map.Entry)var6.next();
                TextOverride override = (TextOverride)this.textOverrides.get(entry.getKey());
                this.drawText(matrixStack, (Text)entry.getValue(), override);
            }
        }

    }

    private void drawText(MatrixStack matrixStack, Text text, @Nullable TextOverride override) {
        if (override == null || !override.isHidden()) {
            int color = this.style.getColor(text.getColor()).toARGB();
            ITextComponent content = text.getText();
            if (override != null && override.getContent() != null) {
                content = override.getContent();
            }

            Point pos = text.getPosition().resolve(this.getBounds(false));
            if (text.isCenterHorizontally()) {
                int textWidth = this.font.getStringPropertyWidth(content);
                pos = pos.move(-textWidth / 2, 0);
            }

            this.font.drawText(matrixStack, content, (float)pos.getX(), (float)pos.getY(), color);
        }
    }

    public void drawFG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY) {
    }

    protected final void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float f, int x, int y) {
        this.drawBG(matrixStack, this.guiLeft, this.guiTop, x, y, f);
        this.widgets.drawBackgroundLayer(matrixStack, this.getBlitOffset(), this.getBounds(true), new Point(x - this.guiLeft, y - this.guiTop));
        Iterator var5 = this.getInventorySlots().iterator();

        while(var5.hasNext()) {
            Slot slot = (Slot)var5.next();
            if (slot instanceof IOptionalSlot) {
                this.drawOptionalSlotBackground(matrixStack, (IOptionalSlot)slot, false);
            }
        }

        var5 = this.guiSlots.iterator();

        while(var5.hasNext()) {
            CustomSlotWidget slot = (CustomSlotWidget)var5.next();
            if (slot instanceof IOptionalSlot) {
                this.drawOptionalSlotBackground(matrixStack, (IOptionalSlot)slot, true);
            }
        }

    }

    private void drawOptionalSlotBackground(MatrixStack matrixStack, IOptionalSlot slot, boolean alwaysDraw) {
        if (alwaysDraw || slot.isRenderDisabled()) {
            float alpha = slot.isSlotEnabled() ? 1.0F : 0.4F;
            Point pos = slot.getBackgroundPos();
            Icon.SLOT_BACKGROUND.getBlitter().dest(this.guiLeft + pos.getX(), this.guiTop + pos.getY()).color(1.0F, 1.0F, 1.0F, alpha).blit(matrixStack, this.getBlitOffset());
        }

    }

    private Point getMousePoint(double x, double y) {
        return new Point((int)Math.round(x - (double)this.guiLeft), (int)Math.round(y - (double)this.guiTop));
    }

    public boolean mouseScrolled(double x, double y, double wheelDelta) {
        return wheelDelta != 0.0D && this.widgets.onMouseWheel(this.getMousePoint(x, y), wheelDelta);
    }

    public boolean mouseClicked(double xCoord, double yCoord, int btn) {
        this.drag_click.clear();
        if (btn == 1) {
            this.handlingRightClick = true;

            try {
                Iterator var6 = this.buttons.iterator();

                while(var6.hasNext()) {
                    Widget widget = (Widget)var6.next();
                    if (widget.isMouseOver(xCoord, yCoord)) {
                        boolean var8 = super.mouseClicked(xCoord, yCoord, 0);
                        return var8;
                    }
                }
            } finally {
                this.handlingRightClick = false;
            }
        }

        CustomSlotWidget slot = this.getGuiSlotAt(xCoord, yCoord);
        if (slot != null) {
            slot.slotClicked(this.getPlayer().inventory.getItemStack(), btn);
        }

        if (this.widgets.onMouseDown(this.getMousePoint(xCoord, yCoord), btn)) {
            return true;
        } else {
            return super.mouseClicked(xCoord, yCoord, btn);
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.widgets.onMouseUp(this.getMousePoint(mouseX, mouseY), button) ? true : super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
        Slot slot = this.getSlot((int)mouseX, (int)mouseY);
        ItemStack itemstack = this.getPlayer().inventory.getItemStack();
        Point mousePos = new Point((int)Math.round(mouseX - (double)this.guiLeft), (int)Math.round(mouseY - (double)this.guiTop));
        if (this.widgets.onMouseDrag(mousePos, mouseButton)) {
            return true;
        } else if (slot instanceof FakeSlot && !itemstack.isEmpty()) {
            this.drag_click.add(slot);
            if (this.drag_click.size() > 1) {
                Iterator var13 = this.drag_click.iterator();

                while(var13.hasNext()) {
                    Slot dr = (Slot)var13.next();
                    InventoryActionPacket p = new InventoryActionPacket(mouseButton == 0 ? InventoryAction.PICKUP_OR_SET_DOWN : InventoryAction.PLACE_SINGLE, dr.slotNumber, 0L);
                    NetworkHandler.instance().sendToServer(p);
                }
            }

            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
        }
    }

    protected void handleMouseClick(@Nullable Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (!(slot instanceof DisabledSlot)) {
            InventoryAction action;
            InventoryActionPacket p;
            if (slot instanceof FakeSlot) {
                action = mouseButton == 1 ? InventoryAction.SPLIT_OR_PLACE_SINGLE : InventoryAction.PICKUP_OR_SET_DOWN;
                if (this.drag_click.size() <= 1) {
                    p = new InventoryActionPacket(action, slotIdx, 0L);
                    NetworkHandler.instance().sendToServer(p);
                }
            } else {
                if (slot instanceof PatternTermSlot) {
                    if (mouseButton == 6) {
                        return;
                    }

                    NetworkHandler.instance().sendToServer(((PatternTermSlot)slot).getRequest(hasShiftDown()));
                } else if (slot instanceof CraftingTermSlot) {
                    if (mouseButton == 6) {
                        return;
                    }

                    if (hasShiftDown()) {
                        action = InventoryAction.CRAFT_SHIFT;
                    } else {
                        action = mouseButton == 1 ? InventoryAction.CRAFT_STACK : InventoryAction.CRAFT_ITEM;
                    }

                    p = new InventoryActionPacket(action, slotIdx, 0L);
                    NetworkHandler.instance().sendToServer(p);
                    return;
                }

                if (slot != null && InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), 32)) {
                    int slotNum = slot.slotNumber;
                    p = new InventoryActionPacket(InventoryAction.MOVE_REGION, slotNum, 0L);
                    NetworkHandler.instance().sendToServer(p);
                } else {
                    if (slot != null && !this.disableShiftClick && hasShiftDown() && mouseButton == 0) {
                        this.disableShiftClick = true;
                        if (!this.dbl_whichItem.isEmpty() && this.bl_clicked == slot && this.dbl_clickTimer.elapsed(TimeUnit.MILLISECONDS) <= 250L) {
                            if (!this.dbl_whichItem.isEmpty()) {
                                List<Slot> slots = this.getInventorySlots();
                                Iterator var6 = slots.iterator();

                                while(var6.hasNext()) {
                                    Slot inventorySlot = (Slot)var6.next();
                                    if (inventorySlot != null && inventorySlot.canTakeStack(this.getPlayer()) && inventorySlot.getHasStack() && inventorySlot.isSameInventory(slot) && Container.canAddItemToSlot(inventorySlot, this.dbl_whichItem, true)) {
                                        this.handleMouseClick(inventorySlot, inventorySlot.slotNumber, 0, ClickType.QUICK_MOVE);
                                    }
                                }

                                this.dbl_whichItem = ItemStack.EMPTY;
                            }
                        } else {
                            this.bl_clicked = slot;
                            this.dbl_clickTimer = Stopwatch.createStarted();
                            this.dbl_whichItem = slot.getHasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
                        }

                        this.disableShiftClick = false;
                    }

                    super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
                }
            }
        }
    }
    @Override
    protected boolean itemStackMoved(int keyCode, int scanCode) {
        return this.checkHotbarKeys(InputMappings.getInputByCode(keyCode, scanCode));
    }

    protected ClientPlayerEntity getPlayer() {
        return (ClientPlayerEntity) Preconditions.checkNotNull(this.getMinecraft().player);
    }

    protected boolean checkHotbarKeys(InputMappings.Input input) {
        Slot theSlot = this.getSlotUnderMouse();
        if (this.getPlayer().inventory.getItemStack().isEmpty() && theSlot != null) {
            for(int j = 0; j < 9; ++j) {
                if (this.getMinecraft().gameSettings.keyBindsHotbar[j].isActiveAndMatches(input)) {
                    List<Slot> slots = this.getInventorySlots();
                    Iterator var5 = slots.iterator();

                    Slot s;
                    while(var5.hasNext()) {
                        s = (Slot)var5.next();
                        if (s.getSlotIndex() == j && s.inventory == ((AEBaseContainer)this.container).getPlayerInventory() && !s.canTakeStack(((AEBaseContainer)this.container).getPlayerInventory().player)) {
                            return false;
                        }
                    }

                    if (theSlot.getSlotStackLimit() == 64) {
                        this.handleMouseClick(theSlot, theSlot.slotNumber, j, ClickType.SWAP);
                        return true;
                    }

                    var5 = slots.iterator();

                    while(var5.hasNext()) {
                        s = (Slot)var5.next();
                        if (s.getSlotIndex() == j && s.inventory == ((AEBaseContainer)this.container).getPlayerInventory()) {
                            NetworkHandler.instance().sendToServer(new SwapSlotsPacket(s.slotNumber, theSlot.slotNumber));
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected Slot getSlot(int mouseX, int mouseY) {
        List<Slot> slots = this.getInventorySlots();
        Iterator var4 = slots.iterator();

        Slot slot;
        do {
            if (!var4.hasNext()) {
                return null;
            }

            slot = (Slot)var4.next();
        } while(!this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, (double)mouseX, (double)mouseY));

        return slot;
    }

    public void drawBG(MatrixStack matrixStack, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        if (this.style.getBackground() != null) {
            this.style.getBackground().dest(offsetX, offsetY).blit(matrixStack, this.getBlitOffset());
        }

    }

    public void drawItem(int x, int y, ItemStack is) {
        this.itemRenderer.zLevel = 100.0F;
        RenderHelper.enableStandardItemLighting();
        this.itemRenderer.renderItemAndEffectIntoGUI(is, x, y);
        RenderHelper.disableStandardItemLighting();
        this.itemRenderer.zLevel = 0.0F;
    }

    protected ITextComponent getGuiDisplayName(ITextComponent in) {
        return this.title.getString().isEmpty() ? in : this.title;
    }

    protected void moveItems(MatrixStack matrices, Slot s) {
        if (s instanceof AppEngSlot) {
            try {
                this.renderAppEngSlot(matrices, (AppEngSlot)s);
            } catch (Exception var4) {
                AELog.warn("[AppEng] AE prevented crash while drawing slot: " + var4, new Object[0]);
            }
        } else {
            //TODO FIX ME

            //super.moveItems(matrices, s);
        }

    }

    private void renderAppEngSlot(MatrixStack matrices, AppEngSlot s) {
        ItemStack is = s.getStack();
        if ((s.renderIconWithItem() || is.isEmpty()) && s.isSlotEnabled() && s.getIcon() != null) {
            s.getIcon().getBlitter().dest(s.xPos, s.yPos).opacity(s.getOpacityOfIcon()).blit(matrices, this.getBlitOffset());
        }

        if (!s.isValid()) {
            fill(matrices, s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 1728013926);
        }

        s.setRendering(true);

        try {
            //TODO FIX ME

            //super.moveItems(matrices, s);
        } finally {
            s.setRendering(false);
        }

    }

    public void bindTexture(String file) {
        ResourceLocation loc = new ResourceLocation("moreterminal", "textures/" + file);
        this.getMinecraft().getTextureManager().bindTexture(loc);
    }

    public void tick() {
        super.tick();
        this.widgets.tick();
        Iterator var1 = this.children.iterator();

        while(var1.hasNext()) {
            IGuiEventListener child = (IGuiEventListener)var1.next();
            if (child instanceof ITickingWidget) {
                ((ITickingWidget)child).tick();
            }
        }

    }

    public boolean isHandlingRightClick() {
        return this.handlingRightClick;
    }

    protected final <B extends Button> B addToLeftToolbar(B button) {
        this.verticalToolbar.add(button);
        return button;
    }

    public List<Rectangle2d> getExclusionZones() {
        List<Rectangle2d> result = new ArrayList(2);
        this.widgets.addExclusionZones(result, this.getBounds(true));
        return result;
    }

    protected void fillRect(MatrixStack matrices, Rectangle2d rect, int color) {
        fill(matrices, rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color);
    }

    private TextOverride getOrCreateTextOverride(String id) {
        return (TextOverride)this.textOverrides.computeIfAbsent(id, (x) -> {
            return new TextOverride();
        });
    }

    protected final void setTextHidden(String id, boolean hidden) {
        this.getOrCreateTextOverride(id).setHidden(hidden);
    }

    protected final void setSlotsHidden(SlotSemantic semantic, boolean hidden) {
        if (hidden) {
            Slot slot;
            if (this.hiddenSlots.add(semantic)) {

                //TODO FIX ME
                /*
                for(Iterator var3 = ((AEBaseContainer)this.container).getSlots(semantic).iterator(); var3.hasNext();
                    slot.yPos = HIDDEN_SLOT_POS.getY()) {
                    slot = (Slot)var3.next();
                    //slot.xPos = HIDDEN_SLOT_POS.getX();
                }*/
            }
        } else if (this.hiddenSlots.remove(semantic) && this.style != null) {
            this.positionSlots(this.style);
        }

    }

    @Nullable
    private CustomSlotWidget getGuiSlotAt(double x, double y) {
        Iterator var5 = this.guiSlots.iterator();

        CustomSlotWidget slot;
        do {
            if (!var5.hasNext()) {
                return null;
            }

            slot = (CustomSlotWidget)var5.next();
        } while(!this.isPointInRegion(slot.getTooltipAreaX(), slot.getTooltipAreaY(), slot.getTooltipAreaWidth(), slot.getTooltipAreaHeight(), x, y) || !slot.canClick(this.getPlayer()));

        return slot;
    }

    public List<CustomSlotWidget> getGuiSlots() {
        return Collections.unmodifiableList(this.guiSlots);
    }

    protected final void setTextContent(String id, ITextComponent content) {
        this.getOrCreateTextOverride(id).setContent(content);
    }

    public MTScreenStyle getStyle() {
        return this.style;
    }

    @Nullable
    public Object getIngredientUnderMouse(double mouseX, double mouseY) {
        IIngredientSupplier ingredientSupplier = null;
        CustomSlotWidget guiSlot = this.getGuiSlotAt(mouseX, mouseY);
        if (guiSlot instanceof IIngredientSupplier) {
            ingredientSupplier = (IIngredientSupplier)guiSlot;
        }

        if (ingredientSupplier == null) {
            Iterator var7 = super.children.iterator();

            while(var7.hasNext()) {
                IGuiEventListener child = (IGuiEventListener)var7.next();
                if (child instanceof IIngredientSupplier && child.isMouseOver(mouseX, mouseY)) {
                    ingredientSupplier = (IIngredientSupplier)child;
                    break;
                }
            }
        }

        Object ingredient = null;
        if (ingredientSupplier != null) {
            ingredient = ingredientSupplier.getFluidIngredient();
            if (ingredient == null) {
                ingredient = ingredientSupplier.getItemIngredient();
            }
        }

        return ingredient;
    }

    static {
        TOOLTIP_HEADER = Style.EMPTY.applyFormatting(TextFormatting.WHITE);
        TOOLTIP_BODY = Style.EMPTY.applyFormatting(TextFormatting.GRAY);
    }
}
