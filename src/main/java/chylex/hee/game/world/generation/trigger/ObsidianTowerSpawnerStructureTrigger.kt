package chylex.hee.game.world.generation.trigger

import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.world.generation.structure.IStructureTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.util.Transform
import chylex.hee.game.world.util.getTile
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Size
import chylex.hee.util.math.Size.Alignment.CENTER
import chylex.hee.util.math.Size.Alignment.MIN
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IServerWorld

class ObsidianTowerSpawnerStructureTrigger(spawner: TileEntitySpawnerObsidianTower, private val roomOffset: BlockPos, private val roomSize: Size) : IStructureTrigger {
	private val spawner = TileEntityStructureTrigger(ModBlocks.SPAWNER_OBSIDIAN_TOWERS, spawner)
	
	override fun setup(world: IStructureWorld, pos: BlockPos, transform: Transform) {
		spawner.setup(world, pos, transform)
	}
	
	override fun realize(world: IServerWorld, pos: BlockPos, transform: Transform) {
		spawner.realize(world, pos, transform)
		pos.getTile<TileEntitySpawnerObsidianTower>(world)?.offset = calculateTransformedOffset(transform)
	}
	
	private fun calculateTransformedOffset(transform: Transform): BlockPos {
		val floorCenter = transform(roomSize.getPos(CENTER, MIN, CENTER).up(), roomSize)
		return transform(roomOffset, roomSize).subtract(floorCenter)
	}
}
