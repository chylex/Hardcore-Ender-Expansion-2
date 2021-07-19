package chylex.hee.game.block

import chylex.hee.game.Resource.location
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.FAIL
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.PASSTHROUGH
import chylex.hee.game.block.BlockCorruptedEnergy.SpawnResult.SUCCESS
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
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
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.removeItem
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.INVISIBLE
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockCorruptedEnergy(builder: BlockBuilder) : HeeBlock(builder) {
	companion object {
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
		
		private fun tickRateForLevel(level: Int): Int {
			return (MAX_TICK_RATE - (level / 2)).coerceAtLeast(MIN_TICK_RATE)
		}
		
		private fun isEntityTolerant(entity: LivingEntity): Boolean {
			return CustomCreatureType.isDemon(entity) || CustomCreatureType.isShadow(entity) || entity is IImmuneToCorruptedEnergy
		}
	}
	
	override val model
		get() = BlockStateModel(BlockStatePreset.Simple, BlockModel.NoAmbientOcclusion(BlockModel.Cross(Blocks.BARRIER.asItem().location)))
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(LEVEL)
	}
	
	// Utility methods
	
	enum class SpawnResult {
		SUCCESS, PASSTHROUGH, FAIL
	}
	
	fun spawnCorruptedEnergy(world: World, pos: BlockPos, level: Int): SpawnResult {
		if (level < MIN_LEVEL) {
			return FAIL
		}
		else if (level > MAX_LEVEL) {
			return spawnCorruptedEnergy(world, pos, MAX_LEVEL)
		}
		
		val currentState = pos.getState(world)
		val currentBlock = currentState.block
		var updateFlags = FLAG_SYNC_CLIENT
		
		if (currentBlock === this) {
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
		
		pos.setState(world, this.with(LEVEL, level), updateFlags)
		return SUCCESS
	}
	
	// Tick handling
	
	override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
		world.pendingBlockTicks.scheduleTick(pos, this, tickRateForLevel(state[LEVEL]))
	}
	
	override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		if (!world.pendingBlockTicks.isTickScheduled(pos, this)) {
			pos.removeBlock(world)
		}
	}
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random) {
		val level = state[LEVEL]
		val remainingFacings = Facing6.toMutableList()
		
		repeat(rand.nextInt(3, 5).coerceAtMost(level)) {
			val facing = rand.removeItem(remainingFacings)
			val adjacentPos = pos.offset(facing)
			
			val spreadDecrease = if (rand.nextInt(3) == 0) 0 else 1
			
			if (spawnCorruptedEnergy(world, adjacentPos, level - spreadDecrease) == PASSTHROUGH) {
				spawnCorruptedEnergy(world, adjacentPos.offset(facing), level - spreadDecrease - 1)
			}
		}
		
		if (rand.nextInt(4) != 0) {
			val decreaseToLevel = level - rand.nextInt(1, 2)
			
			if (decreaseToLevel < MIN_LEVEL) {
				pos.removeBlock(world)
				return
			}
			
			pos.setState(world, state.with(LEVEL, decreaseToLevel), FLAG_NONE) // does not call onBlockAdded for the same Block instance
		}
		
		world.pendingBlockTicks.scheduleTick(pos, this, tickRateForLevel(level))
	}
	
	// Interactions
	
	override fun isAir(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return true
	}
	
	override fun propagatesSkylightDown(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean {
		return true
	}
	
	override fun onEntityCollision(state: BlockState, world: World, pos: BlockPos, entity: Entity) {
		if (!world.isRemote && entity is LivingEntity && !isEntityTolerant(entity)) {
			CombinedDamage(
				DAMAGE_PART_NORMAL to 0.75F,
				DAMAGE_PART_MAGIC to (0.75F + state[LEVEL] / 10F)
			).dealTo(entity, TITLE_MAGIC)
		}
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
		val amount = rand.nextInt(0, 2)
		
		if (amount > 0) {
			PARTICLE_CORRUPTION.spawn(Point(pos, amount), rand) // POLISH figure out how to show particles outside animateTick range
		}
	}
	
	@Sided(Side.CLIENT)
	override fun getAmbientOcclusionLightValue(state: BlockState, world: IBlockReader, pos: BlockPos): Float {
		return 1F
	}
	
	override fun getRenderType(state: BlockState) = INVISIBLE
	
	// Debugging
	// override fun getRenderLayer() = CUTOUT
}
