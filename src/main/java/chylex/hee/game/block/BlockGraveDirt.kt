package chylex.hee.game.block

import chylex.hee.client.util.MC
import chylex.hee.game.Environment
import chylex.hee.game.Resource
import chylex.hee.game.Resource.location
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockDrop
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStateModels
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.IBlockStateModel
import chylex.hee.game.block.util.Property
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.living.EntityMobSpiderling
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.fx.util.playServer
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.territory.storage.ForgottenTombsEndData
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.isPeaceful
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextBiasedFloat
import chylex.hee.system.random.nextFloat
import chylex.hee.system.random.nextInt
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.center
import chylex.hee.util.math.floorToInt
import chylex.hee.util.math.toYaw
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FireBlock
import net.minecraft.block.SoundType
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType
import net.minecraft.entity.EntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootContext
import net.minecraft.loot.LootParameterSets
import net.minecraft.loot.LootParameters
import net.minecraft.state.StateContainer.Builder
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.DamageSource
import net.minecraft.util.Direction
import net.minecraft.util.Direction.UP
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.util.math.shapes.VoxelShapes
import net.minecraft.world.Difficulty.PEACEFUL
import net.minecraft.world.Explosion
import net.minecraft.world.GameRules.DO_MOB_LOOT
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorld
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

sealed class BlockGraveDirt(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.9375, 1.0)) {
	companion object {
		val FULL = Property.bool("full")
		
		private fun makeSpiderling(world: World, pos: BlockPos, yaw: Float = 0F): EntityMobSpiderling {
			return EntityMobSpiderling(world).apply {
				setLocationAndAngles(pos.x + 0.5, pos.y + 0.01, pos.z + 0.5, yaw, 0F)
			}
		}
	}
	
	// Instance
	
	override val model: IBlockStateModel
		get() = BlockStateModels.ItemOnly(ItemModel.FromParent(Resource.Custom("block/grave_dirt_low")))
	
	final override val drop
		get() = BlockDrop.Manual
	
	val soundType: SoundType
		get() = soundType
	
	init {
		defaultState = stateContainer.baseState.with(FULL, true)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(FULL)
	}
	
	// Bounding box
	
	override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
		return this.with(FULL, context.pos.up().getBlock(context.world) is BlockGraveDirt)
	}
	
	override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
		return if (facing == UP)
			state.with(FULL, pos.up().getBlock(world) is BlockGraveDirt)
		else
			state
	}
	
	override fun getShape(state: BlockState, source: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape {
		return if (state[FULL])
			VoxelShapes.fullCube()
		else
			@Suppress("DEPRECATION")
			super.getShape(state, source, pos, context)
	}
	
	// Mobs
	
	override fun canCreatureSpawn(state: BlockState, world: IBlockReader, pos: BlockPos, type: PlacementType?, entityType: EntityType<*>?): Boolean {
		return true
	}
	
	// Behavior
	
	override fun harvestBlock(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack) {
		super.harvestBlock(world, player, pos, state, tile, stack)
		
		if (world.difficulty != PEACEFUL) {
			val instance = TerritoryInstance.fromPos(pos)
			val data = instance?.getStorageComponent<ForgottenTombsEndData>()
			if (data != null && data.roomAABB?.contains(pos.center) == true) {
				data.startEndSequence(world, instance, pos.center)
			}
		}
	}
	
	// Explosions
	
	override fun canDropFromExplosion(explosion: Explosion): Boolean {
		return false
	}
	
	override fun onBlockExploded(state: BlockState, world: World, pos: BlockPos, explosion: Explosion) {
		super.onBlockExploded(state, world, pos, explosion)
		
		if (world is ServerWorld) {
			LootContext.Builder(world)
				.withRandom(world.rand)
				.withParameter(LootParameters.ORIGIN, pos.center)
				.withParameter(LootParameters.EXPLOSION_RADIUS, explosion.size)
				.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
				.withNullableParameter(LootParameters.BLOCK_ENTITY, null)
				.let(state::getDrops)
				.forEach { spawnAsEntity(world, pos, it) }
		}
	}
	
	// Variations
	
	class Plain(builder: BlockBuilder) : BlockGraveDirt(builder) {
		override val model
			get() = BlockStateModel(
				BlockStatePreset.None,
				BlockModel.Suffixed("_full", BlockModel.WithTextures(
					BlockModel.Cube(this.location),
					mapOf("particle" to this.location)
				)),
				ItemModel.FromParent(Resource.Custom("block/grave_dirt_low"))
			)
	}
	
	class Loot(builder: BlockBuilder) : BlockGraveDirt(builder) {
		override val model
			get() = BlockStateModel(
				BlockStatePreset.None,
				BlockModel.Multi((1..6).map {
					BlockModel.Suffixed("_$it", BlockModel.WithTextures(
						BlockModel.FromParent(Resource.Custom("block/grave_dirt_low")),
						mapOf("top" to Resource.Custom("block/grave_dirt_loot_$it"))
					))
				}),
				ItemModel.FromParent(Resource.Custom("block/grave_dirt_loot_4"))
			)
	}
	
	class Spiderling(builder: BlockBuilder) : BlockGraveDirt(builder) {
		private var clientLastSpiderlingSound = 0L
		
		override fun updatePostPlacement(state: BlockState, facing: Direction, neighborState: BlockState, world: IWorld, pos: BlockPos, neighborPos: BlockPos): BlockState {
			if (!world.isRemote && !world.isPeaceful && facing == UP && neighborState.block is FireBlock && world is World) {
				makeSpiderling(world, neighborPos, yaw = world.rand.nextFloat()).apply {
					health = maxHealth * rng.nextFloat(0.5F, 1F)
					
					setFire(rng.nextInt(6, 7))
					getHurtSound(DamageSource.IN_FIRE).playServer(world, neighborPos, soundCategory, volume = 1.2F, pitch = soundPitch)
					
					world.addEntity(this)
				}
				
				return ModBlocks.GRAVE_DIRT_PLAIN.defaultState
			}
			
			@Suppress("DEPRECATION")
			return super.updatePostPlacement(state, facing, neighborState, world, pos, neighborPos)
		}
		
		override fun harvestBlock(world: World, player: PlayerEntity, pos: BlockPos, state: BlockState, tile: TileEntity?, stack: ItemStack) {
			super.harvestBlock(world, player, pos, state, tile, stack)
			
			if (!world.isPeaceful) {
				makeSpiderling(world, pos, player.posVec.subtract(pos.center).toYaw()).apply {
					world.addEntity(this)
					attackTarget = player
				}
			}
		}
		
		override fun onExplosionDestroy(world: World, pos: BlockPos, explosion: Explosion) {
			super.onExplosionDestroy(world, pos, explosion)
			
			if (world.isRemote) {
				makeSpiderling(world, pos).apply {
					spawnExplosionParticle()
					deathSound.playClient(pos, soundCategory, volume = 0.8F, pitch = soundPitch)
				}
			}
			else if (world is ServerWorld && world.gameRules.getBoolean(DO_MOB_LOOT)) {
				val lootContext = LootContext.Builder(world).withRandom(world.rand).build(LootParameterSets.EMPTY)
				
				for (drop in Environment.getLootTable(EntityMobSpiderling.LOOT_TABLE).generate(lootContext)) {
					spawnAsEntity(world, pos, drop)
				}
			}
		}
		
		@Sided(Side.CLIENT)
		override fun animateTick(state: BlockState, world: World, pos: BlockPos, rand: Random) {
			if (!world.isPeaceful && world.gameTime - clientLastSpiderlingSound > 27L) {
				val distanceSq = MC.player?.getDistanceSq(pos.center) ?: 0.0
				
				if (rand.nextInt(10 + ((distanceSq.floorToInt() * 4) / 5)) == 0) {
					clientLastSpiderlingSound = world.gameTime
					
					val volumeRand = Random(pos.toLong())
					val volume = 0.05F + (0.25F * volumeRand.nextBiasedFloat(1F))
					
					makeSpiderling(world, pos).apply {
						ambientSound.playClient(pos, soundCategory, volume, pitch = rand.nextFloat(0.4F, 0.6F))
					}
				}
			}
		}
	}
}
