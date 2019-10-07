package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.info.Materials
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.particle.ParticleEnderGoo
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModItems
import chylex.hee.init.ModPotions
import chylex.hee.system.migration.Facing.DOWN
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.migration.vanilla.Potions
import chylex.hee.system.util.Pos
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.makeEffect
import chylex.hee.system.util.posVec
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.potion.Potion
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.pow

open class BlockEnderGoo : BlockAbstractGoo(FluidEnderGoo, Materials.ENDER_GOO){
	private companion object{
		private val PARTICLE_STATIONARY = ParticleSpawnerCustom(
			type = ParticleEnderGoo,
			pos = Constant(0.25F, UP) + InBox(0.5F, 0.05F, 0.5F),
			mot = Constant(0.11F, UP) + InBox(0.05F, 0.01F, 0.05F)
		)
		
		private val PARTICLE_FLOWING = ParticleSpawnerCustom(
			type = ParticleEnderGoo,
			pos = Constant(0.1F, DOWN) + InBox(0.4F, 0.1F, 0.4F),
			mot = Constant(0.08F, UP) + InBox(0.08F, 0.01F, 0.08F)
		)
		
		private val PARTICLE_COLLISION = ParticleSpawnerCustom(
			type = ParticleEnderGoo,
			pos = Constant(0.9F, UP) + InBox(0.75F, 0F, 0.75F),
			mot = Constant(0.11F, UP) + InBox(0.05F, 0.01F, 0.05F)
		)
		
		private const val MAX_COLLISION_TICK_COUNTER = 20 * 140
		private const val PERSISTENT_EFFECT_DURATION_TICKS = 8
		
		// Status effects
		
		private fun addGooEffect(entity: EntityLivingBase, type: Potion, durationTicks: Int, level: Int = 0){
			val existingEffect = entity.getActivePotionEffect(type)
			
			if (existingEffect == null || (level >= existingEffect.amplifier && durationTicks > existingEffect.duration + 30)){
				entity.addPotionEffect(type.makeEffect(durationTicks, level, isAmbient = true, showParticles = true))
			}
		}
		
		private fun updateGooEffects(entity: EntityLivingBase, totalTicks: Int){
			addGooEffect(entity, ModPotions.LIFELESS, PERSISTENT_EFFECT_DURATION_TICKS)
			
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
						
						if (entity.rng.nextInt(100) < poisonChancePercent && !entity.isPotionActive(Potions.POISON)){
							addGooEffect(entity, Potions.POISON, 80 + (totalTicks - 20 * 20) / 10)
						}
					}
					
					addGooEffect(entity, Potions.WEAKNESS, weaknessDuration.coerceAtMost((20 * 60 * 3) + 19), weaknessLevel)
				}
				
				addGooEffect(entity, Potions.MINING_FATIGUE, PERSISTENT_EFFECT_DURATION_TICKS, miningFatigueLevel)
			}
		}
	}
	
	// Initialization
	
	override val filledBucket
		get() = ModItems.ENDER_GOO_BUCKET
	
	override val tickTrackingKey
		get() = "EnderGoo"
	
	init{
		tickRate = 18
	}
	
	// Behavior
	
	override fun onInsideGoo(entity: Entity){
		if (entity is EntityLivingBase && !CustomCreatureType.isEnder(entity)){
			updateGooEffects(entity, trackTick(entity, MAX_COLLISION_TICK_COUNTER))
		}
		else if (entity is EntityItem && entity.lifespan > 0){
			--entity.lifespan
		}
	}
	
	override fun modifyMotion(entity: Entity, level: Int){
		val world = entity.world
		val strength = ((quantaPerBlock - level) / quantaPerBlockFloat).pow(1.75F)
		
		if (world.isRemote){
			val rand = world.rand
			
			if (rand.nextFloat() < strength){
				val motionMp = 12.5
				val posVec = entity.posVec.add(entity.motionX * motionMp, 0.0, entity.motionZ * motionMp)
				
				if (Pos(posVec).getBlock(world) === this){
					PARTICLE_COLLISION.spawn(Point(posVec, 2), rand) // only triggered for the client player itself
				}
			}
		}
		
		entity.motionX *= 0.8 - (0.75 * strength)
		entity.motionY *= 1.0 - (0.24 * strength)
		entity.motionZ *= 0.8 - (0.75 * strength)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun randomDisplayTick(state: IBlockState, world: World, pos: BlockPos, rand: Random){
		if (rand.nextBoolean()){
			val particle = if (state[LEVEL] == 0)
				PARTICLE_STATIONARY
			else
				PARTICLE_FLOWING
			
			particle.spawn(Point(pos, 1), rand)
		}
	}
}
