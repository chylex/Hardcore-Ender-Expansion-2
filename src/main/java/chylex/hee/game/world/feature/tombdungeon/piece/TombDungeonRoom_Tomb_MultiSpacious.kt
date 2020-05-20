package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextRounded

class TombDungeonRoom_Tomb_MultiSpacious(file: String, private val tombsPerColumn: Int, entranceY: Int, allowSecrets: Boolean, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowSecrets, isFancy){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		val chests = rand.nextInt(0, (tombsPerColumn * 2) / 5) * rand.nextRounded(0.77F)
		
		repeat(chests){
			val (offset, facing) = if (rand.nextBoolean())
				-3 to EAST
			else
				3 to WEST
			
			val pos = Pos(centerX + offset, 1, 3 + (3 * rand.nextInt(tombsPerColumn)))
			
			if (world.getBlock(pos) is BlockGraveDirt){
				placeChest(world, pos, facing)
			}
		}
	}
}
