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

class EnergyShrineRoom_Primary_TwoFloorFork(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineRoomConnection(Pos(0, 0, 3), WEST),
		EnergyShrineRoomConnection(Pos(1, 5, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		world.setState(Pos(1, 6, maxZ - 1), ModBlocks.DARK_CHEST.withFacing(EAST))
		world.addTrigger(Pos(1, 6, maxZ - 1), LootChestStructureTrigger(EnergyShrinePieces.LOOT_GENERAL, rand.nextLong()))
		
		world.setState(Pos(1, 6, maxZ - 2), ModBlocks.DARK_CHEST.withFacing(EAST))
		world.addTrigger(Pos(1, 6, maxZ - 2), LootChestStructureTrigger(EnergyShrinePieces.LOOT_GENERAL, rand.nextLong()))
	}
}
