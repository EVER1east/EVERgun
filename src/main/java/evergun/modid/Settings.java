package evergun.modid;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Settings {
    public static final Item gunever = Registry.register(
            Registries.ITEM,
            new Identifier("modid", "gunever"),
            new gunever(new FabricItemSettings().maxDamage(666)) // Укажите прочность
    );

    public static void registerModItems() {
        System.out.println("test VERSUS TEST");
    }
}