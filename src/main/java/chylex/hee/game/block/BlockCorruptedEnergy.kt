package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.game.Resource.location
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.FAIL
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.PASSTHROUGH
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.SUCCESS
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockClientEffectsComponent
import chylex.hee.game.block.components.IBlockCollideWithEntityComponent
import chylex.hee.game.block.components.IBlockRandomTickComponent
import chylex.hee.game.block.components.IBlockScheduledTickComponent
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.Materials
import chylex.hee.game.block.properties.TickSchedule
import chylex.hee.game.block.util.Property
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.CustomCreatureType
import chylex.hee.game.entity.damage.CombinedDamage
import chylex.hee.game.entity.damage.Damage
import chylex.hee.game.entity.damage.IDamageDealer.Companion.TITLE_MAGIC
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ARMOR_PROTECTION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.DIFFICULTY_SCALING
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.ENCHANTMENT_PROTECTION
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.MAGIC_TYPE
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.NUDITY_DANGER
import chylex.hee.game.entity.damage.IDamageProcessor.Companion.RAPID_DAMAGE
import chylex.hee.game.entity.living.IImmuneToCorruptedEnergy
import chylex.hee.game.particle.ParticleCorruptedEnergy
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.FLAG_NONE
import chylex.hee.game.world.util.FLAG_SYNC_CLIENT
import chylex.hee.game.world.util.Facing6
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.getTile
import chylex.hee.game.world.util.removeBlock
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModBlocks
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.removeItem
import net.minecraft.block.BlockRenderType.INVISIBLE
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

object BlockCorruptedEnergy : HeeBlockBuilder() {
	private const val MIN_LEVEL = 0
	private const val MAX_LEVEL = 20
	
	private const val MAX_TICK_RATE = 5
	private const val MIN_TICK_RATE = 1
	
	val LEVEL = Property.int("level", MIN_LEVEL..MAX_LEVEL)
	
	private val DAMAGE_PART_NORMAL = Damage(DIFFICULTY_SCALING, ARMOR_PROTECTION(false), ENCHANTMENT_PROTECTION)
	private val DAMAGE_PART_MAGIC = Damage(MAGIC_TYPE, NUDITY_DANGER, RAPID_DAMAGE(2))
	
	private val PARTICLE_CORRUPTION = ParticleSpawnerCustom(
		type = ParticleCorruptedEnergy,
		pos = InBox(0.75F),
		mot = InBox(0.05F),
		hideOnMinimalSetting = false
	)
	
	init {
		localization = LocalizationStrategy.None
		model = BlockStateModel(BlockStatePreset.Simple, BlockModel.NoAmbientOcclusion(BlockModel.Cross(Blocks.BARRIER.asItem().location)))
		// renderLayer = CUTOUT // Debugging
		
		material = Materials.CORRUPTED_ENERGY
		color = MaterialColor.PURPLE
		sound = SoundType.SAND
		
		isSolid = false
		
		drop = BlockDrop.Nothing
		
		components.states.set(LEVEL, MIN_LEVEL)
		
		components.renderType = INVISIBLE
		components.ambientOcclusionValue = 1F
		
		components.clientEffects = object : IBlockClientEffectsComponent {
			override fun randomTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
				val amount = rand.nextInt(0, 2)
				if (amount > 0) {
					PARTICLE_CORRUPTION.spawn(Point(pos, amount), rand) // POLISH figure out how to show particles outside animateTick range
				}
			}
		}
		
		components.scheduledTick = object : IBlockScheduledTickComponent {
			override fun onAdded(state: BlockState, world: World, pos: BlockPos, rand: Random): TickSchedule {
				return tickScheduleForLevel(state[LEVEL])
			}
			
			override fun onTick(state: BlockState, world: World, pos: BlockPos, rand: Random): TickSchedule {
				val level = state[LEVEL]
				val remainingFacings = Facing6.toMutableList()
				
				repeat(rand.nextInt(3, 5).coerceAtMost(level)) {
					val facing = rand.removeItem(remainingFacings)
					val adjacentPos = pos.offset(facing)
					
					val spreadDecrease = if (rand.nextInt(3) == 0) 0 else 1
					
					if (spawn(world, adjacentPos, level - spreadDecrease) == PASSTHROUGH) {
						spawn(world, adjacentPos.offset(facing), level - spreadDecrease - 1)
					}
				}
				
				if (rand.nextInt(4) != 0) {
					val decreaseToLevel = level - rand.nextInt(1, 2)
					
					if (decreaseToLevel < MIN_LEVEL) {
						pos.removeBlock(world)
						return TickSchedule.Never
					}
					
					pos.setState(world, state.with(LEVEL, decreaseToLevel), FLAG_NONE) // does not call onBlockAdded for the same Block instance
				}
				
				return tickScheduleForLevel(level)
			}
			
			private fun tickScheduleForLevel(level: Int): TickSchedule {
				return TickSchedule.InTicks((MAX_TICK_RATE - (level / 2)).coerceAtLeast(MIN_TICK_RATE))
			}
		}
		
		components.randomTick = IBlockRandomTickComponent { state, world, pos, _ ->
			if (!world.pendingBlockTicks.isTickScheduled(pos, state.block)) {
				pos.removeBlock(world)
			}
		}
		
		components.collideWithEntity = IBlockCollideWithEntityComponent { state, world, pos, entity ->
			if (!world.isRemote && entity is LivingEntity && !isEntityTolerant(entity)) {
				CombinedDamage(
					DAMAGE_PART_NORMAL to 0.75F,
					DAMAGE_PART_MAGIC to (0.75F + state[LEVEL] / 10F)
				).dealTo(entity, TITLE_MAGIC)
			}
		}
		
		components.isAir = true
	}
	
	private fun isEntityTolerant(entity: LivingEntity): Boolean {
		return CustomCreatureType.isDemon(entity) || CustomCreatureType.isShadow(entity) || entity is IImmuneToCorruptedEnergy
	}
	
	// Utility methods
	
	enum class SpawnResult {
		SUCCESS, PASSTHROUGH, FAIL
	}
	
	fun spawn(world: World, pos: BlockPos, level: Int): SpawnResult {
		if (level < MIN_LEVEL) {
			return FAIL
		}
		else if (level > MAX_LEVEL) {
			return spawn(world, pos, MAX_LEVEL)
		}
		
		val currentState = pos.getState(world)
		val currentBlock = currentState.block
		var updateFlags = FLAG_SYNC_CLIENT
		
		if (currentBlock === ModBlocks.CORRUPTED_ENERGY) {
			if (level - currentState[LEVEL] < 3 || world.rand.nextBoolean()) {
				return FAIL
			}
			
			updateFlags = FLAG_NONE
		}
		else if (currentBlock === ModBlocks.ENERGY_CLUSTER) {
			if (world.rand.nextInt(100) < 5 * level) {
				pos.getTile<TileEntityEnergyCluster>(world)?.deteriorateCapacity(level)
			}
			
			return PASSTHROUGH
		}
		else if (!currentBlock.isAir(currentState, world, pos)) {
			return if (currentState.isNormalCube(world, pos))
				FAIL
			else
				PASSTHROUGH
		}
		
		pos.setState(world, ModBlocks.CORRUPTED_ENERGY.with(LEVEL, level), updateFlags)
		return SUCCESS
	}
}
