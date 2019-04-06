package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineTerminalConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.LootChestStructureTrigger
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.allInBoxMutable
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.withFacing
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.math.BlockPos

class EnergyShrineRoom_Secondary_Storage(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineTerminalConnection(Pos(centerX, 0, maxZ), SOUTH),
		EnergyShrineTerminalConnection(Pos(centerX - 1, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val points = arrayOf(
			Pos(1, 2, 3),
			Pos(1, 2, maxZ - 4),
			Pos(maxX - 3, 2, 3),
			Pos(maxX - 3, 2, maxZ - 4)
		)
		
		for(pos in points){
			placeShelfDecorations(world, pos)
			placeShelfDecorations(world, pos.up(2))
		}
		
		val rand = world.rand
		val chestZ = rand.nextInt(0, 1)
		val chestPos = rand.nextItem(points).add(rand.nextInt(0, 2), if (rand.nextBoolean()) 0 else 2, chestZ)
		
		world.setState(chestPos, ModBlocks.DARK_CHEST.withFacing(if (chestZ == 0) NORTH else SOUTH))
		world.addTrigger(chestPos, LootChestStructureTrigger(EnergyShrinePieces.LOOT_PICK(rand), rand.nextLong()))
	}
	
	private fun placeShelfDecorations(world: IStructureWorld, pos: BlockPos){
		val rand = world.rand
		
		val block = when(rand.nextInt(100)){
			in  0..59 -> ModBlocks.GLOOMTORCH
			in 60..69 -> Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE
			in 70..79 -> Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE
			else -> return
		}
		
		pos.allInBoxMutable(pos.add(2, 0, 1)).forEach {
			if (rand.nextInt(4) != 0){
				world.setBlock(it, block)
			}
		}
	}
}