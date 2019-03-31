package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IBlockPicker.Weighted.Companion.Weighted
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.collection.MutableWeightedList.Companion.mutableWeightedListOf
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineRoom_Primary_DisplayCase(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(3, 0, maxZ), SOUTH),
		EnergyShrineRoomConnection(Pos(2, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		val decorations = mutableWeightedListOf(
			75 to Single(ModBlocks.DARK_CHEST.withFacing(WEST)),
			
			25 to Single(ModBlocks.GLOOMTORCH),
			 5 to Single(ModBlocks.GLOOMTORCH),
			
			15 to Single(Blocks.FLOWER_POT), // TODO add death flower
			15 to Single(Blocks.FLOWER_POT),
			
			25 to Weighted(
				5 to Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultState,
				8 to Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE.defaultState,
				9 to Blocks.STONE_BUTTON.withFacing(UP)
			),
			
			15 to Air,
			 5 to Air
		)
		
		for(z in 4..(maxZ - 4)){
			placeDecoration(world, Pos(5, 2, z), decorations.removeItem(rand))
		}
		
		placeWallBanner(world, Pos(4, 3, 3), WEST)
		placeWallBanner(world, Pos(4, 3, maxZ - 3), WEST)
	}
}
