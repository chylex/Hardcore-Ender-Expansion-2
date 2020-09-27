package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.block.with
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.CORRIDOR
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_OUTSIDE
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.BlockSlab
import chylex.hee.system.migration.Blocks
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.random.nextInt
import net.minecraft.state.properties.SlabType

class TombDungeonDeadEnd_Tomb(length: Int, private val tombConstructor: (Boolean) -> TombDungeonAbstractPiece, override val isFancy: Boolean) : TombDungeonAbstractPiece(), ITombDungeonPieceWithTombs{
	override val size = Size(5, 5, length)
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 0
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH),
		TombDungeonConnection(TOMB_ENTRANCE_OUTSIDE, Pos(size.centerX, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		placeLayout(world)
		
		val length = size.z
		
		if (length > 1 || !instance.hasAvailableConnections){
			placeConnections(world, instance)
		}
		
		if (length > 2){
			world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 1, size.maxZ - 1), Air)
		}
		
		if (instance.hasAvailableConnections){
			val rand = world.rand
			
			if (rand.nextInt(3) != 0){
				val centerPos = Pos(size.centerX, size.centerY, 0)
				
				world.setBlock(centerPos, ModBlocks.DUSTY_STONE_DECORATION)
				
				when(rand.nextInt(0, 7)){
					0 -> {
						world.setBlock(centerPos.up(), ModBlocks.DUSTY_STONE_DECORATION)
						world.setBlock(centerPos.down(), ModBlocks.DUSTY_STONE_DECORATION)
					}
					
					1 -> {
						world.setBlock(centerPos.east(), ModBlocks.DUSTY_STONE_DECORATION)
						world.setBlock(centerPos.west(), ModBlocks.DUSTY_STONE_DECORATION)
					}
				}
			}
			else if (rand.nextInt(4) != 0 && length > 1){
				world.setBlock(Pos(1, 1, 1), ModBlocks.DUSTY_STONE_BRICKS)
				world.setBlock(Pos(2, 1, 1), ModBlocks.DUSTY_STONE_DECORATION)
				world.setBlock(Pos(3, 1, 1), ModBlocks.DUSTY_STONE_BRICKS)
				world.placeCube(Pos(1, size.maxY - 1, 1), Pos(3, size.maxY - 1, 1), Single(ModBlocks.DUSTY_STONE_BRICK_SLAB.with(BlockSlab.TYPE, SlabType.TOP)))
				
				val shelfPos = Pos(2, 2, 1)
				
				if (rand.nextInt(3) == 0){
					placeChest(world, instance, shelfPos, SOUTH)
				}
				else{
					world.setBlock(shelfPos, Blocks.REDSTONE_TORCH)
				}
			}
		}
		
		placeCobwebs(world, instance)
	}
	
	override fun constructTomb(): TombDungeonAbstractPiece{
		return tombConstructor(isFancy)
	}
}
