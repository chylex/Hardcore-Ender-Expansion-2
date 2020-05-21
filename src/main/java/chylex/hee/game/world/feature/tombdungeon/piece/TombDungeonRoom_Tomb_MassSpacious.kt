package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt

class TombDungeonRoom_Tomb_MassSpacious(file: String, entranceY: Int, allowSecrets: Boolean, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets, isFancy){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		if (rand.nextInt(3) == 0){
			val chestX = if (rand.nextBoolean())
				rand.nextInt(1, 2)
			else
				maxX - rand.nextInt(1, 2)
			
			placeChest(world, instance, Pos(chestX, 3, maxZ - 2), NORTH)
		}
	}
}
