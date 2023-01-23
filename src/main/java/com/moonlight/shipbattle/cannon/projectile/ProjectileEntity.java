package com.moonlight.shipbattle.cannon.projectile;

import com.moonlight.shipbattle.Main;
import com.moonlight.shipbattle.Utils;
import com.moonlight.shipbattle.configuration.Configuration;
import com.moonlight.shipbattle.teams.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftMagicNumbers;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.*;

public class ProjectileEntity extends FallingBlockEntity {
    private Team shooterTeam;
    private Player shooter;
    private ProjectileType type;
    private Direction direction;

    public Player getShooter() {
        return this.shooter;
    }

    public Team getShooterTeam() {
        return this.shooterTeam;
    }

    public ProjectileEntity(final Level world, final double d0, final double d1, final double d2, BlockState iblockdata, Direction direction) {
        super(EntityType.FALLING_BLOCK, world);
        try {
            Field f = this.getClass().getSuperclass().getDeclaredField("ao");
            f.setAccessible(true);
            f.set(this, iblockdata);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
        this.setStartPos(new BlockPos(d0,d1,d2));
        this.teleportTo(d0,d1,d2);
    }

    @Override
    public void tick() {

        final Block block = this.getBlockState().getBlock();

        if (this.tickCount++ == 0) {
            final BlockPos blockposition = this.blockPosition();
            if (this.level.getBlockState(blockposition).is(block) && !CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, Blocks.AIR.defaultBlockState()).isCancelled()) {
                this.level.removeBlock(blockposition, false);
            }
        }

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        if (!this.level.isClientSide) {
            BlockPos blockposition = this.blockPosition();
            final boolean flag = this.getBlockState().getBlock() instanceof ConcretePowderBlock;
            boolean flag2 = flag && this.level.getFluidState(blockposition).is(FluidTags.WATER);
            final double d0 = this.getDeltaMovement().lengthSqr();
            if (flag && d0 > 1.0) {
                final HitResult movingobjectpositionblock = this.level.clip(new ClipContext(new Vec3(this.xOld, this.yOld, this.zOld), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this));
                if (movingobjectpositionblock.getType() != HitResult.Type.MISS && this.level.getFluidState(new BlockPos(movingobjectpositionblock.getLocation())).is(FluidTags.WATER)) {
                    blockposition = new BlockPos(movingobjectpositionblock.getLocation());
                    flag2 = true;
                }
            }
            if (!this.onGround && !flag2) {
                if ((this.tickCount > 100 && (blockposition.getY() < 1 || blockposition.getY() > 256)) || this.tickCount > 600) {
                    this.discard();
                    Bukkit.getLogger().severe("Debug 2");

                }
            }else{
                final BlockState iblockdata = this.level.getBlockState(blockposition);
                if (!flag2 && FallingBlock.isFree(this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 0.01, this.getZ())))) {
                    this.onGround = false;
                }
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
                if (!iblockdata.is(Blocks.MOVING_PISTON)) {
                    this.discard();
                    this.playEffect();
                    Bukkit.getLogger().severe("Debug 3");

                }
            }
        }
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));


        BlockState type1 = this.level.getBlockState(this.blockPosition());
        net.minecraft.world.level.material.Material material = type1.getMaterial();

        if (this.xOld == getX() || this.zOld == getZ() || material != net.minecraft.world.level.material.Material.AIR) {
            Bukkit.getLogger().severe("Debug 4");
            this.discard();
            this.playEffect();
        }
    }

    public static void spawnProjectile(final Location location, final Team shooterTeam, final Player shooter, final ProjectileType type, final Vector velocity) {
        Direction d = Direction.WTF_NOT_A_CANON_LOL;
        if (velocity.getX() != 0.0D && velocity.getZ() == 0.0D) {
            d = Direction.X;
        } else if (velocity.getX() == 0.0D && velocity.getZ() != 0.0D) {
            d = Direction.Z;
        }
        final Level world = Objects.requireNonNull((CraftWorld) location.getWorld()).getHandle();
        final BlockState data = CraftMagicNumbers.getBlock(type.getItemStack().getType()).defaultBlockState();
        final Item item = CraftMagicNumbers.getItem(type.getItemStack().getType());

        ProjectileEntity projectile = new ProjectileEntity(world, location.getX() + 0.5, location.getY() + 0.5, location.getZ() + 0.5, data, d);
        projectile.getBukkitEntity().setVelocity(velocity);
        projectile.tickCount = 1;
        projectile.shooter = shooter;
        projectile.shooterTeam = shooterTeam;
        projectile.type = type;

        world.addFreshEntity(projectile);
    }

    private void playEffect() {
        final Location location = this.getBukkitEntity().getLocation().clone();
        final Material[] liquids = {Material.WATER, Material.LEGACY_STATIONARY_WATER, Material.LAVA, Material.LEGACY_STATIONARY_LAVA};
        final org.bukkit.World arenaWorld = this.shooterTeam.getGame().getArena().getWorld();
        if (Arrays.asList(liquids).contains(location.getBlock().getType()) || Utils.isInArea(location, arenaWorld, this.shooterTeam.getArea()[0], this.shooterTeam.getArea()[1])) {
            return;
        }
        Objects.requireNonNull(location.getWorld()).playSound(location, Sound.BLOCK_STONE_FALL, 1.0f, 1.0f);
        location.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        location.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, location, 2);
        switch (this.type.getEffect()) {
            case EXPLOSION -> this.playExplosionEffect();
            case FIRE -> this.playFireEffect();
            case SMOKE -> this.playSmokeEffect();
            case LIGHTNING -> this.playLightningEffect();
        }
    }

    private void playExplosionEffect() {
        Explosion explosion = new Explosion(this.level, this, null, null, this.xOld, this.yOld / 2.0f, this.zOld, Configuration.cannonballExplosionStrength, false, Explosion.BlockInteraction.DESTROY);

        explosion.explode();

        List<BlockPos> blocks = explosion.getToBlow();
        List<BlockPos> toRemove = new ArrayList<>();

        blocks.forEach(blockPosition -> {
            Location location = new Location(this.level.getWorld(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());

            if (location.getBlock().getType() == Material.LADDER) {
                toRemove.add(blockPosition);
            }
        });

        blocks.removeAll(toRemove);
        explosion.finalizeExplosion(true);
    }

    private void playFireEffect() {
        final Location location = this.getBukkitEntity().getLocation().clone();
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                for (int dy = 0; dy <= 2; ++dy) {
                    final Location loc = location.getBlock().getLocation().add(dx, dz, dy);
                    if (loc.getBlock().getType() == Material.AIR && loc.clone().subtract(0.0, 1.0, 0.0).getBlock().getType().isSolid()) {
                        loc.getBlock().setType(Material.FIRE);
                    }
                }
            }
        }
    }

    private void playSmokeEffect() {
        final org.bukkit.World world = this.level.getWorld();
        final Random random = new Random();
        new BukkitRunnable() {
            int ticks;

            public void run() {
                if (this.ticks > 20) {
                    this.cancel();
                    return;
                }
                ++this.ticks;
                for (int i = 0; i <= 10; ++i) {
                    final double dx = (random.nextInt(50) - 25) / 10.0;
                    final double dy = random.nextInt(25) / 10.0;
                    final double dz = (random.nextInt(50) - 25) / 10.0;
                    world.playEffect(new Location(world, ProjectileEntity.this.xOld + dx, ProjectileEntity.this.yOld + dy, ProjectileEntity.this.zOld + dz), Effect.SMOKE, 0);
                }
            }
        }.runTaskTimer(Main.getMain(), 0L, 1L);
        for (final Player player : this.shooterTeam.getEnemyTeam().getPlayers()) {
            final Location min = this.shooterTeam.getEnemyTeam().getArea()[0];
            final Location max = this.shooterTeam.getEnemyTeam().getArea()[1];
            if (Utils.isInArea(player.getLocation(), world, min, max)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0, false, false, true));
            }
        }
    }

    private void playLightningEffect() {
        final Location location = this.getBukkitEntity().getLocation().clone();
        final org.bukkit.World world = location.getWorld();
        final int[][] delta = {{-2, -2}, {-2, 0}, {-2, 2}, {0, 2}, {2, 2}, {2, 0}, {2, -2}, {0, -2}};
        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();
        new BukkitRunnable() {
            int i = 0;

            public void run() {
                if (this.i > 7) {
                    ProjectileEntity.this.strikeLightning(location);
                    assert world != null;
                    final Creeper creeper = (Creeper) world.spawnEntity(location, org.bukkit.entity.EntityType.CREEPER);
                    creeper.setPowered(true);
                    Objects.requireNonNull(creeper.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(30.0);
                    Objects.requireNonNull(creeper.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.4);
                    creeper.setHealth(30.0);
                    this.cancel();
                } else {
                    final Location loc = new Location(world, (double) (x + delta[this.i][0]), (double) y, (double) (z + delta[this.i][1]));
                    ProjectileEntity.this.strikeLightning(loc);
                }
                ++this.i;
            }
        }.runTaskTimer(Main.getMain(), 0L, 4L);
    }

    private void strikeLightning(final Location location) {
        Objects.requireNonNull(location.getWorld()).strikeLightningEffect(location);
        for (final Player player : this.shooterTeam.getGame().getPlayers()) {
            if (player.getLocation().distance(location) < 1.5) {
                player.damage(13.0);
            }
        }
    }

    private enum Direction {
        X, Z, WTF_NOT_A_CANON_LOL;
    }
}
