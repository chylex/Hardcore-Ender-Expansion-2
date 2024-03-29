package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import net.minecraft.util.Direction.NORTH
import java.util.Random

class TombDungeonRoom_Tomb_MassSpacious(file: String, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowExit = false, allowSecrets = false, isFancy) {
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextInt(3) == 0) {
			val chestX = if (rand.nextBoolean())
				rand.nextInt(1, 2)
			else
				maxX - rand.nextInt(1, 2)
			
			placeChest(world, instance, Pos(chestX, 3, maxZ - 2), NORTH)
		}
		
		val jarChance = if (instance.context.let { it == null || it < TombDungeonLevel.THIRD }) 5 else 3
		
		if (rand.nextInt(jarChance) == 0) {
			val jarX = if (rand.nextBoolean())
				rand.nextInt(1, 3)
			else
				maxX - rand.nextInt(1, 3)
			
			placeJars(world, instance, listOf(Pos(jarX, 3, maxZ - 2)))
		}
	}
	
	override fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount? {
		return MobAmount.HIGH.takeIf { rand.nextInt(11) < (if (level <= TombDungeonLevel.SECOND) 7 else 4) }
	}
}
