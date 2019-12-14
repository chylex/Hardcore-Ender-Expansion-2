package chylex.hee.game.entity.living
import chylex.hee.game.entity.living.EntityMobEndermanMuppet.Type.FIRST_KILL
import chylex.hee.game.entity.living.EntityMobEndermanMuppet.Type.INVALID
import chylex.hee.game.entity.living.behavior.EndermanTeleportHandler
import chylex.hee.game.entity.technical.EntityTechnicalCausatumEvent
import chylex.hee.game.mechanics.causatum.events.CausatumEventEndermanKill
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.getAttribute
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.posVec
import chylex.hee.system.util.selectExistingEntities
import chylex.hee.system.util.setEnum
import net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH
import net.minecraft.entity.SharedMonsterAttributes.MOVEMENT_SPEED
import net.minecraft.world.World

class EntityMobEndermanMuppet(world: World) : EntityMobAbstractEnderman(world){
	constructor(world: World, type: Type) : this(world){
		this.type = type
	}
	
	private companion object{
		private const val TYPE_TAG = "Type"
	}
	
	enum class Type{
		INVALID,
		FIRST_KILL
	}
	
	// Instance
	
	override val teleportCooldown = Int.MAX_VALUE
	private var type = INVALID
	
	override fun applyEntityAttributes(){
		super.applyEntityAttributes()
		
		getAttribute(MAX_HEALTH).baseValue = 40.0
		getAttribute(MOVEMENT_SPEED).baseValue = 0.0
		
		experienceValue = 0
	}
	
	override fun onUpdate(){
		super.onUpdate()
		
		if (ticksExisted == 1){
			setRenderYawOffset(rotationYawHead)
			prevRenderYawOffset = renderYawOffset
		}
	}
	
	override fun updateAITasks(){
		if (type == INVALID){
			setDead()
		}
		else if (type == FIRST_KILL){
			if (world.isAreaLoaded(Pos(this), 24) && world.selectExistingEntities.inRange<EntityTechnicalCausatumEvent>(posVec, 24.0).none { it.type == CausatumEventEndermanKill::class.java }){
				despawnOutOfWorld()
			}
		}
	}
	
	// Despawning
	
	fun despawnOutOfWorld(){
		EndermanTeleportHandler(this).teleportOutOfWorld(force = true)
	}
	
	override fun canDespawn(): Boolean{
		return false
	}
	
	override fun despawnEntity(){
		return
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		setEnum(TYPE_TAG, type)
	}
	
	override fun readEntityFromNBT(nbt: TagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		type = getEnum<Type>(TYPE_TAG) ?: INVALID
	}
}
