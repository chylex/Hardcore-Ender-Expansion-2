package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.util.STAIRS_HALF
import chylex.hee.game.block.util.STAIRS_SHAPE
import chylex.hee.game.block.util.withFacing
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.random.nextInt
import chylex.hee.util.random.nextRounded
import net.minecraft.state.properties.Half
import net.minecraft.state.properties.StairsShape
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Mirror
import java.util.Random

class TombDungeonRoom_Tomb_MultiSpacious(file: String, private val tombsPerColumn: Int, entranceY: Int, isFancy: Boolean) : TombDungeonRoom_Tomb(file, entranceY, allowExit = true, allowSecrets = false, isFancy) {
	override val sidePathAttachWeight = 7
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		val chests = rand.nextInt(1, rand.nextInt(2, (tombsPerColumn / 2))) * rand.nextRounded(0.86F)
		
		repeat(chests) {
			val (offset, facing) = if (rand.nextBoolean())
				-1 to EAST
			else
				1 to WEST
			
			if (rand.nextInt(5) == 0) {
				val pos = Pos(centerX + (3 * offset), 1, 3 + (3 * rand.nextInt(tombsPerColumn)))
				if (world.getBlock(pos) is BlockGraveDirt) {
					placeChest(world, instance, pos, facing)
					
					if (rand.nextInt(17) == 0) {
						world.setAir(pos.add(0, 1, 0))
						world.setAir(pos.add(-offset, 1, 0))
					}
				}
			}
			else {
				val pos = Pos(centerX + (4 * offset), 4, 3 + (3 * rand.nextInt(tombsPerColumn)))
				if (world.isAir(pos)) {
					placeChest(world, instance, pos, facing)
				}
			}
		}
		
		if (rand.nextInt(100) < 65) {
			placeJars(world, instance, (0 until tombsPerColumn).flatMap {
				listOf(
					Pos(centerX - 4, 4, 3 + (3 * it)),
					Pos(centerX + 4, 4, 3 + (3 * it))
				)
			})
		}
		
		if (isExitConnected(instance)) {
			for (x in 1 until maxX) for (y in 3..5) {
				world.setState(Pos(x, y, 0), world.getState(Pos(x, y, maxZ)).mirror(Mirror.LEFT_RIGHT))
			}
			
			world.setState(Pos(2, maxY - 1, 1), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(NORTH).with(STAIRS_HALF, Half.TOP))
			world.setState(Pos(3, maxY - 1, 1), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(WEST).with(STAIRS_HALF, Half.TOP).with(STAIRS_SHAPE, StairsShape.OUTER_RIGHT))
			world.setState(Pos(maxX - 2, maxY - 1, 1), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(NORTH).with(STAIRS_HALF, Half.TOP))
			world.setState(Pos(maxX - 3, maxY - 1, 1), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(EAST).with(STAIRS_HALF, Half.TOP).with(STAIRS_SHAPE, StairsShape.OUTER_LEFT))
		}
	}
	
	override fun getSpawnerTriggerMobAmount(rand: Random, level: TombDungeonLevel): MobAmount? {
		if (rand.nextInt(10) >= 6) {
			return null
		}
		
		return when {
			tombsPerColumn <= 4 -> if (rand.nextInt(3) == 0) MobAmount.LOW else MobAmount.MEDIUM
			tombsPerColumn <= 6 -> if (rand.nextBoolean()) MobAmount.MEDIUM else MobAmount.HIGH
			else                -> MobAmount.HIGH
		}
	}
}
