package chylex.hee.system.migration

// UPDATE

/**
 * Contains magic numbers and other constants which cannot be easily accessed using code, and must be reviewed before updating Minecraft versions.
 */
object MagicValues{
	/**
	 * Value of [EntityLivingBase.deathTime][net.minecraft.entity.EntityLivingBase.deathTime] animation at which an entity becomes officially dead.
	 *
	 * Found in [EntityLivingBase.onDeathUpdate][net.minecraft.entity.EntityLivingBase.onDeathUpdate].
	 */
	const val DEATH_TIME_MAX = 20
	
	/**
	 * Default damage applied to [ItemSword][net.minecraft.item.ItemSword]s, which is added to [ToolMaterial.attackDamage][net.minecraft.item.Item.ToolMaterial.attackDamage].
	 *
	 * Found in [ItemSword][net.minecraft.item.ItemSword] constructor (+ 3F) and [SharedMonsterAttributes.ATTACK_DAMAGE][net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE] (+ 1F).
	 */
	const val SWORD_DEFAULT_DAMAGE_INCREASE = 4F
	
	/**
	 * Frequency of triggering damage from [MobEffects.POISON][net.minecraft.init.MobEffects.POISON].
	 *
	 * Found in [Potion.isReady][net.minecraft.potion.Potion.isReady]. Don't forget to check if more negative effect types were added that function this way.
	 */
	const val POTION_POISON_TRIGGER_RATE = 25
	
	/**
	 * Frequency of triggering damage from [MobEffects.WITHER][net.minecraft.init.MobEffects.WITHER].
	 *
	 * Found in [Potion.isReady][net.minecraft.potion.Potion.isReady]. Don't forget to check if more negative effect types were added that function this way.
	 */
	const val POTION_WITHER_TRIGGER_RATE = 40
	
	/**
	 * Found in [TileEntityFurnace.SLOTS_SIDES][net.minecraft.tileentity.TileEntityFurnace.SLOTS_SIDES].
	 */
	const val FURNACE_FUEL_SLOT = 1
}
