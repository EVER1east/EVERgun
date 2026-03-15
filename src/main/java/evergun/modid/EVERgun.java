package evergun.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static evergun.modid.Settings.registerModItems;

public class EVERgun implements ModInitializer {
	public static final String MOD_ID = "evergun";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        registerModItems();
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((itemGroup) -> itemGroup.add(Settings.GUN));
		LOGGER.info("Loading EVERgun...");
	}

}