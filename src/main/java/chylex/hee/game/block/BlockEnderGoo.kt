package chylex.hee.game.block
import chylex.hee.game.block.BlockAbstractGoo.Companion.CollisionTickerBase
import chylex.hee.game.block.BlockEnderGoo.Companion.CollisionTicker.Provider
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.material.Materials
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.mechanics.potion.PotionLifeless.LIFELESS
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.util.forge.capabilities.CapabilityProvider
import chylex.hee.system.util.forge.capabilities.NullFactory
import chylex.hee.system.util.forge.capabilities.NullStorage
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.MobEffects.MINING_FATIGUE
import net.minecraft.init.MobEffects.POISON
import net.minecraft.init.MobEffects.WEAKNESS
import net.minecraft.nbt.NBTTagInt
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.pow

open class BlockEnderGoo : BlockAbstractGoo(FluidEnderGoo, Materials.ENDER_GOO){
	private companion object{
		
		// Collision tracking
		
		private const val MAX_COLLISION_TICK_COUNTER = 20 * 140
		
		@JvmStatic
		@CapabilityInject(CollisionTicker::class)
		private var CAP_COLLISION_TICKER: Capability<CollisionTicker>? = null
		
		private val CAP_KEY = Resource.Custom("goo")
		
		init{
			CapabilityManager.INSTANCE.register(CollisionTicker::class.java, NullStorage.get(), NullFactory.get())
			MinecraftForge.EVENT_BUS.register(this)
		}
		
		@SubscribeEvent
		fun onAttachCapabilities(e: AttachCapabilitiesEvent<Entity>){
			val entity = e.`object`
			
			if (entity is EntityLivingBase){
				e.addCapability(CAP_KEY, Provider(entity.world.totalWorldTime))
			}
		}
		
		private class CollisionTicker private constructor(lastWorldTime: Long) : CollisionTickerBase(lastWorldTime, MAX_COLLISION_TICK_COUNTER){
			class Provider(worldTime: Long) : CapabilityProvider<CollisionTicker, NBTTagInt>(CAP_COLLISION_TICKER!!, CollisionTicker(worldTime))
		}
		
		// Status effects
		
		private fun updateGooEffects(entity: EntityLivingBase, totalTicks: Int){
			addGooEffect(entity, LIFELESS, PERSISTENT_EFFECT_DURATION_TICKS)
			
			if (totalTicks >= 20 * 5){
				var miningFatigueLevel = 0
				
				if (totalTicks >= 20 * 12){
					var weaknessLevel = 0
					var weaknessDuration = 80 + 20 * ((totalTicks - (20 * 12)) / 25)
					
					if (totalTicks >= 20 * 20){
						var poisonChancePercent = 1
						
						if (totalTicks >= 20 * 35){
							miningFatigueLevel++
							weaknessDuration += 20 * ((totalTicks - (20 * 35)) / 25)
							
							if (totalTicks >= 20 * 50){
								poisonChancePercent = 2
								
								if (totalTicks >= 20 * 70){
									weaknessLevel++
								}
							}
						}
						
						if (entity.rng.nextInt(100) < poisonChancePercent && !entity.isPotionActive(POISON)){
							addGooEffect(entity, POISON, 80 + (totalTicks - 20 * 20) / 10)
						}
					}
					
					addGooEffect(entity, WEAKNESS, weaknessDuration.coerceAtMost((20 * 60 * 3) + 19), weaknessLevel)
				}
				
				addGooEffect(entity, MINING_FATIGUE, PERSISTENT_EFFECT_DURATION_TICKS, miningFatigueLevel)
			}
		}
	}
	
	// Initialization
	
	override val filledBucket
		get() = ModItems.ENDER_GOO_BUCKET
	
	// Behavior
	
	override fun modifyEntityMotion(entity: Entity, level: Int){
		val strength = ((quantaPerBlock - level) / quantaPerBlockFloat).pow(1.75F)
		
		entity.motionX *= 0.8 - (0.75 * strength)
		entity.motionY *= 1.0 - (0.24 * strength)
		entity.motionZ *= 0.8 - (0.75 * strength)
	}
	
	override fun onEntityCollidedWithBlock(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		super.onEntityCollidedWithBlock(world, pos, state, entity)
		
		if (entity is EntityLivingBase){
			if (!world.isRemote && !CustomCreatureType.isEnder(entity)){
				entity.getCapability(CAP_COLLISION_TICKER!!, null)?.let {
					val totalTicks = it.tick(world.totalWorldTime, entity.rng)
					
					if (totalTicks != IGNORE_COLLISION_TICK){
						updateGooEffects(entity, totalTicks)
					}
				}
			}
		}
		else if (entity is EntityItem){
			if (!world.isRemote && entity.lifespan > 0){
				--entity.lifespan
			}
		}
	}
}
