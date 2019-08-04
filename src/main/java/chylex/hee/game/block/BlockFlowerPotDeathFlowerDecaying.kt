package chylex.hee.game.block
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.LEVEL
import chylex.hee.game.block.IBlockDeathFlowerDecaying.Companion.MIN_LEVEL
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.init.ModBlocks
import chylex.hee.init.ModItems
import chylex.hee.system.util.get
import chylex.hee.system.util.with
import net.minecraft.block.Block
import net.minecraft.block.state.BlockStateContainer
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random

class BlockFlowerPotDeathFlowerDecaying(builder: BlockBuilder, flower: Block) : BlockFlowerPotCustom(builder, flower), IBlockDeathFlowerDecaying{
	override fun createBlockState() = BlockStateContainer(this, LEVEL)
	
	override fun getMetaFromState(state: IBlockState) = state[LEVEL] - MIN_LEVEL
	override fun getStateFromMeta(meta: Int) = this.with(LEVEL, meta + MIN_LEVEL)
	
	override val thisAsBlock
		get() = this
	
	override val healedFlowerBlock
		get() = ModBlocks.POTTED_DEATH_FLOWER_HEALED
	
	override val witheredFlowerBlock
		get() = ModBlocks.POTTED_DEATH_FLOWER_WITHERED
	
	override fun tickRate(world: World): Int{
		return implTickRate()
	}
	
	override fun onBlockAdded(world: World, pos: BlockPos, state: IBlockState){
		super.onBlockAdded(world, pos, state)
		implOnBlockAdded(world, pos)
	}
	
	override fun updateTick(world: World, pos: BlockPos, state: IBlockState, rand: Random){
		super.updateTick(world, pos, state, rand)
		implUpdateTick(world, pos, state, rand)
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		if (player.getHeldItem(hand).item === ModItems.END_POWDER){
			return false
		}
		
		return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)
	}
}
