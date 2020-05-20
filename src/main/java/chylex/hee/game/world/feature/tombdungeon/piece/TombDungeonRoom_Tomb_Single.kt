package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.util.Pos

class TombDungeonRoom_Tomb_Single(file: String, entranceY: Int, allowSecrets: Boolean, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets, isFancy){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		if (world.rand.nextInt(10) < 3){
			placeChest(world, Pos(centerX, 1, maxZ - 4), SOUTH)
		}
	}
}
