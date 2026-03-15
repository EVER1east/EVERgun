package evergun.modid.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ArrowEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public class ArrowEntityMixin {
    @Inject(method = "onHit", at = @At("HEAD"))
    private void applyEffects(LivingEntity target, CallbackInfo ci) {
        ArrowEntity arrow = (ArrowEntity) (Object) this;
        if (arrow.getCommandTags().contains("frost_1")) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 1));
        }
        if (arrow.getCommandTags().contains("wither_1")) {
            target.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
        }
    }
}
