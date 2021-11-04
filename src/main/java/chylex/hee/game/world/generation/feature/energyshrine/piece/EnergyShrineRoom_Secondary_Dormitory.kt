package chylex.hee.game.world.generation.feature.energyshrine.piece

import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.generation.feature.energyshrine.connection.EnergyShrineConnectionType.ROOM
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.BedStructureTrigger
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextItem
import net.minecraft.item.DyeColor
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST
import java.util.Random

class EnergyShrineRoom_Secondary_Dormitory(file: String) : EnergyShrineRoom_Generic(file) {
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(ROOM, Pos(3, 0, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		val mirror = instance.transform.mirror
		
		for ((index, color) in generateBedColors(rand, mirror).withIndex()) {
			world.addTrigger(Pos(6 + index, 1, 5), BedStructureTrigger(SOUTH, color))
		}
		
		for ((index, color) in generateBedColors(rand, mirror).withIndex()) {
			world.addTrigger(Pos(6 + index, 3, 5), BedStructureTrigger(SOUTH, color))
		}
		
		placeWallBanner(world, instance, Pos(4, 2, 1), WEST)
		placeWallBanner(world, instance, Pos(4, 2, 4), WEST)
	}
	
	private fun generateBedColors(rand: Random, mirror: Boolean): Array<DyeColor> {
		if (rand.nextInt(1332) == 0) {
			return arrayOf(DyeColor.RED, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIME, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.PURPLE).apply {
				if (!mirror) {
					reverse()
				}
			}
		}
		
		val colors = Array(7) { DyeColor.LIGHT_GRAY }
		
		val param1 = rand.nextInt(0, 1)
		val param2 = rand.nextInt(0, 1)
		
		repeat(rand.nextInt(1 + param1, 2 + param2)) {
			val color = rand.nextItem<DyeColor>()
			
			repeat(rand.nextInt(2 - param1, 3 - param2)) {
				colors[rand.nextInt(colors.size)] = color
			}
		}
		
		return colors
	}
}
