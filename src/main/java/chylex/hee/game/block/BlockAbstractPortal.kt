package chylex.hee.game.block
import chylex.hee.game.block.entity.TileEntityPortalInner
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumBlockRenderType
import net.minecraft.util.EnumBlockRenderType.INVISIBLE
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

abstract class BlockAbstractPortal(builder: BlockSimple.Builder) : BlockSimple(builder), ITileEntityProvider{
	companion object{
		const val MAX_DISTANCE_FROM_FRAME = 6.0
		
		private val SELECTION_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75,  1.0)
		private val COLLISION_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.025, 1.0)
	}
	
	protected abstract fun onEntityInside(world: World, pos: BlockPos, entity: Entity)
	
	final override fun createNewTileEntity(world: World, meta: Int): TileEntity{
		return TileEntityPortalInner()
	}
	
	final override fun onEntityCollidedWithBlock(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (!world.isRemote && !entity.isRiding && !entity.isBeingRidden && entity.isNonBoss && entity.posY <= pos.y + 0.05){
			onEntityInside(world, pos, entity)
		}
	}
	
	final override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos): AxisAlignedBB = COLLISION_AABB
	final override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB = SELECTION_AABB.offset(pos)
	final override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing): BlockFaceShape = UNDEFINED
	
	final override fun isFullCube(state: IBlockState): Boolean = false
	final override fun isOpaqueCube(state: IBlockState): Boolean = false
	final override fun getRenderType(state: IBlockState): EnumBlockRenderType = INVISIBLE
}
