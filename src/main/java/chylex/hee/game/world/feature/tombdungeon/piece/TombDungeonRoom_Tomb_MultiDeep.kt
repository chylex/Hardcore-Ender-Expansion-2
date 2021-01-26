package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextRounded
import java.util.Random

class TombDungeonRoom_Tomb_MultiDeep(file: String, private val tombsPerColumn: Int, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets = false, isFancy) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		val chests = rand.nextInt(0, (tombsPerColumn * 2) / 5) * rand.nextRounded(0.66F)
		
		repeat(chests) {
			val (offset, facing) = if (rand.nextBoolean())
				-3 to EAST
			else
				3 to WEST
			
			val pos = Pos(centerX + offset, 1, 4 + (2 * rand.nextInt(tombsPerColumn)))
			
			if (world.getBlock(pos) is BlockGraveDirt) {
				placeChest(world, instance, pos, facing)
			}
		}
		
		if (rand.nextBoolean()) {
			placeJars(world, instance, (0 until tombsPerColumn).flatMap {
				listOf(
					Pos(centerX - 4, 3, 4 + (2 * it)),
					Pos(centerX + 4, 3, 4 + (2 * it))
				)
			})
		}
	}
	
	override fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount? {
		if (rand.nextBoolean()) {
			return null
		}
		
		return when {
			tombsPerColumn <= 6 -> MobAmount.MEDIUM
			else                -> MobAmount.HIGH
		}
	}
}
