package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld

class ObsidianTowerRoom_Rare_FenceBlockage(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		placeLootTrigger(world, Pos(centerX + (4 * (if (world.rand.nextBoolean()) 1 else -1)), 1, centerZ), isSpecial = true)
		
		for(y in 2..4){
			placeSpawner(world, Pos(centerX - 2, y, 1), instance)
			placeSpawner(world, Pos(centerX + 2, y, 1), instance)
		}
	}
}
