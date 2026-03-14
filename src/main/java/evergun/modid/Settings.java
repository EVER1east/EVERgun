package evergun.modid;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class Settings {
    public static final Item GUN = Registry.register(
            Registries.ITEM,
            new Identifier("evergun", "gunever"),
            new Gun(new FabricItemSettings().maxDamage(752))
    );

    public static void registerModItems() {
        EVERgun.LOGGER.info("Registering items...");
    }
}


