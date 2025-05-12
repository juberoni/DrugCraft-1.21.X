package net.jube.drugcraft.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jube.drugcraft.DrugCraft;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WoodDryingTableScreen extends HandledScreen<WoodDryingTableScreenHandler> {
    private static final Identifier WOOD_GUI_TEXTURE =
            Identifier.of(DrugCraft.MOD_ID, "textures/gui/drying_table/wood_drying_table_gui.png");
    private static final Identifier WOOD_ARROW_TEXTURE =
            Identifier.of(DrugCraft.MOD_ID, "textures/gui/arrow_progress_vert.png");


    public WoodDryingTableScreen(WoodDryingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, WOOD_GUI_TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(WOOD_GUI_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        renderProgressArrow(context, x, y);
    }


    private void renderProgressArrow(DrawContext context, int guiLeftX, int guiTopY) {
        if (handler.isCrafting()) {
            int progressHeight = handler.getScaledArrowProgress();
            if (progressHeight > 0) {
                int arrowScreenX = guiLeftX + 81;
                int arrowScreenY = guiTopY + 43;

                context.drawTexture(WOOD_ARROW_TEXTURE,
                        arrowScreenX, arrowScreenY,
                        0, 0,
                        16, progressHeight);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
