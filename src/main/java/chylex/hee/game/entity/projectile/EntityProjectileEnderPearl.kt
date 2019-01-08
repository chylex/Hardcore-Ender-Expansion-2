package chylex.hee.game.entity.projectile
import chylex.hee.HEE
import chylex.hee.game.mechanics.damage.Damage
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.ALL_PROTECTIONS_WITH_SHIELD
import chylex.hee.game.mechanics.damage.IDamageProcessor.Companion.PEACEFUL_EXCLUSION
import chylex.hee.game.world.util.Teleporter
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.posVec
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@EventBusSubscriber(modid = HEE.ID)
class EntityProjectileEnderPearl : EntityEnderPearl{
	companion object{
		private val DAMAGE_HIT_ENTITY = Damage(PEACEFUL_EXCLUSION, *ALL_PROTECTIONS_WITH_SHIELD)
		
		@JvmStatic
		@SubscribeEvent
		fun onEntityJoinWorld(e: EntityJoinWorldEvent){
			val original = e.entity
			
			if (original is EntityEnderPearl && original !is EntityProjectileEnderPearl){
				e.isCanceled = true
				e.world.spawnEntity(EntityProjectileEnderPearl(original.thrower!!))
			}
		}
	}
	
	@Suppress("unused")
	constructor(world: World) : super(world)
	
	constructor(thrower: EntityLivingBase) : super(thrower.world, thrower){
		shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, 0F, 1.5F, 1F)
	}
	
	// Impact
	
	override fun onImpact(result: RayTraceResult){
		val thrower: EntityLivingBase? = thrower
		val hitEntity: Entity? = result.entityHit
		
		if (hitEntity === thrower){
			return
		}
		
		if (hitEntity != null){
			DAMAGE_HIT_ENTITY.dealToIndirectly(4F, hitEntity, this, thrower)
		}
		
		if (!world.isRemote){
			val teleporter = Teleporter(resetFall = true, damageDealt = 1F + world.difficulty.id, causedInstability = 20u)
			
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
	}
	
	override fun readEntityFromNBT(nbt: NBTTagCompound) = with(nbt.heeTag){
		super.readEntityFromNBT(nbt)
	}
}
