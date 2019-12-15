package chylex.hee.game.block
import chylex.hee.game.block.util.CustomPlantType
import chylex.hee.game.block.util.Property
import chylex.hee.game.world.feature.basic.trees.WhitebarkTreeGenerator
import chylex.hee.game.world.structure.world.WorldToStructureWorldAdapter
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.FLAG_SKIP_RENDER
import chylex.hee.system.util.get
import chylex.hee.system.util.setState
import chylex.hee.system.util.with
import net.minecraft.block.BlockBush
import net.minecraft.block.IGrowable
import net.minecraft.block.SoundType
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.common.EnumPlantType
import java.util.Random

class BlockWhitebarkSapling(private val generator: WhitebarkTreeGenerator) : BlockBush(), IGrowable{
	companion object{
		val STAGE = Property.int("stage", 0..2)
		val AABB = AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 0.8, 0.9)
	}
	
	// Instance
	
	init{
		soundType = SoundType.PLANT
		defaultState = blockState.baseState.with(STAGE, 0)
	}
	
	override fun createBlockState() = BlockStateContainer(this, STAGE)
	
	override fun getMetaFromState(state: IBlockState) = state[STAGE]
	override fun getStateFromMeta(meta: Int): IBlockState = defaultState.with(STAGE, meta)
	
	// Bounding box
	
	override fun getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB{
		return AABB
	}
	
	// Placement behavior
	
	override fun getPlantType(world: IBlockAccess, pos: BlockPos): EnumPlantType{
		return CustomPlantType.END
	}
	
	override fun canSustainBush(state: IBlockState): Boolean{
		return state.block.let { it === Blocks.END_STONE || it === ModBlocks.ENDERSOL || it === ModBlocks.HUMUS }
	}
	
	// Growth rules
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		super.updateTick(world, pos, state, rand)
		
		if (rand.nextInt(7) == 0){
			grow(world, rand, pos, state)
		}
	}
	
	override fun canGrow(world: World, pos: BlockPos, state: IBlockState, isClient: Boolean): Boolean{
		return true
	}
	
	override fun canUseBonemeal(world: World, rand: Random, pos: BlockPos, state: IBlockState): Boolean{
		return rand.nextFloat() < 0.45F
	}
	
	override fun grow(world: World, rand: Random, pos: BlockPos, state: IBlockState){
		val stage = state[STAGE]
		
		if (stage < STAGE.allowedValues.max()!!){
			pos.setState(world, state.with(STAGE, stage + 1), FLAG_SKIP_RENDER)
		}
		else{
			generator.generate(WorldToStructureWorldAdapter(world, rand, pos), BlockPos.ORIGIN)
		}
	}
}
