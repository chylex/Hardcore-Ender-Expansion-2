package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
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

class EnergyShrineRoom_Primary_TwoFloorSecret(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineRoomConnection(Pos(maxX, 0, 1), EAST),
		EnergyShrineRoomConnection(Pos(maxX, maxY - 5, maxZ - 2), EAST)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.setState(Pos(2, 7, 7), ModBlocks.DARK_CHEST.withFacing(WEST))
		world.addTrigger(Pos(2, 7, 7), LootChestStructureTrigger(EnergyShrinePieces.LOOT_BUILDING_MATERIALS, rand.nextLong()))
		
		world.setState(Pos(maxX - 1, 7, 5), ModBlocks.DARK_CHEST.withFacing(NORTH))
		world.addTrigger(Pos(maxX - 1, 7, 5), LootChestStructureTrigger(EnergyShrinePieces.LOOT_BUILDING_MATERIALS, rand.nextLong()))
		
		world.setState(Pos(maxX - 2, 7, 5), ModBlocks.DARK_CHEST.withFacing(NORTH))
		world.addTrigger(Pos(maxX - 2, 7, 5), LootChestStructureTrigger(EnergyShrinePieces.LOOT_BUILDING_MATERIALS, rand.nextLong()))
	}
}
