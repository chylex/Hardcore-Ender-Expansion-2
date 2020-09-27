package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.feature.stronghold.StrongholdPieces
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.facades.Facing4
import chylex.hee.system.migration.Blocks
import chylex.hee.system.random.nextInt
import chylex.hee.system.random.nextItem

class StrongholdRoom_Chest_DecoratedCorners(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		// Unchiseling
		
		if (rand.nextInt(3) == 0){
			world.placeCube(Pos(centerX, 0, 2), Pos(centerX, 0, maxZ - 2), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
			world.placeCube(Pos(2, 0, centerZ), Pos(maxX - 2, 0, centerZ), StrongholdPieces.PALETTE_ENTRY_STONE_BRICK)
		}
		
		// Chests
		
		repeat(rand.nextInt(1, 2)){
			val facing = rand.nextItem(Facing4)
			val chestFacingOffset = if (rand.nextBoolean()) facing else facing.rotateYCCW()
			val chestPos = Pos(centerX, 2, centerZ).offset(facing.rotateYCCW(), 3).offset(facing, 3).offset(chestFacingOffset)
			
			if (world.isAir(chestPos)){
				world.setState(chestPos, Blocks.CHEST.withFacing(chestFacingOffset.opposite))
				world.addTrigger(chestPos, LootChestStructureTrigger(StrongholdPieces.LOOT_GENERIC, rand.nextLong()))
			}
		}
		
		// Decorations
		
		val flowerPotTypes = arrayOf(
			Blocks.POTTED_RED_TULIP,
			Blocks.POTTED_BLUE_ORCHID,
			Blocks.POTTED_OXEYE_DAISY,
			Blocks.POTTED_AZURE_BLUET,
			Blocks.POTTED_DANDELION
		)
		
		repeat(rand.nextInt(3, 5)){
			val facing = rand.nextItem(Facing4)
			val decorPos = Pos(centerX, 2, centerZ).offset(facing, 4).offset(if (rand.nextBoolean()) facing.rotateY() else facing.rotateYCCW(), 3)
			
			if (world.isAir(decorPos)){
				if (rand.nextInt(7) <= 2){
					world.setBlock(decorPos, ModBlocks.ANCIENT_COBWEB)
				}
				else{
					world.setBlock(decorPos, rand.nextItem(flowerPotTypes))
				}
			}
		}
	}
}
