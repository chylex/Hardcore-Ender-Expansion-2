package chylex.hee.game.world.feature.stronghold.piece
import chylex.hee.game.block.util.FutureBlocks
import chylex.hee.game.world.feature.stronghold.StrongholdPieceType
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.trigger.FlowerPotStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Facing4
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.removeItem
import chylex.hee.system.util.withFacing
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.util.math.BlockPos

class StrongholdRoom_Main_Workshop(file: String) : StrongholdAbstractPieceFromFile(file, StrongholdPieceType.ROOM){
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		// Utility
		
		for(facing in Facing4){
			val offsetPos = Pos(centerX, 1, centerZ).offset(facing, 5)
			val revFacing = facing.opposite
			
			placeUtilityColumn(world, offsetPos.offset(facing.rotateY(), 2), revFacing)
			placeUtilityColumn(world, offsetPos.offset(facing.rotateYCCW(), 2), revFacing)
		}
		
		// Shelves
		
		val shelfPositions = (
			sequenceOf(WEST, EAST).flatMap { sequenceOf(
				Pos(centerX + (6 * it.xOffset), 3, centerZ - 5) to it,
				Pos(centerX + (6 * it.xOffset), 3, centerZ - 3) to it,
				Pos(centerX + (6 * it.xOffset), 3, centerZ + 3) to it,
				Pos(centerX + (6 * it.xOffset), 3, centerZ + 5) to it
			)} +
			sequenceOf(NORTH, SOUTH).flatMap { sequenceOf(
				Pos(centerX - 5, 3, centerZ + (6 * it.zOffset)) to it,
				Pos(centerX - 3, 3, centerZ + (6 * it.zOffset)) to it,
				Pos(centerX + 3, 3, centerZ + (6 * it.zOffset)) to it,
				Pos(centerX + 5, 3, centerZ + (6 * it.zOffset)) to it
			)}
		).toMutableList()
		
		repeat(1 + rand.nextInt(2) * rand.nextInt(3)){
			val (shelfPos, shelfFacing) = rand.removeItem(shelfPositions)
			val skullRot = shelfFacing.horizontalIndex * 4 // not quite correct but it works
			
			world.addTrigger(shelfPos, TileEntityStructureTrigger(FutureBlocks.SKULL_FLOOR, TileEntitySkull().apply { setType(2); skullRotation = skullRot }))
		}
		
		repeat(rand.nextInt(1, 6)){
			val shelfPos = rand.removeItem(shelfPositions).first
			world.setBlock(shelfPos, ModBlocks.ANCIENT_COBWEB)
		}
		
		// Tables
		
		val flowerTypes = arrayOf(
			FutureBlocks.WHITE_TULIP_STACK,
			FutureBlocks.BLUE_ORCHID_STACK,
			FutureBlocks.ALLIUM_STACK,
			FutureBlocks.POPPY_STACK
		)
		
		repeat(rand.nextInt(4, 7)){
			val facing = rand.nextItem(Facing4)
			val facingOffset = if (rand.nextBoolean()) facing.rotateY() else facing.rotateYCCW()
			val extraOffset = rand.nextInt(0, 2)
			
			val decorPos = Pos(centerX, 2, centerZ).offset(facing, 5).offset(facingOffset, 3 + extraOffset)
			
			if (rand.nextInt(4 + extraOffset) == 0){ // fewer cobwebs near corners
				world.setBlock(decorPos, ModBlocks.ANCIENT_COBWEB)
			}
			else{
				world.addTrigger(decorPos, FlowerPotStructureTrigger(rand.nextItem(flowerTypes)))
			}
		}
	}
	
	private fun placeUtilityColumn(world: IStructureWorld, pos: BlockPos, facing: EnumFacing){
		val rand = world.rand
		val furnace = Blocks.FURNACE.withFacing(facing)
		
		val (bottom, top) = when(rand.nextInt(11)){
			in 0..3 -> Single(Blocks.BOOKSHELF) to Single(Blocks.BOOKSHELF)
			in 4..5 -> Single(Blocks.BOOKSHELF) to Single(Blocks.CRAFTING_TABLE)
			in 6..8 -> Single(furnace) to Single(Blocks.CRAFTING_TABLE)
			in 9..10 -> Single(furnace) to Single(furnace)
			else -> throw IllegalStateException()
		}
		
		world.setState(pos, bottom.pick(rand))
		world.setState(pos.up(), top.pick(rand))
	}
}
