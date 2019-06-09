package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType.CORRIDOR
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.util.Transform
import chylex.hee.system.util.ceilToInt
import net.minecraft.util.EnumFacing
import kotlin.math.pow

abstract class StrongholdAbstractPiece : StructurePiece(){
	abstract val type: StrongholdPieceType
	
	protected open val extraWeightMultiplier = 1
	protected open val isEyeOfEnderTarget = false
	
	override fun generate(world: IStructureWorld, instance: Instance){
		world.placeCubeHollow(size.minPos, size.maxPos, StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		placeConnections(world, instance)
	}
	
	inner class StrongholdInst(val distanceToPortal: Int, val facingFromPortal: EnumFacing?, transform: Transform) : MutableInstance(transform){
		private val weightMultiplier = (type.weightMultiplier * extraWeightMultiplier) / (distanceToPortal + 1F).pow(0.2F)
		
		val type
			get() = this@StrongholdAbstractPiece.type
		
		val isEyeOfEnderTarget
			get() = this@StrongholdAbstractPiece.isEyeOfEnderTarget && distanceToPortal in 4..6
		
		val canLeadIntoDeadEnd
			get() = type == CORRIDOR && hasAvailableConnections
		
		val pickWeight: Int
			get(){
				val connections = findAvailableConnections().size
				
				return if (connections == 0)
					0
				else
					connections.toFloat().pow(type.weightConnectionExponent).times(weightMultiplier).ceilToInt()
			}
	}
}
