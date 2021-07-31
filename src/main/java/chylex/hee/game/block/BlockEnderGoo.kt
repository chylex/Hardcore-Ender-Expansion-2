package chylex.hee.game.block

import chylex.hee.game.block.fluid.FlowingFluid5.Companion.FLOW_DISTANCE
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.util.motionX
import chylex.hee.game.entity.util.motionZ
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.particle.ParticleEnderGoo
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.potion.util.makeInstance
import chylex.hee.game.world.util.getBlock
import chylex.hee.init.ModEffects
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.Pos
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.potion.Effect
import net.minecraft.potion.Effects
import net.minecraft.util.Direction.DOWN
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.pow

open class BlockEnderGoo : BlockAbstractGoo(FluidEnderGoo, Materials.ENDER_GOO) {
	private companion object {
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
		
		private fun addGooEffect(entity: LivingEntity, effect: Effect, durationTicks: Int, level: Int = 0) {
			val existingEffect = entity.getActivePotionEffect(effect)
			
			if (existingEffect == null || (level >= existingEffect.amplifier && durationTicks > existingEffect.duration + 30)) {
				entity.addPotionEffect(effect.makeInstance(durationTicks, level, isAmbient = true, showParticles = true))
			}
		}
		
		private fun updateGooEffects(entity: LivingEntity, totalTicks: Int) {
			addGooEffect(entity, ModEffects.LIFELESS, PERSISTENT_EFFECT_DURATION_TICKS)
			
			if (totalTicks >= 20 * 5) {
				var miningFatigueLevel = 0
				
				if (totalTicks >= 20 * 12) {
					var weaknessLevel = 0
					var weaknessDuration = 80 + 20 * ((totalTicks - (20 * 12)) / 25)
					
					if (totalTicks >= 20 * 20) {
						var poisonChancePercent = 1
						
						if (totalTicks >= 20 * 35) {
							miningFatigueLevel++
							weaknessDuration += 20 * ((totalTicks - (20 * 35)) / 25)
							
							if (totalTicks >= 20 * 50) {
								poisonChancePercent = 2
								
								if (totalTicks >= 20 * 70) {
									weaknessLevel++
								}
							}
						}
						
						if (entity.rng.nextInt(100) < poisonChancePercent && !entity.isPotionActive(Effects.POISON)) {
							addGooEffect(entity, Effects.POISON, 80 + (totalTicks - 20 * 20) / 10)
						}
					}
					
					addGooEffect(entity, Effects.WEAKNESS, weaknessDuration.coerceAtMost((20 * 60 * 3) + 19), weaknessLevel)
				}
				
				addGooEffect(entity, Effects.MINING_FATIGUE, PERSISTENT_EFFECT_DURATION_TICKS, miningFatigueLevel)
			}
		}
	}
	
	// Initialization
	
	override val tickTrackingKey
		get() = "EnderGoo"
	
	// Behavior
	
	override fun onInsideGoo(entity: Entity) {
		if (entity is LivingEntity && !CustomCreatureType.isEnder(entity)) {
			updateGooEffects(entity, trackTick(entity, MAX_COLLISION_TICK_COUNTER))
		}
		else if (entity is ItemEntity && entity.lifespan > 0) {
			--entity.lifespan
		}
	}
	
	override fun modifyMotion(entity: Entity, level: Int) {
		val world = entity.world
		val strength = ((FLOW_DISTANCE - level) / FLOW_DISTANCE.toFloat()).pow(1.75F)
		
		if (world.isRemote) {
			val rand = world.rand
			
			if (rand.nextFloat() < strength) {
				val motionMp = 12.5
				val posVec = entity.posVec.add(entity.motionX * motionMp, 0.0, entity.motionZ * motionMp)
				
				if (Pos(posVec).getBlock(world) === this) {
					PARTICLE_COLLISION.spawn(Point(posVec, 2), rand) // only triggered for the client player itself
				}
			}
		}
		
		entity.motion = entity.motion.mul(
			0.8 - (0.75 * strength),
			1.0 - (0.24 * strength),
			0.8 - (0.75 * strength)
		)
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
		if (rand.nextBoolean()) {
			val particle = if (state[LEVEL] == 0)
				PARTICLE_STATIONARY
			else
				PARTICLE_FLOWING
			
			particle.spawn(Point(pos, 1), rand)
		}
	}
}
