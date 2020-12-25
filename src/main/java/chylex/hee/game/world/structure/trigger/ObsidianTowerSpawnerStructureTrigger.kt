package chylex.hee.game.world.structure.trigger

import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.world.getTile
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.math.Size.Alignment.CENTER
import chylex.hee.game.world.math.Size.Alignment.MIN
import chylex.hee.game.world.math.Transform
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.init.ModBlocks
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorld

class ObsidianTowerSpawnerStructureTrigger(spawner: TileEntitySpawnerObsidianTower, private val roomOffset: BlockPos, private val roomSize: Size) : IStructureTrigger {
	private val spawner = TileEntityStructureTrigger(ModBlocks.SPAWNER_OBSIDIAN_TOWERS, spawner)
	
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
		spawner.setup(world, pos, transform)
	}
	
	override fun realize(world: IWorld, pos: BlockPos, transform: Transform) {
		spawner.realize(world, pos, transform)
		pos.getTile<TileEntitySpawnerObsidianTower>(world)?.offset = calculateTransformedOffset(transform)
	}
	
	private fun calculateTransformedOffset(transform: Transform): BlockPos {
		val floorCenter = transform(roomSize.getPos(CENTER, MIN, CENTER).up(), roomSize)
		return transform(roomOffset, roomSize).subtract(floorCenter)
	}
}
