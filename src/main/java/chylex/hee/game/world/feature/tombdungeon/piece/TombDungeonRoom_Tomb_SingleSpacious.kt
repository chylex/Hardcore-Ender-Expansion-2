package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.structure.IStructureWorld

class TombDungeonRoom_Tomb_SingleSpacious(file: String, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb_SingleNarrow(file, entranceY, isFancy) {
	override val mobAmount: MobAmount?
		get() = null
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextInt(10) < 7) {
			placeJars(world, instance, listOf(
				Pos(centerX - 2, 4, if (rand.nextBoolean()) maxZ - 3 else maxZ - 4),
				Pos(centerX + 2, 4, if (rand.nextBoolean()) maxZ - 3 else maxZ - 4),
			))
		}
	}
}
