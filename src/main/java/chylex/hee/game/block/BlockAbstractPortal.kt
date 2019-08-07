package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.allInBox
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.distanceTo
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getBlock
import chylex.hee.system.util.isAir
import chylex.hee.system.util.isTopSolid
import chylex.hee.system.util.math.LerpedFloat
import chylex.hee.system.util.max
import chylex.hee.system.util.min
import chylex.hee.system.util.offsetUntil
import chylex.hee.system.util.setAir
import chylex.hee.system.util.setBlock
import net.minecraft.block.Block
import net.minecraft.block.ITileEntityProvider
import net.minecraft.block.state.BlockFaceShape.UNDEFINED
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.EnumBlockRenderType.INVISIBLE
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

abstract class BlockAbstractPortal(builder: BlockBuilder) : BlockSimple(builder), ITileEntityProvider{
	companion object{
		const val MAX_DISTANCE_FROM_FRAME = 6.0
		private const val MAX_SIZE = 5
		
		private val SELECTION_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75,  1.0)
		private val COLLISION_AABB = AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.025, 1.0)
		
		fun findInnerArea(world: World, controllerPos: BlockPos, frameBlock: Block): Pair<BlockPos, BlockPos>?{
			val mirrorRange = 1..(MAX_SIZE + 1)
			val halfRange = 1..(1 + (MAX_SIZE / 2))
			
			for(facing in Facing4){
				val mirrorPos = controllerPos.offsetUntil(facing, mirrorRange){ it.getBlock(world) === frameBlock } ?: continue
				val centerPos = controllerPos.offset(facing, controllerPos.distanceTo(mirrorPos).floorToInt() / 2)
				
				val perpendicular1 = centerPos.offsetUntil(facing.rotateY(), halfRange){ it.getBlock(world) === frameBlock } ?: continue
				val perpendicular2 = centerPos.offsetUntil(facing.rotateYCCW(), halfRange){ it.getBlock(world) === frameBlock } ?: continue
				
				val minPos = controllerPos.min(mirrorPos).min(perpendicular1).min(perpendicular2).add(1, 0, 1)
				val maxPos = controllerPos.max(mirrorPos).max(perpendicular1).max(perpendicular2).add(-1, 0, -1)
				
				return minPos to maxPos
			}
			
			return null
		}
		
		fun spawnInnerBlocks(world: World, controllerPos: BlockPos, frameBlock: Block, innerBlock: Block){
			val (minPos, maxPos) = findInnerArea(world, controllerPos, frameBlock) ?: return
			
			if (minPos.allInBoxMutable(maxPos).all { it.isAir(world) }){
				minPos.allInBoxMutable(maxPos).forEach { it.setBlock(world, innerBlock) }
			}
		}
		
		fun ensureClearance(world: World, spawnPos: BlockPos, radius: Int){
			for(pos in spawnPos.add(-radius, 1, -radius).allInBox(spawnPos.add(radius, 2, radius))){
				pos.setAir(world)
			}
		}
		
		fun ensurePlatform(world: World, spawnPos: BlockPos, block: Block, radius: Int){
			for(pos in spawnPos.add(-radius, -1, -radius).allInBox(spawnPos.add(radius, -1, radius))){
				if (!pos.isTopSolid(world)){
					pos.setBlock(world, block)
				}
			}
		}
	}
	
	interface IPortalController{
		val clientAnimationProgress: LerpedFloat
	}
	
	protected abstract fun onEntityInside(world: World, pos: BlockPos, entity: Entity)
	
	final override fun onEntityCollision(world: World, pos: BlockPos, state: IBlockState, entity: Entity){
		if (!world.isRemote && !entity.isRiding && !entity.isBeingRidden && entity.isNonBoss && entity.posY <= pos.y + 0.05){
			onEntityInside(world, pos, entity)
		}
	}
	
	final override fun getCollisionBoundingBox(state: IBlockState, world: IBlockAccess, pos: BlockPos) = COLLISION_AABB
	final override fun getSelectedBoundingBox(state: IBlockState, world: World, pos: BlockPos): AxisAlignedBB = SELECTION_AABB.offset(pos)
	final override fun getBlockFaceShape(world: IBlockAccess, state: IBlockState, pos: BlockPos, face: EnumFacing) = UNDEFINED
	
	final override fun isFullCube(state: IBlockState) = false
	final override fun isOpaqueCube(state: IBlockState) = false
	final override fun getRenderType(state: IBlockState) = INVISIBLE
}
