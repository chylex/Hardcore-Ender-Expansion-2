package chylex.hee.game.world.util
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.floorToInt
import chylex.hee.system.util.getState
import chylex.hee.system.util.lookDirVec
import chylex.hee.system.util.lookPosVec
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayer.REACH_DISTANCE
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.DOWN
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class RayTracer(
	private val canCollideCheck: (IBlockState) -> Boolean
){
	fun traceAtPos(world: World, pos: BlockPos, vecFrom: Vec3d, vecTo: Vec3d): RayTraceResult?{
		val state = pos.getState(world)
		
		return if (canCollideCheck(state))
			state.collisionRayTrace(world, pos, vecFrom, vecTo)
		else
			null
	}
	
	fun traceBlocksBetweenVectors(world: World, vecFrom: Vec3d, vecTo: Vec3d): RayTraceResult?{
		var vecAt = vecFrom
		
		val startPos = Pos(vecAt)
		val earlyResult = traceAtPos(world, startPos, vecAt, vecTo)
		
		if (earlyResult != null){
			return earlyResult
		}
		
		var (xAt, yAt, zAt) = startPos
		val (xTo, yTo, zTo) = Pos(vecTo)
		
		repeat(200){
			if (vecAt.x.isNaN() || vecAt.y.isNaN() || vecAt.z.isNaN()){
				return null
			}
			
			if (xAt == xTo && yAt == yTo && zAt == zTo){
				return null
			}
			
			val offsetX = when{
				xTo > xAt -> xAt + 1.0
				xTo < xAt -> xAt + 0.0
				else -> Double.NaN
			}
			
			val offsetY = when{
				yTo > yAt -> yAt + 1.0
				yTo < yAt -> yAt + 0.0
				else -> Double.NaN
			}
			
			val offsetZ = when{
				zTo > zAt -> zAt + 1.0
				zTo < zAt -> zAt + 0.0
				else -> Double.NaN
			}
			
			var adjX = 999.0
			var adjY = 999.0
			var adjZ = 999.0
			
			val (diffX, diffY, diffZ) = vecTo.subtract(vecAt)
			
			if (!offsetX.isNaN()){
				adjX = sanitizeAdjValue((offsetX - vecAt.x) / diffX)
			}
			
			if (!offsetY.isNaN()){
				adjY = sanitizeAdjValue((offsetY - vecAt.y) / diffY)
			}
			
			if (!offsetZ.isNaN()){
				adjZ = sanitizeAdjValue((offsetZ - vecAt.z) / diffZ)
			}
			
			val facing: EnumFacing
			
			if (adjX < adjY && adjX < adjZ){
				facing = if (xTo > xAt) WEST else EAST
				
				vecAt = Vec3d(
					offsetX,
					vecAt.y + (diffY * adjX),
					vecAt.z + (diffZ * adjX)
				)
			}
			else if (adjY < adjZ){
				facing = if (yTo > yAt) DOWN else UP
				
				vecAt = Vec3d(
					vecAt.x + (diffX * adjY),
					offsetY,
					vecAt.z + (diffZ * adjY)
				)
			}
			else{
				facing = if (zTo > zAt) NORTH else SOUTH
				
				vecAt = Vec3d(
					vecAt.x + (diffX * adjZ),
					vecAt.y + (diffY * adjZ),
					offsetZ
				)
			}
			
			xAt = vecAt.x.floorToInt() - (if (facing == EAST) 1 else 0)
			yAt = vecAt.y.floorToInt() - (if (facing == UP) 1 else 0)
			zAt = vecAt.z.floorToInt() - (if (facing == SOUTH) 1 else 0)
			
			val nextPos = Pos(xAt, yAt, zAt)
			val posResult = traceAtPos(world, nextPos, vecAt, vecTo)
			
			if (posResult != null){
				return posResult
			}
		}
		
		return null
	}
	
	fun traceBlocksInPlayerReach(player: EntityPlayer): RayTraceResult?{
		val vecFrom = player.lookPosVec
		val vecTo = vecFrom.add(player.lookDirVec.scale(player.getEntityAttribute(REACH_DISTANCE).attributeValue))
		
		return traceBlocksBetweenVectors(player.world, vecFrom, vecTo)
	}
	
	// Utilities
	
	private fun sanitizeAdjValue(value: Double): Double{
		return if (value == -0.0)
			-1.0E-4
		else
			value
	}
}
