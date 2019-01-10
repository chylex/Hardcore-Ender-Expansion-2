package chylex.hee.game.entity.projectile
import chylex.hee.HEE
import chylex.hee.game.block.BlockSimple.Builder.Companion.INDESTRUCTIBLE_HARDNESS
import chylex.hee.game.item.infusion.Infusion.HARMLESS
import chylex.hee.game.item.infusion.Infusion.PHASING
import chylex.hee.game.item.infusion.Infusion.SLOW
import chylex.hee.game.item.infusion.InfusionList
import chylex.hee.game.item.infusion.InfusionTag
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.world.util.RayTracer
import chylex.hee.game.world.util.Teleporter
import chylex.hee.system.util.Pos
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.motionVec
import chylex.hee.system.util.posVec
import chylex.hee.system.util.use
import io.netty.buffer.ByteBuf
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.RayTraceResult.Type.MISS
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData

@EventBusSubscriber(modid = HEE.ID)
class EntityProjectileEnderPearl : EntityEnderPearl, IEntityAdditionalSpawnData{
	companion object{
		private val DAMAGE_HIT_ENTITY = Damage(PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD)
		
		@JvmStatic
		@SubscribeEvent
		fun onEntityJoinWorld(e: EntityJoinWorldEvent){
			val original = e.entity
			
			if (original is EntityEnderPearl && original !is EntityProjectileEnderPearl){
				e.isCanceled = true
				e.world.spawnEntity(EntityProjectileEnderPearl(original.thrower!!, InfusionList.EMPTY))
			}
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world){
		setSize(0.35F, 0.35F)
	}
	
	constructor(thrower: EntityLivingBase, infusions: InfusionList) : super(thrower.world, thrower){
		setSize(0.35F, 0.35F)
		loadInfusions(infusions)
		shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, 0F, 1.5F, 1F)
	}
	
	private var infusions = InfusionList.EMPTY
	private var hasPhasedIntoWall = false
	private var hasPhasingFinished = false
	
	private val rayTraceIndestructible by lazy(LazyThreadSafetyMode.NONE){
		RayTracer(
			canCollideCheck = { state, pos -> state.getBlockHardness(world, pos) == INDESTRUCTIBLE_HARDNESS }
		)
	}
	
	// Initialization
	
	private fun loadInfusions(infusions: InfusionList){
		this.infusions = infusions
		this.noClip = infusions.has(PHASING)
	}
	
	override fun writeSpawnData(buffer: ByteBuf) = buffer.use {
		writeBoolean(infusions.has(HARMLESS))
		writeBoolean(infusions.has(SLOW))
	}
	
	override fun readSpawnData(buffer: ByteBuf) = buffer.use {
		var list = InfusionList.EMPTY
		
		if (readBoolean()){
			list = list.with(HARMLESS)
		}
		
		if (readBoolean()){
			list = list.with(SLOW)
		}
		
		loadInfusions(list)
	}
	
	override fun shoot(thrower: Entity, rotationPitch: Float, rotationYaw: Float, pitchOffset: Float, velocity: Float, inaccuracy: Float){
		super.shoot(thrower, rotationPitch, rotationYaw, pitchOffset, velocity, inaccuracy)
		
		if (infusions.has(SLOW)){
			motionVec = motionVec.scale(0.1)
		}
	}
	
	// Behavior
	
	override fun onUpdate(){
		val prevPos = posVec
		val prevMot = motionVec
		super.onUpdate()
		
		if (infusions.has(SLOW)){
			motionVec = prevMot.scale(0.999)
			motionY -= gravityVelocity * 0.01F
		}
		else if (!hasNoGravity()){
			motionVec = prevMot.scale(0.99)
			motionY -= gravityVelocity
		}
		
		if (!world.isRemote && infusions.has(PHASING) && hasPhasedIntoWall){
			val airCheckBox = entityBoundingBox.grow(0.15, 0.15, 0.15)
			
			if (!world.checkBlockCollision(airCheckBox) || rayTraceIndestructible.traceBlocksBetweenVectors(world, prevPos, prevPos.add(prevMot)) != null){
				hasPhasingFinished = true
				posVec = prevPos
				onImpact(RayTraceResult(MISS, prevPos, null, Pos(this)))
			}
		}
	}
	
	override fun onImpact(result: RayTraceResult){
		if (infusions.has(PHASING) && !hasPhasingFinished){
			hasPhasedIntoWall = true
			return
		}
		
		val thrower: EntityLivingBase? = thrower
		val hitEntity: Entity? = result.entityHit
		
		if (hitEntity === thrower){
			return
		}
		
		if (hitEntity != null && !infusions.has(HARMLESS)){
			DAMAGE_HIT_ENTITY.dealToIndirectly(4F, hitEntity, this, thrower)
		}
		
		if (!world.isRemote){
			val damage = if (infusions.has(HARMLESS)) 0F else 1F + world.difficulty.id
			val teleporter = Teleporter(resetFall = true, damageDealt = damage, causedInstability = 20u)
			
			if (thrower is EntityPlayerMP){
				if (thrower.connection.networkManager.isChannelOpen && thrower.world === world){
					teleporter.toLocation(thrower, posVec, SoundCategory.PLAYERS)
				}
			}
			else if (thrower != null){
				teleporter.toLocation(thrower, posVec, SoundCategory.NEUTRAL)
			}
			
			setDead()
		}
	}
	
	// Serialization
	
	override fun writeEntityToNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.writeEntityToNBT(nbt)
		
		InfusionTag.setList(this, infusions)
		setBoolean("HasPhased", hasPhasedIntoWall)
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
		
		loadInfusions(InfusionTag.getList(this))
		hasPhasedIntoWall = getBoolean("HasPhased")
	}
}
