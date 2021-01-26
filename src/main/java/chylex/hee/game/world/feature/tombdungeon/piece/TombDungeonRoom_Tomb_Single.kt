package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.SOUTH

abstract class TombDungeonRoom_Tomb_Single(file: String, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets = false, isFancy) {
	final override val mobAmount: MobAmount?
		get() = null
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		if (world.rand.nextInt(10) < 3) {
			placeChest(world, instance, Pos(centerX, 1, maxZ - 4), SOUTH)
		}
	}
	
	protected fun placeSingleTombUndreadSpawner(world: IStructureWorld) {
		MobSpawnerTrigger.place(world, entrance = connections.first().offset, width = maxX, depth = maxZ, undreads = 1, spiderlings = 0)
	}
}
