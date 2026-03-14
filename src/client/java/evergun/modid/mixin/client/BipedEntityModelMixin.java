package evergun.modid.mixin.client;

import evergun.modid.Gun;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Inject(method = "setAngles*", at = @At("HEAD"))
    private void force3Pos(T entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof PlayerEntity player) {
            ItemStack stack = player.getMainHandStack();
            if (stack.getItem() instanceof Gun && ((Gun)stack.getItem()).getMagazine(stack) > 0) {
                if (player.getMainArm() == Arm.RIGHT) {
                    ((BipedEntityModel<?>)(Object)this).rightArmPose = BipedEntityModel.ArmPose.CROSSBOW_HOLD;
                } else {
                    ((BipedEntityModel<?>)(Object)this).leftArmPose = BipedEntityModel.ArmPose.CROSSBOW_HOLD;
                }
            }
        }
    }
}