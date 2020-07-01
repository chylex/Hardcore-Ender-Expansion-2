package chylex.hee.game.entity.living
import chylex.hee.game.entity.IImmuneToCorruptedEnergy
import chylex.hee.game.mechanics.instability.Instability
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModEntities
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.EntityLivingBase
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.isPeaceful
import chylex.hee.system.util.square
import chylex.hee.system.util.use
import net.minecraft.entity.EntityType
import net.minecraft.util.DamageSource
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World

class EntityMobEndermiteInstability(type: EntityType<EntityMobEndermiteInstability>, world: World) : EntityMobEndermite(type, world), IImmuneToCorruptedEnergy{
	constructor(world: World) : this(ModEntities.ENDERMITE_INSTABILITY, world)
	
	private companion object{
		private const val EXPLODE_TAG = "Explode"
	}
	
	private var spawnCorruptedEnergy = false
	
	override fun livingTick(){
		super.livingTick()
		
		if (world.isPeaceful && attackTarget != null){
			super.setAttackTarget(null)
		}
	}
	
	override fun setAttackTarget(newTarget: EntityLivingBase?){
		if (!world.isPeaceful){
			super.setAttackTarget(newTarget)
		}
	}
	
	override fun onDeath(cause: DamageSource){
		super.onDeath(cause)
		spawnCorruptedEnergy = true
	}
	
	override fun isDespawnPeaceful(): Boolean{
		return false
	}
	
	override fun remove(){
		if (!world.isRemote && spawnCorruptedEnergy && isAlive){
			val pos = Pos(this)
			
			Instability.get(world).triggerRelief(20u, pos)
			ModBlocks.CORRUPTED_ENERGY.spawnCorruptedEnergy(world, pos, 2)
		}
		
		super.remove()
	}
	
	override fun getLootTable(): ResourceLocation{
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
	override fun isInRangeToRenderDist(distanceSq: Double): Boolean{
		return distanceSq < square(96.0)
	}
}
