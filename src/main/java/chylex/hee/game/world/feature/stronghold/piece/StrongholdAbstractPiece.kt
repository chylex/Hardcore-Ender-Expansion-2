package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece
import net.minecraft.init.Blocks

abstract class StrongholdAbstractPiece : StructurePiece(){
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
}
