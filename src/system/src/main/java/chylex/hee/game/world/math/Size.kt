package chylex.hee.game.world.math

import chylex.hee.game.world.Pos
import chylex.hee.game.world.math.Size.Alignment.CENTER
import chylex.hee.game.world.math.Size.Alignment.MAX
import chylex.hee.game.world.math.Size.Alignment.MIN
import chylex.hee.system.math.component1
import chylex.hee.system.math.component2
import chylex.hee.system.math.component3
import net.minecraft.util.Rotation
import net.minecraft.util.Rotation.CLOCKWISE_180
import net.minecraft.util.Rotation.CLOCKWISE_90
import net.minecraft.util.Rotation.COUNTERCLOCKWISE_90
import net.minecraft.util.Rotation.NONE
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3i

data class Size(val x: Int, val y: Int, val z: Int) {
	constructor(xyz: Int) : this(xyz, xyz, xyz)
	
	enum class Alignment {
		MIN, CENTER, MAX
	}
	
	val maxX get() = x - 1
	val maxY get() = y - 1
	val maxZ get() = z - 1
	
	val centerX get() = x / 2
	val centerY get() = y / 2
	val centerZ get() = z / 2
	
	val minPos: BlockPos
		get() = BlockPos.ZERO
	
	val maxPos: BlockPos
		get() = Pos(maxX, maxY, maxZ)
	
	val centerPos: BlockPos
		get() = Pos(centerX, centerY, centerZ)
	
	fun getPos(xAlignment: Alignment, yAlignment: Alignment, zAlignment: Alignment): BlockPos {
		return Pos(
			getPos(xAlignment).x,
			getPos(yAlignment).y,
			getPos(zAlignment).z
		)
	}
	
	fun getPos(alignment: Alignment): BlockPos {
		return when(alignment) {
			MIN    -> minPos
			CENTER -> centerPos
			MAX    -> maxPos
		}
	}
	
	fun rotate(rotation: Rotation) = when(rotation) {
		NONE, CLOCKWISE_180               -> this
		CLOCKWISE_90, COUNTERCLOCKWISE_90 -> Size(z, y, x)
	}
	
	fun expand(by: Vector3i): Size {
		return Size(x + by.x, y + by.y, z + by.z)
	}
	
	fun toBoundingBox(offset: BlockPos): BoundingBox {
		return BoundingBox(offset, offset.add(maxX, maxY, maxZ))
	}
	
	fun toCenteredBoundingBox(offset: Vector3d): AxisAlignedBB {
		val (x, y, z) = offset
		return AxisAlignedBB(x - centerX, y - centerY, z - centerZ, x + centerX, y + centerY, z + centerZ)
	}
}
