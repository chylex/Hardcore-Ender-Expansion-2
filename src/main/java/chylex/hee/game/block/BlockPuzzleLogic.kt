package chylex.hee.game.block
import chylex.hee.HEE
import chylex.hee.client.color.NO_TINT
import chylex.hee.game.block.BlockPuzzleLogic.State.ACTIVE
import chylex.hee.game.block.BlockPuzzleLogic.State.DISABLED
import chylex.hee.game.block.BlockPuzzleLogic.State.INACTIVE
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.Property
import chylex.hee.game.entity.posVec
import chylex.hee.game.particle.ParticleSmokeCustom
import chylex.hee.game.particle.spawner.ParticleSpawnerCustom
import chylex.hee.game.particle.spawner.properties.IOffset.Constant
import chylex.hee.game.particle.spawner.properties.IOffset.InBox
import chylex.hee.game.particle.spawner.properties.IOffset.InSphere
import chylex.hee.game.particle.spawner.properties.IShape.Point
import chylex.hee.game.world.Pos
import chylex.hee.game.world.allInBox
import chylex.hee.game.world.allInCenteredBox
import chylex.hee.game.world.distanceSqTo
import chylex.hee.game.world.floodFill
import chylex.hee.game.world.getBlock
import chylex.hee.game.world.getState
import chylex.hee.game.world.math.BoundingBox
import chylex.hee.game.world.playClient
import chylex.hee.game.world.setState
import chylex.hee.game.world.totalTime
import chylex.hee.init.ModSounds
import chylex.hee.network.client.PacketClientFX
import chylex.hee.network.fx.FxBlockData
import chylex.hee.network.fx.FxBlockHandler
import chylex.hee.network.fx.FxEntityHandler
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Facing4
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.random.nextFloat
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.Entity
import net.minecraft.item.BlockItemUseContext
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.Direction
import net.minecraft.util.IStringSerializable
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.world.ILightReader
import net.minecraft.world.World
import java.util.Random

sealed class BlockPuzzleLogic(builder: BlockBuilder) : BlockSimple(builder){
	companion object{
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
		
		val FX_TOGGLE = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				PARTICLE_TOGGLE.spawn(Point(pos, 5), rand)
				
				val currentTime = world.totalTime
				
				if (currentTime - lastClientClickSoundTime > UPDATE_RATE / 2){
					lastClientClickSoundTime = currentTime
					ModSounds.BLOCK_PUZZLE_LOGIC_CLICK.playClient(pos, SoundCategory.BLOCKS, pitch = world.rand.nextFloat(0.9F, 1F))
				}
			}
		}
		
		val FX_SOLVE_TOGGLE = object : FxBlockHandler(){
			override fun handle(pos: BlockPos, world: World, rand: Random){
				val player = HEE.proxy.getClientSidePlayer() ?: return
				
				var closest = pos
				var closestDistSq = pos.distanceSqTo(player)
				
				for(testPos in pos.floodFill(Facing4){ it.getBlock(world) is BlockPuzzleLogic }){
					PARTICLE_TOGGLE.spawn(Point(testPos, 3), rand)
					
					val distSq = testPos.distanceSqTo(player)
					
					if (distSq < closestDistSq){
						closestDistSq = distSq
						closest = testPos
					}
				}
				
				ModSounds.BLOCK_PUZZLE_LOGIC_CLICK.playClient(closest, SoundCategory.BLOCKS, volume = 3F, pitch = 1.1F)
			}
		}
		
		val FX_SOLVE_SPAWN = object : FxEntityHandler(){
			override fun handle(entity: Entity, rand: Random){
				PARTICLE_SPAWN_MEDALLION.spawn(Point(entity, heightMp = 0.3F, amount = 75), rand)
				ModSounds.ITEM_PUZZLE_MEDALLION_SPAWN.playClient(entity.posVec, SoundCategory.BLOCKS, volume = 3F, pitch = 5F)
			}
		}
		
		private fun isPuzzleBlockEnabled(state: BlockState): Boolean{
			return state.block is BlockPuzzleLogic && state[STATE] != DISABLED
		}
		
		fun findAllBlocks(world: World, pos: BlockPos): List<BlockPos>{
			return pos.floodFill(Facing4){ isPuzzleBlockEnabled(it.getState(world)) }
		}
		
		fun findAllRectangles(world: World, allBlocks: List<BlockPos>): List<BoundingBox>{
			val rects = mutableListOf<BoundingBox>()
			val remainingBlocks = allBlocks.toMutableSet()
			
			while(remainingBlocks.isNotEmpty()){
				val startPos = remainingBlocks.first()
				val y = startPos.y
				
				var coordPos = startPos
				var coordNeg = startPos
				
				while(true){
					val prevCoordPos = coordPos
					val prevCoordNeg = coordNeg
					
					if (Pos(coordPos.x + 1, y, coordNeg.z).allInBox(Pos(coordPos.x + 1, y, coordPos.z)).all { isPuzzleBlockEnabled(it.getState(world)) }){
						coordPos = coordPos.add(1, 0, 0)
					}
					
					if (Pos(coordPos.x, y, coordPos.z + 1).allInBox(Pos(coordNeg.x, y, coordPos.z + 1)).all { isPuzzleBlockEnabled(it.getState(world)) }){
						coordPos = coordPos.add(0, 0, 1)
					}
					
					if (Pos(coordNeg.x - 1, y, coordNeg.z).allInBox(Pos(coordNeg.x - 1, y, coordPos.z)).all { isPuzzleBlockEnabled(it.getState(world)) }){
						coordNeg = coordNeg.add(-1, 0, 0)
					}
					
					if (Pos(coordPos.x, y, coordNeg.z - 1).allInBox(Pos(coordNeg.x, y, coordNeg.z - 1)).all { isPuzzleBlockEnabled(it.getState(world)) }){
						coordNeg = coordNeg.add(0, 0, -1)
					}
					
					if (coordPos == prevCoordPos && coordNeg == prevCoordNeg){
						break
					}
				}
				
				val rect = BoundingBox(coordPos, coordNeg)
				
				remainingBlocks.removeIf(rect::isInside)
				rects.add(rect)
			}
			
			return rects
		}
		
		private fun makePair(pos: BlockPos, facing: Direction): Pair<BlockPos, Direction>{
			return pos.offset(facing) to facing
		}
	}
	
	enum class State(private val serializableName: String) : IStringSerializable{
		ACTIVE("active"),
		INACTIVE("inactive"),
		DISABLED("disabled");
		
		val toggled
			get() = when(this){
				ACTIVE -> INACTIVE
				INACTIVE -> ACTIVE
				DISABLED -> DISABLED
			}
		
		override fun getName(): String{
			return serializableName
		}
	}
	
	init{
		defaultState = stateContainer.baseState.with(STATE, ACTIVE)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(STATE)
	}
	
	// Logic
	
	fun onToggled(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
		val state = pos.getState(world)
		
		return if (toggleState(world, pos, state))
			getNextChains(world, pos, facing)
		else
			emptyList()
	}
	
	protected fun toggleState(world: World, pos: BlockPos, state: BlockState): Boolean{
		val type = state[STATE]
		
		if (type == DISABLED){
			return false
		}
		
		pos.setState(world, state.with(STATE, type.toggled))
		PacketClientFX(FX_TOGGLE, FxBlockData(pos)).sendToAllAround(world, pos, 24.0)
		return true
	}
	
	protected abstract fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>
	
	// Variations
	
	class Plain(builder: BlockBuilder) : BlockPuzzleLogic(builder){
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
			return listOf(makePair(pos, facing))
		}
	}
	
	class Burst(builder: BlockBuilder, private val radius: Int) : BlockPuzzleLogic(builder){
		private fun toggleAndChain(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
			val state = pos.getState(world)
			val block = state.block
			
			return if (block !is BlockPuzzleLogic || !toggleState(world, pos, state) || block is Plain || block is Burst)
				emptyList()
			else
				block.getNextChains(world, pos, facing)
		}
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
			return pos.allInCenteredBox(radius, 0, radius).toList().flatMap { toggleAndChain(world, it, facing)  }.distinct()
		}
	}
	
	class Redirect(builder: BlockBuilder, private val directions: Array<Direction>) : BlockPuzzleLogic(builder){
		init{
			defaultState = stateContainer.baseState.with(STATE, ACTIVE).with(HORIZONTAL_FACING, NORTH)
		}
		
		override fun fillStateContainer(container: Builder<Block, BlockState>){
			container.add(STATE, HORIZONTAL_FACING)
		}
		
		override fun getStateForPlacement(context: BlockItemUseContext): BlockState{
			return this.withFacing(context.placementHorizontalFacing)
		}
		
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
			val rotation = pos.getState(world)[HORIZONTAL_FACING].horizontalIndex + NORTH.horizontalIndex
			val exclude = facing.opposite
			
			return directions.map { Direction.byHorizontalIndex(it.horizontalIndex + rotation) }.filter { it != exclude }.map { makePair(pos, it) }
		}
	}
	
	class RedirectAll(builder: BlockBuilder) : BlockPuzzleLogic(builder){
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
			return Facing4.filter { it != facing.opposite }.map { makePair(pos, it) }
		}
	}
	
	class Teleport(builder: BlockBuilder) : BlockPuzzleLogic(builder){
		override fun getNextChains(world: World, pos: BlockPos, facing: Direction): List<Pair<BlockPos, Direction>>{
			return findAllBlocks(world, pos).filter { it != pos && it.getBlock(world) is Teleport }.map { makePair(it, facing) }
		}
	}
	
	// Client side
	
	@Sided(Side.CLIENT)
	object Color : IBlockColor{
		override fun getColor(state: BlockState, world: ILightReader?, pos: BlockPos?, tintIndex: Int): Int{
			if (tintIndex != 1){
				return NO_TINT
			}
			
			if (world == null && pos == null){
				return RGB(104, 58, 16).i // make the color slightly more visible in inventory
			}
			
			return when(state[STATE]){
				ACTIVE   -> RGB(117,  66,  19).i
				INACTIVE -> RGB(212, 157, 102).i
				DISABLED -> RGB( 58,  40,  23).i
				else     -> NO_TINT
			}
		}
	}
}
