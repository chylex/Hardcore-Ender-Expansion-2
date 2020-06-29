package chylex.hee.system.migration
import chylex.hee.system.util.asVoxelShape
import net.minecraft.util.math.AxisAlignedBB

// UPDATE 1.15

/**
 * Contains magic numbers and other constants which cannot be easily accessed using code, and must be reviewed before updating Minecraft versions.
 */
object MagicValues{
	/**
	 * Value of [LivingEntity.deathTime][net.minecraft.entity.LivingEntity.deathTime] animation at which an entity becomes officially dead.
	 *
	 * Found in [LivingEntity.onDeathUpdate][net.minecraft.entity.LivingEntity.onDeathUpdate].
	 */
	const val DEATH_TIME_MAX = 20
	
	/**
	 * How much an entity's collision box is reduced before checking for block collisions.
	 *
	 * Found in [Entity.doBlockCollisions][net.minecraft.entity.Entity.doBlockCollisions].
	 */
	const val BLOCK_COLLISION_SHRINK = 0.001
	val BLOCK_COLLISION_SHRINK_SHAPE = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0 - (2.0 * BLOCK_COLLISION_SHRINK), 1.0).asVoxelShape
	
	/**
	 * Default player damage added to [IItemTier.getAttackDamage][net.minecraft.item.IItemTier.getAttackDamage].
	 *
	 * Found in [PlayerEntity.registerAttributes][net.minecraft.entity.player.PlayerEntity.registerAttributes] (+ 1F).
	 */
	const val PLAYER_HAND_DAMAGE = 1F
	
	/**
	 * Frequency of triggering damage from [Effects.POISON][net.minecraft.potion.Effects.POISON].
	 *
	 * Found in [Effect.isReady][net.minecraft.potion.Effect.isReady]. Don't forget to check if more negative effect types were added that function this way.
	 */
	const val POTION_POISON_TRIGGER_RATE = 25
	
	/**
	 * Frequency of triggering damage from [Effects.WITHER][net.minecraft.potion.Effects.WITHER].
	 *
	 * Found in [Effect.isReady][net.minecraft.potion.Effect.isReady]. Don't forget to check if more negative effect types were added that function this way.
	 */
	const val POTION_WITHER_TRIGGER_RATE = 40
	
	/**
	 * Found in [AbstractFurnaceTileEntity.SLOTS_HORIZONTAL][net.minecraft.tileentity.AbstractFurnaceTileEntity.SLOTS_HORIZONTAL].
	 */
	const val FURNACE_FUEL_SLOT = 1
	
	/**
	 * Found in [SetContents.doApply][net.minecraft.world.storage.loot.functions.SetContents.doApply].
	 */
	const val TILE_ENTITY_TAG = "BlockEntityTag"
}
