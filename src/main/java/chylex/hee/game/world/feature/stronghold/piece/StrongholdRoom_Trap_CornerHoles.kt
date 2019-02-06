package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.structure.IStructureWorld

class StrongholdRoom_Trap_CornerHoles(file: String) : StrongholdAbstractPieceFromFile(file){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		// TODO silverfish trap trigger
	}
}
