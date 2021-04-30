package chylex.hee.game.world.structure.piece

import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.structure.piece.StructureBuild.AddMode
import chylex.hee.game.world.structure.piece.StructureBuild.PositionedPiece
import chylex.hee.system.facades.Rotation4
import chylex.hee.system.random.nextItem
import java.util.Random

interface IStructureBuilder<T : IStructureBuild> {
	fun build(rand: Random): T?
	
	abstract class ProcessBase<T : StructurePiece<*>.MutableInstance>(protected val build: StructureBuild<T>, protected val rand: Random) {
		protected fun baseAddPiece(mode: AddMode, targetPiece: PositionedPiece<T>, targetConnection: IStructurePieceConnection, generatedPieceConstructor: (Transform) -> T): PositionedPiece<T>? {
			val mirror = rand.nextBoolean()
			
			for(rotation in Rotation4.randomPermutation(rand)) {
				val generatedInstance = generatedPieceConstructor(Transform(rotation, mirror))
				val connections = generatedInstance.findAvailableConnections(targetConnection)
				
				if (connections.isNotEmpty()) {
					return build.addPiece(generatedInstance, rand.nextItem(connections), targetPiece, targetConnection, mode)
				}
			}
			
			return null
		}
	}
}
