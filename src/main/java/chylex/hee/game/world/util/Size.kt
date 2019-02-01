package chylex.hee.game.world.util
import chylex.hee.system.util.Pos
import net.minecraft.util.Rotation
import net.minecraft.util.Rotation.CLOCKWISE_180
import net.minecraft.util.Rotation.CLOCKWISE_90
import net.minecraft.util.Rotation.COUNTERCLOCKWISE_90
import net.minecraft.util.Rotation.NONE
import net.minecraft.util.math.BlockPos

data class Size(val x: Int, val y: Int, val z: Int){
	val maxX get() = x - 1
	val maxY get() = y - 1
	val maxZ get() = z - 1
	
	val centerX get() = x / 2
	val centerY get() = y / 2
	val centerZ get() = z / 2
	
	val minPos: BlockPos
		get() = BlockPos.ORIGIN
	
	val maxPos: BlockPos
		get() = Pos(maxX, maxY, maxZ)
	
	val centerPos: BlockPos
		get() = Pos(centerX, centerY, centerZ)
	
	fun rotate(rotation: Rotation) = when(rotation){
		NONE, CLOCKWISE_180 -> this
		CLOCKWISE_90, COUNTERCLOCKWISE_90 -> Size(z, y, x)
	}
	
	fun toBoundingBox(offset: BlockPos): BoundingBox{
		return BoundingBox(offset, offset.add(maxX, maxY, maxZ))
	}
}