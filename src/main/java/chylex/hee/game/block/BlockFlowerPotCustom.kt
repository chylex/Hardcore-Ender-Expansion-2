package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.migration.vanilla.Items
import chylex.hee.system.util.isTopSolid
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setBlock
import net.minecraft.block.Block
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.BlockRenderLayer.CUTOUT
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import java.util.Random

open class BlockFlowerPotCustom(builder: BlockBuilder, private val flower: Block) : BlockSimpleShaped(builder, AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.375, 0.6875)){
	private fun createFlowerStack(state: IBlockState): ItemStack{
		return ItemStack(flower, 1, getMetaFromState(state))
	}
	
	override fun canPlaceBlockAt(world: World, pos: BlockPos): Boolean{
		return super.canPlaceBlockAt(world, pos) && pos.down().isTopSolid(world)
	}
	
	override fun neighborChanged(state: IBlockState, world: World, pos: BlockPos, neighborBlock: Block, neighborPos: BlockPos){
		if (!pos.down().isTopSolid(world)){
			dropBlockAsItem(world, pos, state, 0)
			pos.setAir(world)
		}
	}
	
	override fun onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean{
		val flowerStack = createFlowerStack(state)
		
		if (player.getHeldItem(hand).isEmpty){
			player.setHeldItem(hand, flowerStack)
		}
		else if (!player.addItemStackToInventory(flowerStack)){
			player.dropItem(flowerStack, false)
		}
		
		pos.setBlock(world, Blocks.FLOWER_POT)
		return true
	}
	
	override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item{
		return Items.FLOWER_POT
	}
	
	override fun getDrops(drops: NonNullList<ItemStack>, world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int){
		drops.add(ItemStack(Items.FLOWER_POT))
		drops.add(createFlowerStack(state))
	}
	
	override fun getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack{
		return createFlowerStack(state)
	}
	
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) = UNDEFINED
	override fun getRenderLayer() = CUTOUT
}
