package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockPuzzleLogic.State.ACTIVE
import chylex.hee.game.block.BlockPuzzleLogic.State.DISABLED
import chylex.hee.game.block.BlockPuzzleLogic.State.INACTIVE
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.BlockTint
import chylex.hee.game.block.properties.IBlockStateModel
import chylex.hee.game.block.util.Property
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.fx.FxBlockData
import chylex.hee.game.fx.FxBlockHandler
import chylex.hee.game.fx.FxEntityHandler
import chylex.hee.game.fx.util.playClient
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IOffset.InSphere
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.allInBox
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.distanceSqTo
import chylex.hee.game.world.util.floodFill
import chylex.hee.game.world.util.getBlock
import chylex.hee.game.world.util.getState
import chylex.hee.game.world.util.setState
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.system.random.nextFloat
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING
import net.minecraft.entity.Entity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.UP
import net.minecraft.util.IStringSerializable
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockDisplayReader
import net.minecraft.world.World
import java.util.Random

sealed class BlockPuzzleLogic(builder: BlockBuilder) : HeeBlock(builder) {
	companion object {
		val STATE = Property.enum<State>("state")
		
		const val UPDATE_RATE = 7
		const val MAX_SIZE = 20
		
		private var lastClientClickSoundTime = 0L
		
		private val PARTICLE_TOGGLE = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(lifespan = 5..13, scale = 0.55F),
			pos = Constant(0.6F, UP) + InBox(0.5F, 0.05F, 0.5F)
		)
		
		private val PARTICLE_SPAWN_MEDALLION = ParticleSpawnerCustom(
			type = ParticleSmokeCustom,
			data = ParticleSmokeCustom.Data(lifespan = 17..31, scale = 0.85F),
			pos = InSphere(1.25F)
		)
		
		val FX_TOGGLE = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
				PARTICLE_TOGGLE.spawn(Point(pos, 5), rand)
				
				val currentTime = world.gameTime
				
				if (currentTime - lastClientClickSoundTime > UPDATE_RATE / 2) {
					lastClientClickSoundTime = currentTime
					ModSounds.BLOCK_PUZZLE_LOGIC_CLICK.playClient(pos, SoundCategory.BLOCKS, pitch = world.rand.nextFloat(0.9F, 1F))
				}
			}
		}
		
		val FX_SOLVE_TOGGLE = object : FxBlockHandler() {
			override fun handle(pos: BlockPos, world: World, rand: Random) {
				val player = MC.player ?: return
				
				var closest = pos
				var closestDistSq = pos.distanceSqTo(player)
				
				for (testPos in pos.floodFill(Facing4) { it.getBlock(world) is BlockPuzzleLogic }) {
					PARTICLE_TOGGLE.spawn(Point(testPos, 3), rand)
					
					val distSq = testPos.distanceSqTo(player)
					
					if (distSq < closestDistSq) {
						closestDistSq = distSq
						closest = testPos
					}
				}
				
				ModSounds.BLOCK_PUZZLE_LOGIC_CLICK.playClient(closest, SoundCategory.BLOCKS, volume = 3F, pitch = 1.1F)
			}
		}
		
		val FX_SOLVE_SPAWN = object : FxEntityHandler() {
			override fun handle(entity: Entity, rand: Random) {
				PARTICLE_SPAWN_MEDALLION.spawn(Point(entity, heightMp = 0.3F, amount = 75), rand)
				ModSounds.ITEM_PUZZLE_MEDALLION_SPAWN.playClient(entity.posVec, SoundCategory.BLOCKS, volume = 3F, pitch = 5F)
			}
		}
		
		private fun isPuzzleBlockEnabled(state: BlockState): Boolean {
			return state.block is BlockPuzzleLogic && state[STATE] != DISABLED
		}
		
		fun findAllBlocks(world: World, pos: BlockPos): List<BlockPos> {
			return pos.floodFill(Facing4) { isPuzzleBlockEnabled(it.getState(world)) }
		}
		
		fun findAllRectangles(world: World, allBlocks: List<BlockPos>): List<BoundingBox> {
			val rects = mutableListOf<BoundingBox>()
			val remainingBlocks = allBlocks.toMutableSet()
			
			while (remainingBlocks.isNotEmpty()) {
				val startPos = remainingBlocks.first()
				val y = startPos.y
				
				var coordPos = startPos
				var coordNeg = startPos
				
				while (true) {
					val prevCoordPos = coordPos
					val prevCoordNeg = coordNeg
					
					if (Pos(coordPos.x + 1, y, coordNeg.z).allInBox(Pos(coordPos.x + 1, y, coordPos.z)).all { isPuzzleBlockEnabled(it.getState(world)) }) {
						coordPos = coordPos.add(1, 0, 0)
					}
					
					if (Pos(coordPos.x, y, coordPos.z + 1).allInBox(Pos(coordNeg.x, y, coordPos.z + 1)).all { isPuzzleBlockEnabled(it.getState(world)) }) {
						coordPos = coordPos.add(0, 0, 1)
					}
					
					if (Pos(coordNeg.x - 1, y, coordNeg.z).allInBox(Pos(coordNeg.x - 1, y, coordPos.z)).all { isPuzzleBlockEnabled(it.getState(world)) }) {
						coordNeg = coordNeg.add(-1, 0, 0)
					}
					
					if (Pos(coordPos.x, y, coordNeg.z - 1).allInBox(Pos(coordNeg.x, y, coordNeg.z - 1)).all { isPuzzleBlockEnabled(it.getState(world)) }) {
						coordNeg = coordNeg.add(0, 0, -1)
					}
					
					if (coordPos == prevCoordPos && coordNeg == prevCoordNeg) {
						break
					}
				}
				
				val rect = BoundingBox(coordPos, coordNeg)
				
				remainingBlocks.removeIf(rect::isInside)
				rects.add(rect)
			}
			
			return rects
		}
		
		private fun makePair(pos: BlockPos, facing: Direction): Pair<BlockPos, Direction> {
			return pos.offset(facing) to facing
		}
		
		private fun createPlainModel() = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi(arrayOf("active", "disabled", "inactive").map {
				BlockModel.WithTextures(
					BlockModel.Parent("puzzle_base_$it", Resource.Vanilla("block/cube_all")),
					mapOf("all" to Resource.Custom("block/puzzle_base_$it"))
				)
			}),
			ItemModel.FromParent(Resource.Custom("block/puzzle_base_active"))
		)
		
		private fun createOverlayModel(itemSuffix: String, blockSuffixes: List<String> = listOf(itemSuffix)) = BlockStateModel(
			BlockStatePreset.None,
			BlockModel.Multi(blockSuffixes.map {
				BlockModel.WithTextures(
					BlockModel.Parent("puzzle_overlay_$it", Resource.Custom("block/puzzle_overlay")),
					mapOf("overlay" to Resource.Custom("block/puzzle_overlay_$it"))
				)
			}),
			ItemModel.WithTextures(
				ItemModel.FromParent(Resource.Custom("block/puzzle_block_inventory")),
				mapOf("top" to Resource.Custom("block/puzzle_overlay_$itemSuffix"))
			)
		)
	}
	
	enum class State(private val serializableName: String) : IStringSerializable {
		ACTIVE("active"),
		INACTIVE("inactive"),
		DISABLED("disabled");
		
		val toggled
			get() = when (this) {
				ACTIVE   -> INACTIVE
				INACTIVE -> ACTIVE
				DISABLED -> DISABLED
			}
		
		override fun getString(): String {
			return serializableName
		}
	}
	
	abstract override val model: IBlockStateModel
	
	final override val renderLayer
		get() = CUTOUT
	
	init {
		defaultState = stateContainer.baseState.with(STATE, ACTIVE)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>) {
		container.add(STATE)
	}
	
	// Logic
	
	fun onToggled(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
		val state = pos.getState(world)
		
		return if (toggleState(world, pos, state))
			getNextChains(world, pos, facing)
		else
			emptyList()
	}
	
	protected fun toggleState(world: World, pos: BlockPos, state: BlockState): Boolean {
		val type = state[STATE]
		
		if (type == DISABLED) {
			return false
		}
		
		pos.setState(world, state.with(STATE, type.toggled))
		PacketClientFX(FX_TOGGLE, FxBlockData(pos)).sendToAllAround(world, pos, 24.0)
		return true
	}
	
	protected abstract fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>
	
	// Variations
	
	class Plain(builder: BlockBuilder) : BlockPuzzleLogic(builder) {
		override val localization
			get() = LocalizationStrategy.Parenthesized(wordCount = 1, fromStart = false)
		
		override val model
			get() = createPlainModel()
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			return listOf(makePair(pos, facing))
		}
	}
	
	class Burst(builder: BlockBuilder, private val radius: Int) : BlockPuzzleLogic(builder) {
		override val localization
			get() = LocalizationStrategy.Parenthesized(LocalizationStrategy.ReplaceWords("$diameter", "${diameter}x${diameter}"), wordCount = 2, fromStart = false)
		
		override val model
			get() = createOverlayModel("burst_$diameter")
		
		private val diameter
			get() = 1 + (radius * 2)
		
		private fun toggleAndChain(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			val state = pos.getState(world)
			val block = state.block
			
			return if (block !is BlockPuzzleLogic || !toggleState(world, pos, state) || block is Plain || block is Burst)
				emptyList()
			else
				block.getNextChains(world, pos, facing)
		}
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			return pos.allInCenteredBox(radius, 0, radius).toList().flatMap { toggleAndChain(world, it, facing) }.distinct()
		}
	}
	
	sealed class RedirectSome private constructor(builder: BlockBuilder, private val blockDirections: Array<String>, private val itemDirection: String, private val directions: Array<Direction>) : BlockPuzzleLogic(builder) {
		class R1(builder: BlockBuilder) : RedirectSome(builder, arrayOf("n", "s", "e", "w"), "n", arrayOf(NORTH))
		class R2(builder: BlockBuilder) : RedirectSome(builder, arrayOf("ns", "ew"), "ns", arrayOf(NORTH, SOUTH))
		
		override val localization
			get() = LocalizationStrategy.Parenthesized(wordCount = 2, fromStart = false)
		
		override val model
			get() = createOverlayModel("redirect_" + directions.size + itemDirection, blockDirections.map { "redirect_" + directions.size + it })
		
		init {
			defaultState = stateContainer.baseState.with(STATE, ACTIVE).withFacing(NORTH)
		}
		
		override fun fillStateContainer(container: Builder<Block, BlockState>) {
			container.add(STATE, HORIZONTAL_FACING)
		}
		
		override fun getStateForPlacement(context: BlockItemUseContext): BlockState {
			return this.withFacing(context.placementHorizontalFacing)
		}
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			val rotation = pos.getState(world)[HORIZONTAL_FACING].horizontalIndex + NORTH.horizontalIndex
			val exclude = facing.opposite
			
			return directions.map { Direction.byHorizontalIndex(it.horizontalIndex + rotation) }.filter { it != exclude }.map { makePair(pos, it) }
		}
	}
	
	class RedirectAll(builder: BlockBuilder) : BlockPuzzleLogic(builder) {
		override val localization
			get() = LocalizationStrategy.Parenthesized(wordCount = 2, fromStart = false)
		
		override val model
			get() = createOverlayModel("redirect_4")
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			return Facing4.filter { it != facing.opposite }.map { makePair(pos, it) }
		}
	}
	
	class Teleport(builder: BlockBuilder) : BlockPuzzleLogic(builder) {
		override val localization
			get() = LocalizationStrategy.Parenthesized(wordCount = 1, fromStart = false)
		
		override val model
			get() = createOverlayModel("teleport")
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			return findAllBlocks(world, pos).filter { it != pos && it.getBlock(world) is Teleport }.map { makePair(it, facing) }
		}
	}
	
	// Client side
	
	override val tint: BlockTint
		get() = Tint
	
	private object Tint : BlockTint() {
		@Sided(Side.CLIENT)
		override fun tint(state: BlockState, world: IBlockDisplayReader?, pos: BlockPos?, tintIndex: Int): Int {
			if (tintIndex != 1) {
				return NO_TINT
			}
			
			if (world == null && pos == null) {
				return RGB(104, 58, 16).i // make the color slightly more visible in inventory
			}
			
			return when (state[STATE]) {
				ACTIVE   -> RGB(117,  66,  19).i
				INACTIVE -> RGB(212, 157, 102).i
				DISABLED -> RGB( 58,  40,  23).i
				else     -> NO_TINT
			}
		}
	}
}
