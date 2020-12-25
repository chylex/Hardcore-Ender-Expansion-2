package chylex.hee.game.world.feature.obsidiantower.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.SOUTH

class ObsidianTowerRoom_Regular_SideWorkTables(file: String) : ObsidianTowerRoom_General(file) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		placeFurnace(world, Pos(if (world.rand.nextBoolean()) 1 else maxX - 1, 2, 3), SOUTH)
	}
}
