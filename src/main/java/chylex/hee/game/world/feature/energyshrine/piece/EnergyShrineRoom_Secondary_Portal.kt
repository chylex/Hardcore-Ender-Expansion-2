package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextItem
import net.minecraft.block.Block
import net.minecraft.block.BlockCarpet
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH

class EnergyShrineRoom_Secondary_Portal(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(centerX, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val carpet = Single(Blocks.CARPET.defaultState.withProperty(BlockCarpet.COLOR, world.rand.nextItem<EnumDyeColor>()))
		
		world.placeCube(Pos(3, 1, 2), Pos(3, 1, maxZ - 1), carpet)
		world.placeCube(Pos(maxX - 3, 1, 2), Pos(maxX - 3, 1, maxZ - 1), carpet)
		
		placeWallBanner(world, Pos(1, maxY - 2, maxZ - 2), NORTH)
		placeWallBanner(world, Pos(maxX - 1, maxY - 2, maxZ - 2), NORTH)
	}
}
