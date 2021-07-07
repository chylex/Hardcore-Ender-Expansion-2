@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.util.math

import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.vector.Vector3d

inline fun Pos(x: Int, y: Int, z: Int) = BlockPos(x, y, z)
inline fun Pos(x: Double, y: Double, z: Double) = BlockPos(x, y, z)
inline fun Pos(vector: Vector3d) = BlockPos(vector)
inline fun Pos(entity: Entity): BlockPos = entity.position
inline fun Pos(packed: Long): BlockPos = BlockPos.fromLong(packed)

inline fun MutablePos(x: Int, y: Int, z: Int) = BlockPos.Mutable(x, y, z)
inline fun MutablePos(pos: BlockPos) = MutablePos(pos.x, pos.y, pos.z)
inline fun MutablePos() = BlockPos.Mutable()

inline val BlockPos.xz
	get() = PosXZ(this)

val BlockPos.center
	get() = Vec(x + 0.5, y + 0.5, z + 0.5)

val BlockPos.bottomCenter
	get() = Vec(x + 0.5, y.toDouble(), z + 0.5)

operator fun BlockPos.component1() = x
operator fun BlockPos.component2() = y
operator fun BlockPos.component3() = z

operator fun ChunkPos.component1() = x
operator fun ChunkPos.component2() = z
