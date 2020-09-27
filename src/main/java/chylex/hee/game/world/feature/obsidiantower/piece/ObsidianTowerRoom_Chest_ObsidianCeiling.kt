package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld

class ObsidianTowerRoom_Chest_ObsidianCeiling(file: String) : ObsidianTowerRoom_General(file){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeLootTrigger(world, Pos(centerX, 1, centerZ), isSpecial = true)
	}
}
