package evergun.modid.mixin.client;

import evergun.modid.Gun;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @ModifyVariable(
            method = "renderFirstPersonItem",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack treatGunAsCrossbow(ItemStack item) {
        return item;
    }
    @Redirect(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;isCharged(Lnet/minecraft/item/ItemStack;)Z")
    )
    private boolean isCharged(ItemStack stack) {
        if (stack.getItem() instanceof Gun gun) {
            return gun.getMagazine(stack) > 0;
        }
        return CrossbowItem.isCharged(stack);
    }
    @Redirect(
            method = "renderFirstPersonItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean isGun(ItemStack stack, Item item) {
        if (item == Items.CROSSBOW && stack.getItem() instanceof Gun) {
            return true;
        }
        return stack.isOf(item);
    }
}
