package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.feature.stronghold.connection.StrongholdRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull

class StrongholdRoom_Trap_Prison(file: String) : StrongholdAbstractPieceFromFile(file){
	override val connections = arrayOf<IStructurePieceConnection>(
		StrongholdRoomConnection(Pos(maxX, 0, maxZ - 2), EAST),
		StrongholdRoomConnection(Pos(0, 0, maxZ - 2), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		// TODO silverfish trap trigger
		
		val rand = world.rand
		
		// Chest
		
		world.addTrigger(Pos(1, 1, 1), LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
		
		// Skull
		
		val skullType = if (rand.nextInt(3) == 0) 2 else 0
		world.addTrigger(Pos(maxX - 1, 1, 1), TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, TileEntitySkull().apply { setType(skullType); skullRotation = 10 }))
		
		// Redstone
		
		repeat(5 + rand.nextInt(5 + rand.nextInt(6))){
			val redstonePos = Pos(rand.nextInt(1, maxX - 1), 1, rand.nextInt(1, 5))
			
			if (world.isAir(redstonePos)){
				world.setBlock(redstonePos, Blocks.REDSTONE_WIRE)
			}
		}
	}
}
