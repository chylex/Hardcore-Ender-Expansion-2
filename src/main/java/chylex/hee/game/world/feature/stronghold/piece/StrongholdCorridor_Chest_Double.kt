package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdCorridorConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.system.util.Pos
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.WEST

class StrongholdCorridor_Chest_Double(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdCorridorConnection(Pos(maxX, 0, centerZ), EAST),
		StrongholdCorridorConnection(Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.addTrigger(Pos(centerX - 1, 2, centerZ - 2), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		world.addTrigger(Pos(centerX + 1, 2, centerZ - 2), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
	}
}
