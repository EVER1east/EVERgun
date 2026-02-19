package evergun.modid;

import com.sun.jna.platform.win32.WinRas;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Animation {
    public static void ItemAnimation() {
        FabricModelPredicateProviderRegistry.register(Settings.GUN, new Identifier("pull"), (itemStack, clientWorld, livingEntity, weapon) -> {
            if (livingEntity == null) {
                return 0.0F;
            }
            return livingEntity.getActiveItem() != itemStack ? 0.0F : (itemStack.getMaxUseTime() - livingEntity.getItemUseTimeLeft()) / 20.0F;
        });


        FabricModelPredicateProviderRegistry.register(Settings.GUN, new Identifier("pulling"), (itemStack, clientWorld, livingEntity, weaponActive) -> {
            if (livingEntity == null) {
                return 0.0F;
            }
            return livingEntity.isUsingItem() && livingEntity.getActiveItem() == itemStack ? 1.0F : 0.0F;
        });

        ModelPredicateProviderRegistry.register(
                Settings.GUN,
                new Identifier("count"),
                (stack, world, entity, seed) -> {
                    if (stack.hasNbt()) {
                        return stack.getNbt().getFloat("Pull");
                    }
                    return 0f;
                }
        );

        System.out.println("pull");
        System.out.println("pulling");
    }

}
