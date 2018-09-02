package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityEnergyCluster
import chylex.hee.game.block.material.Materials
import chylex.hee.system.util.getTileEntity
import chylex.hee.system.util.nextItem
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.SoundType.GLASS
import net.minecraft.block.material.MapColor
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.INVISIBLE
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

class BlockEnergyCluster : Block(Materials.ENERGY_CLUSTER, MapColor.SNOW), ITileEntityProvider{
	init{
		blockSoundType = GLASS
		lightValue = 13
		lightOpacity = 0
		fullBlock = false
		enableStats = false
	}
	
	companion object{
		private val SELECTION_AABB = AxisAlignedBB(0.35, 0.35, 0.35, 0.65, 0.65, 0.65)
	}
	
	override fun createNewTileEntity(world: World, meta: Int): TileEntity?{
		return TileEntityEnergyCluster()
	}
	
	override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB? = NULL_AABB
	override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB = SELECTION_AABB.offset(pos)
	override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	override fun isFullCube(state: IBlockState?): Boolean = false
	override fun isOpaqueCube(state: IBlockState?): Boolean = false
	override fun getRenderType(state: IBlockState?): EnumBlockRenderType = INVISIBLE
}
