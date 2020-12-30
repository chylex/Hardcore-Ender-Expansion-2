package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.random.nextInt

class TombDungeonRoom_Tomb_MassSpacious(file: String, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets = false, isFancy) {
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
}
