package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.StructurePiece
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.withFacing
import net.minecraft.block.Blocks
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

abstract class TombDungeonAbstractPiece : StructurePiece<Unit>(){
	protected abstract val isFancy: Boolean
	
	abstract val sidePathAttachWeight: Int
	abstract val secretAttachWeight: Int
	open val secretAttachY = 0
	
	override fun generate(world: IStructureWorld, instance: Instance){
		placeLayout(world)
		placeConnections(world, instance)
	}
	
	protected fun placeLayout(world: IStructureWorld){
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		world.placeCube(Pos(0, 0, 0), Pos(maxX, 0, maxZ), Single(ModBlocks.DUSTY_STONE))
		world.placeWalls(Pos(0, 1, 0), Pos(maxX, maxY, maxZ), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		
		if (size.x > 1 && size.z > 1){
			world.placeCube(Pos(1, maxY, 1), Pos(maxX - 1, maxY, maxZ - 1), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_CEILING else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		}
	}
	
	protected fun placeChest(world: IStructureWorld, pos: BlockPos, facing: Direction, secret: Boolean = false){
		world.setState(pos, Blocks.CHEST.withFacing(facing))
		world.addTrigger(pos, LootChestStructureTrigger(Resource.Custom("chests/stronghold_generic"), world.rand.nextLong())) // TODO
	}
}
