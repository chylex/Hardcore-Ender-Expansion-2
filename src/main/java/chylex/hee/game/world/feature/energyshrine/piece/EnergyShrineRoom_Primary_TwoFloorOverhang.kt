package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineRoom_Primary_TwoFloorOverhang(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(2, 0, maxZ), SOUTH),
		EnergyShrineConnection(ROOM, Pos(maxX, maxY - 5, maxZ - 2), EAST),
		EnergyShrineConnection(ROOM, Pos(0, maxY - 5, maxZ - 1), WEST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		placeWallBanner(world, Pos(maxX - 3, maxY - 5, maxZ - 4), NORTH)
		placeWallBanner(world, Pos(maxX - 7, maxY - 5, maxZ - 4), NORTH)
		
		val rand = world.rand
		
		world.setState(Pos(3, 2, 5), ModBlocks.DARK_CHEST.withFacing(EAST))
		world.addTrigger(Pos(3, 2, 5), LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
}
