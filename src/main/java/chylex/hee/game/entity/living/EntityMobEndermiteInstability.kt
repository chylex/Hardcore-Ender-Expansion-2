package chylex.hee.game.entity.living

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockCorruptedEnergy
import chylex.hee.game.entity.util.DefaultEntityAttributes
import chylex.hee.game.entity.util.with
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.init.ModEntities
import chylex.hee.system.heeTag
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Pos
import chylex.hee.util.math.square
import chylex.hee.util.nbt.TagCompound
import chylex.hee.util.nbt.use
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.attributes.Attributes.ATTACK_DAMAGE
import net.minecraft.entity.ai.attributes.Attributes.MAX_HEALTH
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

class EntityMobEndermiteInstability(type: EntityType<EntityMobEndermiteInstability>, world: World) : EntityMobEndermite(type, world), IImmuneToCorruptedEnergy {
	@Suppress("unused")
	constructor(world: World) : this(ModEntities.ENDERMITE_INSTABILITY, world)
	
	object Type : BaseType<EntityMobEndermiteInstability>() {
		override val localization
			get() = LocalizationStrategy.Parenthesized(wordCount = 1)
		
		override val tracker
			get() = super.tracker.copy(trackingRange = 6)
		
		override val attributes
			get() = DefaultEntityAttributes.endermite.with(
				MAX_HEALTH    to 8.0,
				ATTACK_DAMAGE to 2.0,
			)
		
		override val spawnEggName
			get() = "Endermite (Instability)"
	}
	
	private companion object {
		private const val EXPLODE_TAG = "Explode"
	}
	
	private var spawnCorruptedEnergy = false
	
	override fun livingTick() {
		super.livingTick()
		
		if (world.isPeaceful && attackTarget != null) {
			super.setAttackTarget(null)
		}
	}
	
	override fun setAttackTarget(newTarget: LivingEntity?) {
		if (!world.isPeaceful) {
			super.setAttackTarget(newTarget)
		}
	}
	
	override fun onDeath(cause: DamageSource) {
		super.onDeath(cause)
		spawnCorruptedEnergy = true
	}
	
	override fun isDespawnPeaceful(): Boolean {
		return false
	}
	
	override fun remove() {
		if (!world.isRemote && spawnCorruptedEnergy && isAlive) {
			val pos = Pos(this)
			
			Instability.get(world).triggerRelief(20u, pos)
			BlockCorruptedEnergy.spawn(world, pos, 2)
		}
		
		super.remove()
	}
	
	override fun getLootTable(): ResourceLocation {
		return Resource.Custom("entities/endermite_instability")
	}
	
	// Serialization
	
	override fun writeAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.writeAdditional(nbt)
		
		putBoolean(EXPLODE_TAG, spawnCorruptedEnergy)
	}
	
	override fun readAdditional(nbt: TagCompound) = nbt.heeTag.use {
		super.readAdditional(nbt)
		
		spawnCorruptedEnergy = getBoolean(EXPLODE_TAG)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean {
		return distanceSq < square(96.0)
	}
}
