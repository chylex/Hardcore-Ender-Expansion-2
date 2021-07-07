package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import net.minecraft.util.Mirror
import java.util.Random

class TombDungeonRoom_Tomb_MultiNarrow(file: String, private val tombsPerColumn: Int, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowExit = true, allowSecrets = false, isFancy) {
	override val sidePathAttachWeight = 5
	
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
		
		if (isExitConnected(instance)) {
			for (x in 1 until maxX) for (y in 2..3) {
				world.setState(Pos(x, y, 1), world.getState(Pos(x, y, maxZ - 1)).mirror(Mirror.LEFT_RIGHT))
			}
			
			val palette = if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING
			world.setState(Pos(2, 2, 1), palette.pick(rand))
			world.setState(Pos(maxX - 2, 2, 1), palette.pick(rand))
		}
	}
	
	override fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount? {
		if (rand.nextInt(10) >= if (level.isFancy) 3 else 5) {
			return null
		}
		
		return when {
			tombsPerColumn <= 4 -> MobAmount.LOW
			tombsPerColumn <= 5 -> if (rand.nextBoolean()) MobAmount.LOW else MobAmount.MEDIUM
			tombsPerColumn <= 6 -> if (rand.nextBoolean()) MobAmount.MEDIUM else MobAmount.HIGH
			else                -> MobAmount.HIGH
		}
	}
}
