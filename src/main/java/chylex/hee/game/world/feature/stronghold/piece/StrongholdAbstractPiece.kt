package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.system.util.ceilToInt
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.Rotation
import kotlin.math.pow

abstract class StrongholdAbstractPiece : StructurePiece(){
	abstract val type: StrongholdPieceType
	
	override fun generate(world: IStructureWorld, instance: Instance){
		world.placeCubeHollow(size.minPos, size.maxPos, StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		
		for(connection in connections){
			if (instance.isConnectionUsed(connection)){
				val offset = connection.offset
				val perpendicular = connection.facing.rotateY()
				
				val addX = perpendicular.xOffset
				val addZ = perpendicular.zOffset
				
				world.placeCube(offset.add(-addX, 1, -addZ), offset.add(addX, 3, addZ), Single(Blocks.AIR))
			}
		}
	}
	
	inner class StrongholdInst(val distanceToPortal: Int, val facingFromPortal: EnumFacing?, rotation: Rotation) : MutableInstance(rotation){
		private val weightMultiplier = type.weightMultiplier / (distanceToPortal + 1F).pow(0.2F)
		
		val type
			get() = this@StrongholdAbstractPiece.type
		
		val pickWeight: Int
			get(){
				val connections = findValidConnections().size
				
				return if (connections == 0)
					0
				else
					connections.toFloat().pow(type.weightConnectionExponent).times(weightMultiplier).ceilToInt()
			}
	}
}
