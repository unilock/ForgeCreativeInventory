package cc.unilock.forgecreativeinv.mixin;

import net.fabricmc.fabric.impl.client.item.group.CreativeGuiExtensions;
import net.fabricmc.fabric.impl.client.item.group.FabricCreativeGuiComponents;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin<T extends ScreenHandler> extends AbstractInventoryScreen<T> implements CreativeGuiExtensions {
    public CreativeInventoryScreenMixin(ScreenHandler container_1, PlayerInventory playerInventory_1, Text textComponent_1) {
        super((T) container_1, playerInventory_1, textComponent_1);
    }

    @Shadow
    protected abstract void setSelectedTab(ItemGroup itemGroup_1);

    @Shadow
    public abstract int getSelectedTab(); /* XXX getSelectedTab XXX */

    // "static" matches selectedTab
    private static int fabric_currentPage = 0;
    private static int fabric_maxPages = (int) Math.ceil((ItemGroup.GROUPS.length - FabricCreativeGuiComponents.COMMON_GROUPS.size()) / 9D);

    private int fabric_getPageOffset(int page) {
        return switch (page) {
            case 0 -> 0;
            case 1 -> 12;
            default -> 12 + ((12 - FabricCreativeGuiComponents.COMMON_GROUPS.size()) * (page - 1));
        };
    }

    private int fabric_getOffsetPage(int offset) {
        if (offset < 12) {
            return 0;
        } else {
            return 1 + ((offset - 12) / (12 - FabricCreativeGuiComponents.COMMON_GROUPS.size()));
        }
    }

    @Override
    public void fabric_nextPage() {
        if (fabric_getPageOffset(fabric_currentPage + 1) >= ItemGroup.GROUPS.length) {
            return;
        }

        fabric_currentPage++;
        fabric_updateSelection();
    }

    @Override
    public void fabric_previousPage() {
        if (fabric_currentPage == 0) {
            return;
        }

        fabric_currentPage--;
        fabric_updateSelection();
    }

    @Override
    public boolean fabric_isButtonVisible(FabricCreativeGuiComponents.Type type) {
        return false;
    }

    @Override
    public boolean fabric_isButtonEnabled(FabricCreativeGuiComponents.Type type) {
        return false;
    }

    private void fabric_updateSelection() {
        int minPos = fabric_getPageOffset(fabric_currentPage);
        int maxPos = fabric_getPageOffset(fabric_currentPage + 1) - 1;
        int curPos = getSelectedTab();

        if (curPos < minPos || curPos > maxPos) {
            setSelectedTab(ItemGroup.GROUPS[fabric_getPageOffset(fabric_currentPage)]);
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo info) {
        fabric_updateSelection();

        //int xpos = x + 116;
        //int ypos = y - 10;

        if (fabric_maxPages > 1) {
            addDrawableChild(new ButtonWidget(x, y - 50, 20, 20, Text.literal("<"), b -> fabric_previousPage()));
            addDrawableChild(new ButtonWidget(x + backgroundWidth - 20, y - 50, 20, 20, Text.literal(">"), b -> fabric_nextPage()));
        }

        //addDrawableChild(new FabricCreativeGuiComponents.ItemGroupButtonWidget(xpos + 11, ypos, FabricCreativeGuiComponents.Type.NEXT, this));
        //addDrawableChild(new FabricCreativeGuiComponents.ItemGroupButtonWidget(xpos, ypos, FabricCreativeGuiComponents.Type.PREVIOUS, this));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (fabric_maxPages > 1) {
            Text page = Text.literal(String.format("%d / %d", fabric_currentPage() + 1, fabric_maxPages));
            this.setZOffset(300);
            this.itemRenderer.zOffset = 300.0F;
            textRenderer.drawWithShadow(matrices, page.asOrderedText(), x + ((float) backgroundWidth / 2) - ((float) textRenderer.getWidth(page) / 2), y - 44, -1);
            this.setZOffset(0);
            this.itemRenderer.zOffset = 0.0F;
        }
    }

    @Inject(method = "setSelectedTab", at = @At("HEAD"), cancellable = true)
    private void setSelectedTab(ItemGroup itemGroup, CallbackInfo info) {
        if (!fabric_isGroupVisible(itemGroup)) {
            info.cancel();
        }
    }

    @Inject(method = "renderTabTooltipIfHovered", at = @At("HEAD"), cancellable = true)
    private void renderTabTooltipIfHovered(MatrixStack matrixStack, ItemGroup itemGroup, int mx, int my, CallbackInfoReturnable<Boolean> info) {
        if (!fabric_isGroupVisible(itemGroup)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "isClickInTab", at = @At("HEAD"), cancellable = true)
    private void isClickInTab(ItemGroup itemGroup, double mx, double my, CallbackInfoReturnable<Boolean> info) {
        if (!fabric_isGroupVisible(itemGroup)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "renderTabIcon", at = @At("HEAD"), cancellable = true)
    private void renderTabIcon(MatrixStack matrixStack, ItemGroup itemGroup, CallbackInfo info) {
        if (!fabric_isGroupVisible(itemGroup)) {
            info.cancel();
        }
    }

    private boolean fabric_isGroupVisible(ItemGroup itemGroup) {
        if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(itemGroup)) {
            return true;
        }

        return fabric_currentPage == fabric_getOffsetPage(itemGroup.getIndex());
    }

    @Override
    public int fabric_currentPage() {
        return fabric_currentPage;
    }
}
