package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
import chylex.hee.game.world.structure.IBlockPicker
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import net.minecraft.util.math.BlockPos

abstract class EnergyShrineRoom_Generic(file: String, protected val cornerBlock: Block, private val bannerColors: BannerColors) : EnergyShrineAbstractPiece(), IStructurePieceFromFile by Delegate("energyshrine/$file", EnergyShrinePieces.PALETTE){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(centerX - 1, 0, 0), NORTH),
		EnergyShrineRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineRoomConnection(Pos(maxX, 0, centerZ - 1), EAST),
		EnergyShrineRoomConnection(Pos(0, 0, centerZ), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		world.placeCube(Pos(1, 0, 1), Pos(maxX - 1, 0, maxZ - 1), Single(ModBlocks.GLOOMROCK_SMOOTH))
		
		world.setBlock(Pos(1, 0, 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 0, 1), cornerBlock)
		world.setBlock(Pos(1, 0, maxZ - 1), cornerBlock)
		world.setBlock(Pos(maxX - 1, 0, maxZ - 1), cornerBlock)
		
		generator.generate(world)
	}
	
	protected fun placeWallBanner(world: IStructureWorld, pos: BlockPos, facing: EnumFacing){
		val state = Blocks.WALL_BANNER.withFacing(facing)
		val data = EnergyShrineBanners.generate(world.rand, bannerColors)
		
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
