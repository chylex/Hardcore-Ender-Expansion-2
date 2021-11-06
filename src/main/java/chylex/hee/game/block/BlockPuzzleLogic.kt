package chylex.hee.game.block

import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockPuzzleLogic.IPuzzleLogic
import chylex.hee.game.block.BlockPuzzleLogic.State.ACTIVE
import chylex.hee.game.block.BlockPuzzleLogic.State.DISABLED
import chylex.hee.game.block.BlockPuzzleLogic.State.INACTIVE
import chylex.hee.game.block.builder.HeeBlockBuilder
import chylex.hee.game.block.components.IBlockPlacementComponent
import chylex.hee.game.block.interfaces.IBlockInterface
import chylex.hee.game.block.interfaces.getHeeInterface
import chylex.hee.game.block.properties.BlockModel
import chylex.hee.game.block.properties.BlockRenderLayer.CUTOUT
import chylex.hee.game.block.properties.BlockStateModel
import chylex.hee.game.block.properties.BlockStatePreset
import chylex.hee.game.block.properties.BlockTint
import chylex.hee.game.block.properties.Materials
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
import chylex.hee.util.color.RGB
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextFloat
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING
import net.minecraft.block.SoundType
import net.minecraft.block.material.MaterialColor
import net.minecraft.entity.Entity
import net.minecraft.item.BlockItemUseContext
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

class BlockPuzzleLogic(impl: IPuzzleLogic) : HeeBlockBuilder() {
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
				
				for (testPos in pos.floodFill(Facing4) { isPuzzleBlock(it.getBlock(world)) }) {
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
		
		fun isPuzzleBlock(block: Block): Boolean {
			return block.getHeeInterface<IPuzzleLogic>() != null
		}
		
		fun isPuzzleBlockEnabled(state: BlockState): Boolean {
			return isPuzzleBlock(state.block) && state[STATE] != DISABLED
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
		
		fun onToggled(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			val state = pos.getState(world)
			val logic = state.block.getHeeInterface<IPuzzleLogic>()
			
			return if (logic != null && toggleState(world, pos, state))
				logic.getNextChains(world, pos, facing)
			else
				emptyList()
		}
		
		private fun toggleState(world: World, pos: BlockPos, state: BlockState): Boolean {
			val type = state[STATE]
			
			if (type == DISABLED) {
				return false
			}
			
			pos.setState(world, state.with(STATE, type.toggled))
			PacketClientFX(FX_TOGGLE, FxBlockData(pos)).sendToAllAround(world, pos, 24.0)
			return true
		}
	}
	
	init {
		includeFrom(BlockIndestructible)
		
		renderLayer = CUTOUT
		
		material = Materials.SOLID
		color = MaterialColor.ADOBE /* RENAME ORANGE */
		sound = SoundType.STONE
		
		tint = object : BlockTint() {
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
		
		components.states.set(STATE, ACTIVE)
		
		interfaces[IPuzzleLogic::class.java] = impl
	}
	
	fun interface IPuzzleLogic : IBlockInterface {
		fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>
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
	
	object Plain : HeeBlockBuilder() {
		internal val LOGIC = IPuzzleLogic { _, pos, facing -> listOf(makePair(pos, facing)) }
		
		init {
			includeFrom(BlockPuzzleLogic(LOGIC))
			
			localization = LocalizationStrategy.Parenthesized(wordCount = 1, fromStart = false)
			model = createPlainModel()
		}
	}
	
	class Burst(radius: Int) : HeeBlockBuilder() {
		private val logic = IPuzzleLogic { world, pos, facing ->
			pos.allInCenteredBox(radius, 0, radius).toList().flatMap { toggleAndChain(world, it, facing) }.distinct()
		}
		
		init {
			includeFrom(BlockPuzzleLogic(logic))
			
			val diameter = 1 + (radius * 2)
			localization = LocalizationStrategy.Parenthesized(LocalizationStrategy.ReplaceWords("$diameter", "${diameter}x${diameter}"), wordCount = 2, fromStart = false)
			model = createOverlayModel("burst_$diameter")
		}
		
		private fun toggleAndChain(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
			val state = pos.getState(world)
			val logic = state.block.getHeeInterface<IPuzzleLogic>()
			
			return if (logic == null || !toggleState(world, pos, state) || logic === Plain.LOGIC || logic === this.logic)
				emptyList()
			else
				logic.getNextChains(world, pos, facing)
		}
	}
	
	private class RedirectSome(blockDirections: Array<String>, itemDirection: String, directions: Array<Direction>) : HeeBlockBuilder() {
		private val logic = BlockPuzzleLogic { world, pos, facing ->
			val rotation = pos.getState(world)[HORIZONTAL_FACING].horizontalIndex + NORTH.horizontalIndex
			val exclude = facing.opposite
			
			directions.map { Direction.byHorizontalIndex(it.horizontalIndex + rotation) }.filter { it != exclude }.map { makePair(pos, it) }
		}
		
		init {
			includeFrom(logic)
			
			localization = LocalizationStrategy.Parenthesized(wordCount = 2, fromStart = false)
			model = createOverlayModel("redirect_" + directions.size + itemDirection, blockDirections.map { "redirect_" + directions.size + it })
			
			components.states.set(HORIZONTAL_FACING, NORTH)
			components.states.facingProperty = HORIZONTAL_FACING
			
			components.placement = object : IBlockPlacementComponent {
				override fun getPlacedState(defaultState: BlockState, world: World, pos: BlockPos, context: BlockItemUseContext): BlockState {
					return defaultState.withFacing(context.placementHorizontalFacing)
				}
			}
		}
	}
	
	object RedirectOne : HeeBlockBuilder() {
		init {
			includeFrom(RedirectSome(arrayOf("n", "s", "e", "w"), "n", arrayOf(NORTH)))
		}
	}
	
	object RedirectTwo : HeeBlockBuilder() {
		init {
			includeFrom(RedirectSome(arrayOf("ns", "ew"), "ns", arrayOf(NORTH, SOUTH)))
		}
	}
	
	object RedirectAll : HeeBlockBuilder() {
		init {
			includeFrom(BlockPuzzleLogic { _, pos, facing ->
				Facing4.filter { it != facing.opposite }.map { makePair(pos, it) }
			})
			
			localization = LocalizationStrategy.Parenthesized(wordCount = 2, fromStart = false)
			model = createOverlayModel("redirect_4")
		}
	}
	
	object Teleport : HeeBlockBuilder() {
		private val logic = object : IPuzzleLogic {
			override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>> {
				return findAllBlocks(world, pos).filter { it != pos && it.getBlock(world).getHeeInterface<IPuzzleLogic>() === this }.map { makePair(it, facing) }
			}
		}
		
		init {
			includeFrom(BlockPuzzleLogic(logic))
			
			localization = LocalizationStrategy.Parenthesized(wordCount = 1, fromStart = false)
			model = createOverlayModel("teleport")
		}
	}
}
