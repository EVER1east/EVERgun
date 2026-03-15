package evergun.modid.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEnchantments {
    public static final Enchantment FROST = Registry.register(
            Registries.ENCHANTMENT,
            new Identifier("evergun", "frost"),
            new FrostEnchantment()
    );

    public static final Enchantment WITHER = Registry.register(
            Registries.ENCHANTMENT,
            new Identifier("evergun", "wither"),
            new FrostEnchantment()
    );

    public static void register() {

    }
}
