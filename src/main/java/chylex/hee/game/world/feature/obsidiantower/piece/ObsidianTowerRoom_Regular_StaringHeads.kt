package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld

class ObsidianTowerRoom_Regular_StaringHeads(file: String) : ObsidianTowerRoom_General(file){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeEndermanHead(world, Pos(1, 3, 3), rotation = 6)
		placeEndermanHead(world, Pos(2, 3, 2), rotation = 7)
		
		placeEndermanHead(world, Pos(maxX - 2, 3, 2), rotation = 9)
		placeEndermanHead(world, Pos(maxX - 1, 3, 3), rotation = 10)
	}
}
