package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.random.nextInt

class ObsidianTowerRoom_Regular_IronSupports(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		when(world.rand.nextInt(0, if (instance.context?.spawnerLevel == LEVEL_1) 1 else 3)){
			0 -> placeSpawner(world, Pos(1, maxY - 1, centerZ), instance)
			1 -> placeSpawner(world, Pos(maxX - 1, maxY - 1, centerZ), instance)
		}
	}
}
