package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineRoomConnection
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.BedStructureTrigger
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import net.minecraft.block.Block
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST
import java.util.Random

class EnergyShrineRoom_Secondary_Dormitory(file: String, cornerBlock: Block, bannerColors: BannerColors) : EnergyShrineRoom_Generic(file, cornerBlock, bannerColors){
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineRoomConnection(Pos(3, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		for((index, color) in generateBedColors(rand).withIndex()){
			world.addTrigger(Pos(6 + index, 1, 5), BedStructureTrigger(SOUTH, color))
		}
		
		for((index, color) in generateBedColors(rand).withIndex()){
			world.addTrigger(Pos(6 + index, 3, 5), BedStructureTrigger(SOUTH, color))
		}
		
		placeWallBanner(world, Pos(4, 2, 1), WEST)
		placeWallBanner(world, Pos(4, 2, 4), WEST)
	}
	
	private fun generateBedColors(rand: Random): Array<EnumDyeColor>{
		val colors = Array(7){ EnumDyeColor.SILVER }
		
		val param1 = rand.nextInt(0, 1)
		val param2 = rand.nextInt(0, 1)
		
		repeat(rand.nextInt(1 + param1, 2 + param2)){
			val color = rand.nextItem<EnumDyeColor>()
			
			repeat(rand.nextInt(2 - param1, 3 - param2)){
				colors[rand.nextInt(colors.size)] = color
			}
		}
		
		return colors
	}
}
