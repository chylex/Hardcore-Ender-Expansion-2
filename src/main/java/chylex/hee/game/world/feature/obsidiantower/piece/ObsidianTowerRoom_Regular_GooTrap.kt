package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.FluidStructureTrigger
import chylex.hee.init.ModBlocks

class ObsidianTowerRoom_Regular_GooTrap(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	override fun generateContents(world: IStructureWorld, instance: Instance){
		for(pos in arrayOf(
			Pos(1, 2, centerZ),
			Pos(centerX, 2, centerZ - 1),
			Pos(maxX - 1, 2, centerZ)
		)){
			placeSpawner(world, pos, instance)
			world.addTrigger(pos.up(2), FluidStructureTrigger(ModBlocks.ENDER_GOO))
		}
	}
}
