package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.structure.IStructureWorld

class TombDungeonRoom_Tomb_SingleNarrow(file: String, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb_Single(file, entranceY, isFancy) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		if (world.rand.nextInt(5) == 0) {
			placeSingleTombUndreadSpawner(world)
		}
	}
}
