package chylex.hee.game.block
import chylex.hee.client.render.util.NO_TINT
import chylex.hee.game.block.BlockPuzzleLogic.State.ACTIVE
import chylex.hee.game.block.BlockPuzzleLogic.State.DISABLED
import chylex.hee.game.block.BlockPuzzleLogic.State.INACTIVE
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Facing4
import chylex.hee.system.util.get
import chylex.hee.system.util.getState
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import chylex.hee.system.util.withFacing
import net.minecraft.block.BlockHorizontal.FACING
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.color.IBlockColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.IStringSerializable
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Stack

sealed class BlockPuzzleLogic(builder: BlockBuilder) : BlockSimple(builder){
	companion object{
		val STATE = Property.enum<State>("state")
		
		const val UPDATE_RATE = 7
		const val MAX_SIZE = 20
		
		fun findAllBlocks(world: World, pos: BlockPos): List<Pair<BlockPos, IBlockState>>{
			val found = mutableListOf<Pair<BlockPos, IBlockState>>()
			
			val stack = Stack<BlockPos>().apply { push(pos) }
			val visited = mutableSetOf(pos)
			
			while(stack.isNotEmpty()){
				val current = stack.pop()
				val state = current.getState(world)
				
				if (state.block is BlockPuzzleLogic && state[STATE] != DISABLED){
					found.add(current to state)
					
					for(facing in Facing4){
						val offset = current.offset(facing)
						
						if (!visited.contains(offset)){
							stack.push(offset)
							visited.add(offset)
						}
					}
				}
			}
			
			return found
		}
		
		private fun makePair(pos: BlockPos, facing: EnumFacing): Pair<BlockPos, EnumFacing>{
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
		defaultState = blockState.baseState.with(STATE, ACTIVE)
	}
	override fun createBlockState() = BlockStateContainer(this, STATE)
	
	override fun getMetaFromState(state: IBlockState) = state[STATE].ordinal
	override fun getStateFromMeta(meta: Int) = this.with(STATE, State.values()[meta])
	
	// Logic
	
	fun onToggled(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
		val state = pos.getState(world)
		
		return if (toggleState(world, pos, state))
			getNextChains(world, pos, facing)
		else
			emptyList()
	}
	
	protected fun toggleState(world: World, pos: BlockPos, state: IBlockState): Boolean{
		val type = state[STATE]
		
		if (type == DISABLED){
			return false
		}
		
		pos.setState(world, state.with(STATE, type.toggled)) // TODO fx
		return true
	}
	
	protected abstract fun getNextChains(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>
	
	// Variations
	
	class Plain(builder: BlockBuilder) : BlockPuzzleLogic(builder){
		override fun getNextChains(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
			return listOf(makePair(pos, facing))
		}
	}
	
	class Burst(builder: BlockBuilder, private val radius: Int) : BlockPuzzleLogic(builder){
		private fun toggleAndChain(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
			val state = pos.getState(world)
			val block = state.block
			
			return if (block !is BlockPuzzleLogic || !toggleState(world, pos, state) || block is Plain || block is Burst)
				emptyList()
			else
				block.getNextChains(world, pos, facing)
		}
		
		override fun getNextChains(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
			return pos.allInCenteredBox(radius, 0, radius).flatMap { toggleAndChain(world, it, facing)  }.distinct()
		}
	}
	
	class Redirect(builder: BlockBuilder, private val directions: Array<EnumFacing>) : BlockPuzzleLogic(builder){
		init{
			defaultState = blockState.baseState.with(STATE, ACTIVE).with(FACING, NORTH)
		}
		
		override fun createBlockState() = BlockStateContainer(this, STATE, FACING)
		
		override fun getMetaFromState(state: IBlockState) = (4 * state[STATE].ordinal) + state[FACING].horizontalIndex
		override fun getStateFromMeta(meta: Int) = this.with(STATE, State.values()[meta / 4]).with(FACING, EnumFacing.byHorizontalIndex(meta % 4))
		
		override fun getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState{
			return this.withFacing(placer.horizontalFacing)
		}
		
		override fun getNextChains(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
			val rotation = pos.getState(world)[FACING].horizontalIndex + NORTH.horizontalIndex
			val exclude = facing.opposite
			
			return directions.map { EnumFacing.byHorizontalIndex(it.horizontalIndex + rotation) }.filter { it != exclude }.map { makePair(pos, it) }
		}
	}
	
	class RedirectAll(builder: BlockBuilder) : BlockPuzzleLogic(builder){
		override fun getNextChains(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
			return Facing4.filter { it != facing.opposite }.map { makePair(pos, it) }
		}
	}
	
	class Teleport(builder: BlockBuilder) : BlockPuzzleLogic(builder){
		override fun getNextChains(world: World, pos: BlockPos, facing: EnumFacing): List<Pair<BlockPos, EnumFacing>>{
			return findAllBlocks(world, pos).filter { it.first != pos && it.second.block is Teleport }.map { makePair(it.first, facing) }
		}
	}
	
	// Client side
	
	override fun getRenderLayer() = CUTOUT
	
	@Sided(Side.CLIENT)
	object Color : IBlockColor{
		override fun colorMultiplier(state: IBlockState, world: IBlockAccess?, pos: BlockPos?, tintIndex: Int): Int{
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
