package evergun.modid;

import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class Gun extends Item {
    static int del = 20;

    public Gun(FabricItemSettings fabricItemSettings) {
        super(fabricItemSettings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.CROSSBOW;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (getMagazine(stack) > 0) {
            if (!world.isClient) {
                ItemStack arrowToShoot = removeProjectile(stack);
                user.getItemCooldownManager().set(stack.getItem(), 5);
                shoot(world, user, hand, stack, arrowToShoot, 1.0f, user.getAbilities().creativeMode);
            }
        }
        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (world.isClient || !(user instanceof PlayerEntity player)) return;

        int usedTicks = getMaxUseTime(stack) - remainingUseTicks;
        setAnim(stack, usedTicks % del);

        if (usedTicks > 0 && usedTicks % del == 0) {
            int magazine = getMagazine(stack);

            if (magazine < 6) {
                ItemStack ammoStack = find(player);
                if (player.getAbilities().creativeMode || !ammoStack.isEmpty()) {
                    ItemStack arrowToSave = ammoStack.copy();
                    arrowToSave.setCount(1);

                    if (!player.getAbilities().creativeMode) {
                        ammoStack.decrement(1);
                    }

                    addProjectile(stack, arrowToSave);

                    world.playSound(null, user.getX(), user.getY(), user.getZ(),
                            SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 1.0f, 1.0f + (magazine * 0.1f));
                }
            }
        }
    }

    private ItemStack find(PlayerEntity player) {
        if (player.getOffHandStack().isIn(ItemTags.ARROWS)) {
            return player.getOffHandStack();
        }
        if (player.getMainHandStack().isIn(ItemTags.ARROWS)) {
            return player.getMainHandStack();
        }
        for (int i = 0; i < player.getInventory().size(); ++i) {
            ItemStack itemStack = player.getInventory().getStack(i);
            if (itemStack.isIn(ItemTags.ARROWS)) {
                return itemStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        setAnim(stack, 0);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient) {
            world.playSound(null, user.getX(), user.getY(), user.getZ(),
                    SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
        setAnim(stack, 0);
        return stack;
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    private static void shoot(
            World world,
            LivingEntity shooter,
            Hand hand,
            ItemStack crossbow,
            ItemStack projectile,
            float soundPitch,
            boolean creative
    ) {
        if (!world.isClient) {
            PersistentProjectileEntity projectileEntity;
            projectileEntity = createArrow(world, shooter, crossbow, projectile);
            if (creative) {
                projectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }

            if (shooter instanceof CrossbowUser user) {
                user.shoot(user.getTarget(), crossbow, projectileEntity, 0);
            }
            crossbow.damage(3, shooter, e -> e.sendToolBreakStatus(hand));
            world.spawnEntity(projectileEntity);
            Vec3d vec3d = shooter.getOppositeRotationVector(1.0F);
            Quaternionf quaternionf = new Quaternionf().setAngleAxis(1 * (float) (Math.PI / 180.0), vec3d.x, vec3d.y, vec3d.z);
            Vec3d vec3d2 = shooter.getRotationVec(1.0F);
            Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
            projectileEntity.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), 3, 1);
            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, soundPitch);
        }
    }

    private static PersistentProjectileEntity createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrowStack) {
        ArrowItem arrowItem = (ArrowItem) (arrowStack.getItem() instanceof ArrowItem ? arrowStack.getItem() : Items.ARROW);
        PersistentProjectileEntity projectile = arrowItem.createArrow(world, arrowStack, entity);

        if (entity instanceof PlayerEntity) {
            projectile.setCritical(true);
        }
        projectile.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
        projectile.setShotFromCrossbow(true);
        return projectile;
    }

    public static void setAnim(ItemStack stack, int amount) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putFloat("animation", (float) amount / del);
    }

    public void addProjectile(ItemStack gun, ItemStack arrow) {
        NbtCompound nbt = gun.getOrCreateNbt();
        NbtList list = nbt.getList("LoadedArrows", NbtElement.COMPOUND_TYPE);

        NbtCompound arrowNbt = new NbtCompound();
        arrow.writeNbt(arrowNbt);

        list.add(arrowNbt);
        nbt.put("LoadedArrows", list);

        nbt.putInt("magazine", list.size());
        nbt.putBoolean("Charged", true);
    }

    public ItemStack removeProjectile(ItemStack gun) {
        NbtCompound nbt = gun.getNbt();
        if (nbt != null && nbt.contains("LoadedArrows", NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList("LoadedArrows", NbtElement.COMPOUND_TYPE);
            if (!list.isEmpty()) {
                NbtCompound arrowNbt = list.getCompound(list.size() - 1);
                ItemStack arrow = ItemStack.fromNbt(arrowNbt);

                list.removeLast();
                nbt.putInt("magazine", list.size());
                nbt.putBoolean("Charged", !list.isEmpty());

                return arrow;
            }
        }
        return new ItemStack(Items.ARROW);
    }

    public int getMagazine(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("LoadedArrows", NbtElement.LIST_TYPE)) {
            return nbt.getList("LoadedArrows", NbtElement.COMPOUND_TYPE).size();
        }
        return 0;
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null && nbtCompound.contains("LoadedArrows", 9)) {
            NbtList nbtList = nbtCompound.getList("LoadedArrows", 10);
            if (nbtList != null) {
                for (int i = 0; i < nbtList.size(); ++i) {
                    NbtCompound nbtCompound2 = nbtList.getCompound(i);
                    list.add(ItemStack.fromNbt(nbtCompound2));
                }
            }
        }

        return list;
    }
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        List<ItemStack> list = getProjectiles(stack);
        if (!list.isEmpty()) {
            ItemStack itemStack = (ItemStack)list.get(getMagazine(stack) -1);
            tooltip.add(Text.translatable("item.minecraft.crossbow.projectile").append(ScreenTexts.SPACE).append(itemStack.toHoverableText()));
        }
    }

    /**
     * {@return the maximum durability of this item} Can be configured through {@link Item.Settings#maxDamage(int) settings.maxDamage()}.
     */
}




