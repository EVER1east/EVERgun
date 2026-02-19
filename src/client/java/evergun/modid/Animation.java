package evergun.modid;

import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class Animation {
    public static void registerAnimation() {

        ModelPredicateProviderRegistry.register(Settings.GUN, new Identifier("animation"), (stack, world, entity, seed) -> {
            if (stack.hasNbt()) {
                assert stack.getNbt() != null;
                return stack.getNbt().getFloat("animation");
            }
            return 0.0F;
        });

        ModelPredicateProviderRegistry.register(Settings.GUN, new Identifier("ammo"), (stack, world, entity, seed) -> {
            if (stack.hasNbt()) {
                assert stack.getNbt() != null;
                return (float) stack.getNbt().getInt("Magazine");
            }
            return 0.0F;
        });

        ModelPredicateProviderRegistry.register(Settings.GUN, new Identifier("animation_with_arrow"), (stack, world, entity, seed) -> {
            if (stack.hasNbt()) {
                assert stack.getNbt() != null;
                float f = stack.getNbt().getFloat("animation");
                int a = stack.getNbt().getInt("Magazine");
                if (a > 0 && f > 0) {
                    return f;
                }
            }
            return 0.0F;
        });
    }
}