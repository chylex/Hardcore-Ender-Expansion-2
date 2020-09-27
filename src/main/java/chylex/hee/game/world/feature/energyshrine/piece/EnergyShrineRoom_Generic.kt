package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.EnergyShrineRoomData
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.IBlockPicker
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.palette.ColoredBlocks
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos

abstract class EnergyShrineRoom_Generic(file: String) : EnergyShrineAbstractPiece(), IStructurePieceFromFile by Delegate("energyshrine/$file", EnergyShrinePieces.PALETTE){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(centerX - 1, 0, 0), NORTH),
		EnergyShrineConnection(ROOM, Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(maxX, 0, centerZ - 1), EAST),
		EnergyShrineConnection(ROOM, Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		world.placeCube(Pos(1, 0, 1), Pos(maxX - 1, 0, maxZ - 1), Single(ModBlocks.GLOOMROCK_SMOOTH))
		
		val cornerBlock = getContext(instance).cornerBlock
		world.setBlock(Pos(1, 0, 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 0, 1), cornerBlock)
		world.setBlock(Pos(1, 0, maxZ - 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 0, maxZ - 1), cornerBlock)
		
		generator.generate(world)
	}
	
	protected fun getContext(instance: Instance): EnergyShrineRoomData{
		return instance.context ?: EnergyShrineRoomData.DEFAULT
	}
	
	protected fun placeWallBanner(world: IStructureWorld, instance: Instance, pos: BlockPos, facing: Direction){
		val (color, data) = EnergyShrineBanners.generate(world.rand, getContext(instance).bannerColors)
		val state = ColoredBlocks.WALL_BANNER.getValue(color).withFacing(facing)
		
		world.addTrigger(pos, TileEntityStructureTrigger(state, data))
	}
	
	protected fun placeDecoration(world: IStructureWorld, pos: BlockPos, picker: IBlockPicker?){
		val rand = world.rand
		val pick = (picker ?: Air).pick(rand)
		
		world.setState(pos, pick)
		
		if (pick.block === ModBlocks.DARK_CHEST){
			world.addTrigger(pos, LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
		}
	}
}
