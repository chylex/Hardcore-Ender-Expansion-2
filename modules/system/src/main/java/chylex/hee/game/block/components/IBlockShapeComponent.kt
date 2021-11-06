package chylex.hee.game.block.components

import chylex.hee.game.block.util.asVoxelShape
import net.minecraft.block.BlockState
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.shapes.VoxelShape

interface IBlockShapeComponent {
	fun getShape(state: BlockState): VoxelShape
	
	fun getCollisionShape(state: BlockState): VoxelShape? {
		return null
	}
	
	fun getRaytraceShape(state: BlockState): VoxelShape? {
		return null
	}
	
	companion object {
		fun of(shape: VoxelShape) = object : IBlockShapeComponent {
			override fun getShape(state: BlockState): VoxelShape {
				return shape
			}
		}
		
		fun of(aabb: AxisAlignedBB): IBlockShapeComponent {
			return of(aabb.asVoxelShape)
		}
	}
}
