package chylex.hee.system

// UPDATE 1.15

/**
 * Contains magic numbers and other constants which cannot be easily accessed using code, and must be reviewed before updating Minecraft versions.
 */
object MagicValues {
	/**
	 * Value of [LivingEntity.deathTime][net.minecraft.entity.LivingEntity.deathTime] animation at which an entity becomes officially dead.
	 *
	 * Found in [LivingEntity.onDeathUpdate][net.minecraft.entity.LivingEntity.onDeathUpdate].
	 */
	const val DEATH_TIME_MAX = 20
	
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
	 * Found in [SetContents.doApply][net.minecraft.loot.functions.SetContents.doApply].
	 */
	const val TILE_ENTITY_TAG = "BlockEntityTag"
}
