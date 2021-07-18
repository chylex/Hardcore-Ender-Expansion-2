package chylex.hee.game.world.generation.feature.obsidiantower.piece

import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.system.random.removeItem
import chylex.hee.util.math.Pos
import chylex.hee.util.math.PosXZ
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import java.util.Random

class ObsidianTowerRoom_PreBoss_GloomrockPillars(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true) {
	override fun generateContents(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		
		val pillarOffsets = arrayOf(
			PosXZ(-4, -2),
			PosXZ(-4, +0),
			PosXZ(-4, +2),
			PosXZ(-2, -4),
			PosXZ(-2, +4)
		).flatMap {
			listOf(it, it.copy(x = -it.x))
		}.toMutableList()
		
		world.setBlock(nextPos(pillarOffsets, rand), Blocks.IRON_BLOCK)
		
		repeat(2) {
			world.setBlock(nextPos(pillarOffsets, rand), if (rand.nextBoolean()) Blocks.COAL_BLOCK else Blocks.LAPIS_BLOCK)
		}
		
		while (pillarOffsets.isNotEmpty()) {
			placeSpawner(world, nextPos(pillarOffsets, rand), instance)
		}
		
		world.addTrigger(Pos(centerX, 1, centerZ), EntityStructureTrigger({ realWorld -> EntityTokenHolder(realWorld, TokenType.NORMAL, TerritoryType.ENDER_CITY).apply { currentCharge = 0F } }, yOffset = 0.65))
	}
	
	private fun nextPos(list: MutableList<PosXZ>, rand: Random): BlockPos {
		return rand.removeItem(list).add(centerX, centerZ).withY(centerY)
	}
}
