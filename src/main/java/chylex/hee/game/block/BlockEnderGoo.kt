package chylex.hee.game.block
import chylex.hee.game.block.fluid.FluidEnderGoo
import chylex.hee.game.block.material.Materials
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.mechanics.potion.PotionLifeless.LIFELESS
import chylex.hee.game.particle.ParticleEnderGoo
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.util.IOffset.Constant
import chylex.hee.game.particle.util.IOffset.InBox
import chylex.hee.game.particle.util.IShape.Point
import chylex.hee.init.ModItems
import chylex.hee.system.util.Pos
import chylex.hee.system.util.get
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.posVec
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.MobEffects.MINING_FATIGUE
import net.minecraft.init.MobEffects.POISON
import net.minecraft.init.MobEffects.WEAKNESS
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
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
	
	@SideOnly(Side.CLIENT)
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
