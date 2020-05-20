package chylex.hee.game.world.util
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.vanilla.BlockChest
import chylex.hee.system.migration.vanilla.BlockStairs
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.facades.Rotation4
import chylex.hee.system.util.nextItem
import net.minecraft.block.BlockState
import net.minecraft.entity.Entity
import net.minecraft.state.properties.StairsShape.INNER_LEFT
import net.minecraft.state.properties.StairsShape.INNER_RIGHT
import net.minecraft.state.properties.StairsShape.OUTER_LEFT
import net.minecraft.state.properties.StairsShape.OUTER_RIGHT
import net.minecraft.state.properties.StairsShape.STRAIGHT
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.Mirror
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import java.util.Random

data class Transform(val rotation: Rotation, val mirror: Boolean){
	companion object{
		val NONE = Transform(Rotation.NONE, mirror = false)
		val ALL = booleanArrayOf(false, true).flatMap { mirror -> Rotation4.map { rotation -> Transform(rotation, mirror) } }
		
		fun random(rand: Random) = rand.nextItem(ALL)
	}
	
	private val mirroring = if (mirror) Mirror.FRONT_BACK else Mirror.NONE
	
	val reverse
		get() = when(rotation){
			Rotation.NONE ->
				Transform(Rotation.NONE, mirror)
			
			Rotation.CLOCKWISE_90 ->
				Transform(if (mirror) Rotation.CLOCKWISE_90 else Rotation.COUNTERCLOCKWISE_90, mirror)
			
			Rotation.CLOCKWISE_180 ->
				Transform(Rotation.CLOCKWISE_180, mirror)
			
			Rotation.COUNTERCLOCKWISE_90 ->
				Transform(if (mirror) Rotation.COUNTERCLOCKWISE_90 else Rotation.CLOCKWISE_90, mirror)
		}
	
	fun applyTo(target: Transform): Transform{
		return Transform(target.rotation.add(rotation), target.mirror xor mirror)
	}
	
	operator fun invoke(facing: Direction): Direction{
		return mirroring.mirror(rotation.rotate(facing))
	}
	
	operator fun invoke(state: BlockState): BlockState{
		val transformed = state.rotate(rotation).mirror(mirroring)
		
		if (mirror){
			when(state.block){
				is BlockStairs -> return unfuckStairMirror(transformed) // UPDATE 1.14 (check if stairs still need unfucking)
				is BlockChest -> return unfuckChestMirror(transformed) // UPDATE 1.14 (check if chests still need unfucking)
			}
		}
		
		return transformed
	}
	
	operator fun invoke(entity: Entity){
		entity.rotationYaw = entity.getRotatedYaw(rotation)
		entity.rotationYaw = entity.getMirroredYaw(mirroring)
	}
	
	operator fun invoke(tile: TileEntity){
		tile.rotate(rotation)
		tile.mirror(mirroring)
	}
	
	operator fun invoke(size: Size): Size{
		return size.rotate(rotation)
	}
	
	operator fun invoke(pos: BlockPos, size: Size): BlockPos{
		val (x, y, z) = pos
		
		val maxX = size.maxX
		val maxZ = size.maxZ
		
		val transformedX: Int
		val transformedZ: Int
		
		when(rotation){
			Rotation.NONE ->
			{ transformedX = if (mirror) maxX - x else x; transformedZ = z }
			
			Rotation.CLOCKWISE_90 ->
			{ transformedX = if (mirror) z else maxZ - z; transformedZ = x }
			
			Rotation.CLOCKWISE_180 ->
			{ transformedX = if (mirror) x else maxX - x; transformedZ = maxZ - z }
			
			Rotation.COUNTERCLOCKWISE_90 ->
			{ transformedX = if (mirror) maxZ - z else z; transformedZ = maxX - x }
		}
		
		return Pos(transformedX, y, transformedZ)
	}
	
	// Unfucking
	
	private fun unfuckStairMirror(state: BlockState): BlockState{
		return when(state[BlockStairs.SHAPE]){
			STRAIGHT    -> state
			INNER_LEFT  -> state.with(BlockStairs.SHAPE, INNER_RIGHT)
			INNER_RIGHT -> state.with(BlockStairs.SHAPE, INNER_LEFT)
			OUTER_LEFT  -> if (state[BlockStairs.FACING].let { it === NORTH || it === SOUTH }) state.with(BlockStairs.SHAPE, OUTER_RIGHT) else state
			OUTER_RIGHT -> if (state[BlockStairs.FACING].let { it === NORTH || it === SOUTH }) state.with(BlockStairs.SHAPE, OUTER_LEFT) else state
			else        -> state
		}
	}
	
	private fun unfuckChestMirror(state: BlockState): BlockState{
		return state.with(BlockChest.TYPE, state[BlockChest.TYPE].opposite())
	}
}
