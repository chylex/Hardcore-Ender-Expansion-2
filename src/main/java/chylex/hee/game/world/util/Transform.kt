package chylex.hee.game.world.util
import chylex.hee.system.util.Pos
import chylex.hee.system.util.component1
import chylex.hee.system.util.component2
import chylex.hee.system.util.component3
import chylex.hee.system.util.facades.Rotation4
import chylex.hee.system.util.nextItem
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
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
	
	operator fun invoke(facing: EnumFacing): EnumFacing{
		return mirroring.mirror(rotation.rotate(facing))
	}
	
	operator fun invoke(state: IBlockState): IBlockState{
		return state.withRotation(rotation).withMirror(mirroring)
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
}
