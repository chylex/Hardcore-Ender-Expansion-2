package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.util.Property
import chylex.hee.game.world.feature.basic.trees.WhitebarkTreeGenerator
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.asVoxelShape
import chylex.hee.system.util.setState
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.IGrowable
import net.minecraft.state.StateContainer.Builder
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.shapes.ISelectionContext
import net.minecraft.util.math.shapes.VoxelShape
import net.minecraft.world.IBlockReader
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import java.util.Random

class BlockWhitebarkSapling(builder: BlockBuilder, private val generator: WhitebarkTreeGenerator<*>) : BlockEndPlant(builder), IGrowable{
	companion object{
		val STAGE = Property.int("stage", 0..2)
		val AABB = AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.8, 0.9).asVoxelShape
	}
	
	// Instance
	
	init{
		defaultState = stateContainer.baseState.with(STAGE, 0)
	}
	
	override fun fillStateContainer(container: Builder<Block, BlockState>){
		container.add(STAGE)
	}
	
	// Bounding box
	
	override fun getShape(state: BlockState, world: IBlockReader, pos: BlockPos, context: ISelectionContext): VoxelShape{
		return AABB
	}
	
	// Placement behavior
	
	override fun isValidGround(state: BlockState, world: IBlockReader, pos: BlockPos): Boolean{
		return state.block.let { it === Blocks.END_STONE || it === ModBlocks.ENDERSOL || it === ModBlocks.HUMUS }
	}
	
	// Growth rules
	
	override fun tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random){
		super.tick(state, world, pos, rand)
		
		if (rand.nextInt(7) == 0){
			grow(world, rand, pos, state)
		}
	}
	
	override fun canGrow(world: IBlockReader, pos: BlockPos, state: BlockState, isClient: Boolean): Boolean{
		return true
	}
	
	override fun canUseBonemeal(world: World, rand: Random, pos: BlockPos, state: BlockState): Boolean{
		return rand.nextFloat() < 0.45F
	}
	
	override fun grow(world: ServerWorld, rand: Random, pos: BlockPos, state: BlockState){
		val stage = state[STAGE]
		
		if (stage < STAGE.allowedValues.maxOrNull()!!){
			pos.setState(world, state.with(STAGE, stage + 1), FLAG_SKIP_RENDER)
		}
		else{
			generator.generate(WorldToStructureWorldAdapter(world, rand, pos), BlockPos.ZERO)
		}
	}
}
