package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextRounded
import chylex.hee.util.math.Pos
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Mirror
import java.util.Random

class TombDungeonRoom_Tomb_MultiDeep(file: String, private val tombsPerColumn: Int, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowExit = true, allowSecrets = false, isFancy) {
	override val sidePathAttachWeight = 6
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		val chests = rand.nextInt(0, (tombsPerColumn * 2) / 5) * rand.nextRounded(0.85F)
		
		repeat(chests) {
			val (offset, facing) = if (rand.nextBoolean())
				-1 to EAST
			else
				1 to WEST
			
			if (rand.nextInt(7) <= 1) {
				val pos = Pos(centerX + (3 * offset), 1, 4 + (2 * rand.nextInt(tombsPerColumn)))
				if (world.getBlock(pos) is BlockGraveDirt) {
					placeChest(world, instance, pos, facing)
					
					if (rand.nextInt(9) == 0) {
						world.setAir(pos.add(0, 1, 0))
						world.setAir(pos.add(-offset, 1, 0))
					}
				}
			}
			else {
				val pos = Pos(centerX + (4 * offset), 3, 4 + (2 * rand.nextInt(tombsPerColumn)))
				if (world.isAir(pos)) {
					placeChest(world, instance, pos, facing)
				}
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
		
		if (isExitConnected(instance)) {
			for (x in 1 until maxX) for (y in 3..6) for (z in 0..2) {
				world.setState(Pos(x, y, z), world.getState(Pos(x, y, maxZ - z)).mirror(Mirror.LEFT_RIGHT))
			}
		}
	}
	
	override fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount? {
		if (rand.nextBoolean()) {
			return null
		}
		
		return when {
			tombsPerColumn <= 4 -> if (rand.nextBoolean()) MobAmount.LOW else MobAmount.MEDIUM
			tombsPerColumn <= 5 -> if (rand.nextInt(3) != 0) MobAmount.MEDIUM else MobAmount.HIGH
			tombsPerColumn <= 6 -> if (rand.nextInt(2) != 0) MobAmount.MEDIUM else MobAmount.HIGH
			tombsPerColumn <= 7 -> if (rand.nextInt(4) == 0) MobAmount.MEDIUM else MobAmount.HIGH
			else                -> MobAmount.HIGH
		}
	}
}
