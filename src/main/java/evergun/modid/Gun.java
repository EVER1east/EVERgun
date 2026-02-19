package evergun.modid;

import com.google.common.collect.Lists;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class Gun extends CrossbowItem {

    int timer = 0;
    boolean bl = false;
    float global;

    public Gun(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        int ammo = getMagazine(itemStack);
        if (ammo > 0) {
            shootAll(world, user, hand, itemStack, 3.1F, 1.0F);
            int newAmmo = ammo - 1;
            setMagazine(itemStack, newAmmo);

            if (newAmmo > 0) {
                bl = false;
                putArrowBack(itemStack);
                user.getItemCooldownManager().set(this, 10);
                setCharged(itemStack, true);
            } else {
                setCharged(itemStack, false);
            }
            return TypedActionResult.consume(itemStack);
        } else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!world.isClient) {
            int usageTime = getMaxUseTime(stack) - remainingUseTicks * 3;
            int ammo = getMagazine(stack);
            timer = (int) getCount(stack);
            if (ammo >= 6) {
                if (!bl) {
                    bl = true;
                    world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_END, SoundCategory.PLAYERS, 0.9F, 1F);
                }
                return;
            }

            if (usageTime > 0 && getCount(stack) >= 20) {
                int NAmmo = ammo + 1;
                System.out.println("G" + NAmmo);
                setMagazine(stack, NAmmo);
                putArrowBack(stack);
                setCharged(stack, true);
                world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_CROSSBOW_LOADING_MIDDLE, SoundCategory.PLAYERS, 0.5F, 1.2F);
                setCount(stack, 0);
                setCharged(stack, false);
                anim(stack, 0);
            } else {
                anim(stack, timer + 1);
                setCount(stack, timer + 1);
            }
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!world.isClient) {
            setCount(stack, 0);
            anim(stack, 0);
            if (getMagazine(stack) > 0) {
                setCharged(stack, true);
            }
        }
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    public static void shootAll(World world, LivingEntity entity, Hand hand, ItemStack stack, float speed, float divergence) {
        List<ItemStack> list = getProjectiles(stack);
        float[] fs = getSoundPitches(entity.getRandom());

        for (int i = 0; i < list.size(); i++) {

            ItemStack itemStack = list.get(i);
            boolean bl = entity instanceof PlayerEntity && ((PlayerEntity)entity).getAbilities().creativeMode;
            if (!itemStack.isEmpty()) {
                shoot(world, entity, hand, stack, itemStack, fs[i], bl, speed, divergence, 0.0F);
            }
        }
        postShoot(world, entity, stack);
    }

    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        List<ItemStack> list = Lists.newArrayList();
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null && nbtCompound.contains("ChargedProjectiles", NbtElement.LIST_TYPE)) {
            NbtList nbtList = nbtCompound.getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE);
            if (nbtList != null) {
                for (int i = 0; i < nbtList.size(); i++) {
                    NbtCompound nbtCompound2 = nbtList.getCompound(i);
                    list.add(ItemStack.fromNbt(nbtCompound2));
                }
            }
        }

        return list;
    }

    private void putArrowBack(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList nbtList = new NbtList();
        NbtCompound arrowNbt = new NbtCompound();
        new ItemStack(Items.ARROW).writeNbt(arrowNbt);
        nbtList.add(arrowNbt);
        nbt.put("ChargedProjectiles", nbtList);
    }

    private static void postShoot(World world, LivingEntity entity, ItemStack stack) {
        if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
            if (!world.isClient) {
                Criteria.SHOT_CROSSBOW.trigger(serverPlayerEntity, stack);
            }

            serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        }

        clearProjectiles(stack);
    }

    private static void clearProjectiles(ItemStack crossbow) {
        NbtCompound nbtCompound = crossbow.getNbt();
        if (nbtCompound != null) {
            NbtList nbtList = nbtCompound.getList("ChargedProjectiles", NbtElement.LIST_TYPE);
            nbtList.clear();
            nbtCompound.put("ChargedProjectiles", nbtList);
        }
    }

    private static void shoot(
            World world,
            LivingEntity shooter,
            Hand hand,
            ItemStack crossbow,
            ItemStack projectile,
            float soundPitch,
            boolean creative,
            float speed,
            float divergence,
            float simulated
    ) {
        if (!world.isClient) {
            ProjectileEntity projectileEntity;
            projectileEntity = createArrow(world, shooter, crossbow, projectile);
            if (creative || simulated != 0.0F) {
                ((PersistentProjectileEntity)projectileEntity).pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
            }

            if (shooter instanceof CrossbowUser crossbowUser) {
                crossbowUser.shoot(crossbowUser.getTarget(), crossbow, projectileEntity, simulated);
            } else {
                Vec3d vec3d = shooter.getOppositeRotationVector(1.0F);
                Quaternionf quaternionf = new Quaternionf().setAngleAxis(simulated * (float) (Math.PI / 180.0), vec3d.x, vec3d.y, vec3d.z);
                Vec3d vec3d2 = shooter.getRotationVec(1.0F);
                Vector3f vector3f = vec3d2.toVector3f().rotate(quaternionf);
                projectileEntity.setVelocity(vector3f.x(), vector3f.y(), vector3f.z(), speed, divergence);
            }

            crossbow.damage(3, shooter, e -> e.sendToolBreakStatus(hand));
            world.spawnEntity(projectileEntity);
            world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, soundPitch);
        }
    }

    private static PersistentProjectileEntity createArrow(World world, LivingEntity entity, ItemStack crossbow, ItemStack arrow) {
        ArrowItem arrowItem = (ArrowItem)(arrow.getItem() instanceof ArrowItem ? arrow.getItem() : Items.ARROW);
        PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(world, arrow, entity);
        if (entity instanceof PlayerEntity) {
            persistentProjectileEntity.setCritical(true);
        }

        persistentProjectileEntity.setSound(SoundEvents.ITEM_CROSSBOW_HIT);
        persistentProjectileEntity.setShotFromCrossbow(true);
        int i = EnchantmentHelper.getLevel(Enchantments.PIERCING, crossbow);
        if (i > 0) {
            persistentProjectileEntity.setPierceLevel((byte)i);
        }

        return persistentProjectileEntity;
    }

    private static float[] getSoundPitches(Random random) {
        boolean bl = random.nextBoolean();
        return new float[]{1.0F, getSoundPitch(bl, random), getSoundPitch(!bl, random)};
    }

    private static float getSoundPitch(boolean flag, Random random) {
        float f = flag ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }


    public int getMagazine(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            return nbt.getInt("Magazine");
        }
        return 0;
    }

    public void setMagazine(ItemStack stack, int amount) {
        stack.getOrCreateNbt().putInt("Magazine", amount);
    }

    public float getCount(ItemStack stack) {
        return stack.getOrCreateNbt().getInt("count");
    }

    public void setCount(ItemStack stack, int amount) {
        stack.getOrCreateNbt().putInt("count", amount);
    }
    public void anim(ItemStack stack, float amount) {
        float f = global;
        if (amount % 2 == 0) {
            f = amount / 20;
            global = f;
        }
        System.out.println(f);
        stack.getOrCreateNbt().putFloat("animation", f);
    }
}

