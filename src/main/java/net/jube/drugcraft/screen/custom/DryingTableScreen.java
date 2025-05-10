package net.jube.drugcraft.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import net.jube.drugcraft.DrugCraft;
import net.jube.drugcraft.item.custom.DryingFanItem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quantiles;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class DryingTableScreen extends HandledScreen<DryingTableScreenHandler> {
    private static final Identifier GUI_TEXTURE =
            Identifier.of(DrugCraft.MOD_ID, "textures/gui/drying_table/drying_table_gui.png");
    private static final Identifier ARROW_TEXTURE =
            Identifier.of(DrugCraft.MOD_ID, "textures/gui/arrow_progress.png");

    private float fanRotationAngle = 0f;

    public DryingTableScreen(DryingTableScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(GUI_TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        renderProgressArrow(context, x, y);

        ItemStack fanStack = handler.getSlot(2).getStack();
        if (!fanStack.isEmpty() && handler.isCrafting()) {
            fanRotationAngle += delta *(handler.isCrafting() ?10f : 15f);
            if (fanRotationAngle > 360f) {
                fanRotationAngle -= 360f;
            }
            // Save the current matrix stack
            context.getMatrices().push();

            // Save current matrix
            context.getMatrices().push();

            // Translate to the center of the fan item
            context.getMatrices().translate(x + 82, y + 20, 0);
            float radians = fanRotationAngle * ((float) Math.PI / 180.0f);
            context.getMatrices().multiply(new Quaternionf().rotateZ(radians));

            // Move back so the item is drawn from its top-left corner again
            context.getMatrices().translate(-8, -8, 0);

            // Draw the fan item
            context.drawItem(fanStack, 0, 0);

            // Restore matrix
            context.getMatrices().pop();

        }
    }

    private void renderProgressArrow(DrawContext context, int x, int y) {
        if(handler.isCrafting()) {
            context.drawTexture(ARROW_TEXTURE, x + 73, y + 35, 0, 0,
                    handler.getScaledArrowProgress(), 16, 24, 16);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
