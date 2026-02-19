package evergun.modid;

import net.fabricmc.api.ClientModInitializer;

import static evergun.modid.Animation.registerAnimation;

public class EVERgunClient implements ClientModInitializer {

	@Override
	public void onInitializeClient()  {
        registerAnimation();
	}
}