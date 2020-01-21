package chylex.hee.game.world.util
import chylex.hee.system.util.Pos
import chylex.hee.system.util.square
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.gen.Heightmap
import kotlin.math.sqrt

data class PosXZ(val x: Int, val z: Int){
	constructor(pos: BlockPos) : this(pos.x, pos.z)
	
	val chunkX
		get() = x shr 4
	
	val chunkZ
		get() = z shr 4
	
	fun add(x: Int, z: Int): PosXZ{
		return PosXZ(this.x + x, this.z + z)
	}
	
	fun offset(facing: Direction): PosXZ{
		return PosXZ(this.x + facing.xOffset, this.z + facing.zOffset)
	}
	
	fun withY(y: Int): BlockPos{
		return Pos(x, y, z)
	}
	
	fun distanceSqTo(pos: PosXZ): Double{
		return (square(pos.x - x) + square(pos.z - z)).toDouble()
	}
	
	fun distanceTo(pos: PosXZ): Double{
		return sqrt(distanceSqTo(pos))
	}
	
	fun getTopBlock(world: World, type: Heightmap.Type): BlockPos{
		return withY(world.getHeight(type, x, z))
	}
}
