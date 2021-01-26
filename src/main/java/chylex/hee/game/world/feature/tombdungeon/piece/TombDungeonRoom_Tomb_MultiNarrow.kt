package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.structure.IStructureWorld
import java.util.Random

class TombDungeonRoom_Tomb_MultiNarrow(file: String, private val tombsPerColumn: Int, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets = false, isFancy) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextInt(10) < 4) {
			placeJars(world, instance, (0 until tombsPerColumn).flatMap {
				listOf(
					Pos(centerX - 3, 3, 2 + (2 * it)),
					Pos(centerX + 3, 3, 2 + (2 * it))
				)
			})
		}
	}
	
	override fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount? {
		if (rand.nextInt(10) >= if (level.isFancy) 3 else 5) {
			return null
		}
		
		return when {
			tombsPerColumn <= 4 -> MobAmount.LOW
			tombsPerColumn <= 6 -> MobAmount.MEDIUM
			else                -> MobAmount.HIGH
		}
	}
}
