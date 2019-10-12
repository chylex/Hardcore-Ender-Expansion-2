package chylex.hee.game.world.structure.trigger
import chylex.hee.game.block.entity.TileEntitySpawnerObsidianTower
import chylex.hee.game.world.structure.IStructureTrigger
import chylex.hee.game.world.util.Size
import chylex.hee.game.world.util.Size.Alignment.CENTER
import chylex.hee.game.world.util.Size.Alignment.MIN
import chylex.hee.game.world.util.Transform
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.getTile
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ObsidianTowerSpawnerStructureTrigger(private val spawner: TileEntitySpawnerObsidianTower, private val roomOffset: BlockPos, private val roomSize: Size) : IStructureTrigger{
	override fun realize(world: World, pos: BlockPos, transform: Transform){
		TileEntityStructureTrigger(ModBlocks.SPAWNER_OBSIDIAN_TOWERS, spawner).realize(world, pos, transform)
		
		val floorCenter = transform(roomSize.getPos(CENTER, MIN, CENTER).up(), roomSize)
		val transformedOffset = transform(roomOffset, roomSize).subtract(floorCenter)
		
		pos.getTile<TileEntitySpawnerObsidianTower>(world)?.offset = transformedOffset
	}
}
