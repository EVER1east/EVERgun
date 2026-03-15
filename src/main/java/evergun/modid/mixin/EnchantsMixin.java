package evergun.modid.mixin;

import evergun.modid.Gun;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
class EnchantsMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private void allowEnchants(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof Gun) {
            Enchantment self = (Enchantment) (Object) this;

            if (self == Enchantments.QUICK_CHARGE ||
                    self == Enchantments.PIERCING ||
                    self == Enchantments.FLAME ||
                    self == Enchantments.POWER ||
                    self == Enchantments.UNBREAKING ||
                    self == Enchantments.MENDING) {
                cir.setReturnValue(true);
            }
        }
    }
}
