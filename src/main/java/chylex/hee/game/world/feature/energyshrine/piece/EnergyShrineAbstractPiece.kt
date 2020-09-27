package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.EnergyShrineRoomData
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.init.ModBlocks

abstract class EnergyShrineAbstractPiece : StructurePiece<EnergyShrineRoomData>(){
	protected open val ceilingBlock
		get() = ModBlocks.GLOOMROCK_SMOOTH
	
	override fun generate(world: IStructureWorld, instance: Instance){
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		world.placeCube(Pos(1, 0, 1), Pos(maxX - 1, 0, maxZ - 1), Single(ModBlocks.GLOOMROCK))
		world.placeWalls(Pos(0, 0, 0), Pos(maxX, maxY - 1, maxZ), Single(ModBlocks.GLOOMROCK_BRICKS))
		
		world.placeCube(Pos(1, maxY - 1, 1), Pos(maxX - 1, maxY - 1, maxZ - 1), Single(ceilingBlock))
		world.placeCube(Pos(0, maxY, 0), Pos(maxX, maxY, maxZ), Single(ModBlocks.GLOOMROCK))
		
		placeConnections(world, instance)
	}
}
