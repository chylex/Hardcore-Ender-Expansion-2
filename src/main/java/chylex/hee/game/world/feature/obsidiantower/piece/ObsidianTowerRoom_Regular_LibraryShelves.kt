package chylex.hee.game.world.feature.obsidiantower.piece
import chylex.hee.game.world.feature.obsidiantower.ObsidianTowerSpawnerLevel.LEVEL_1
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.PosXZ
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.migration.vanilla.Blocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItem
import chylex.hee.system.util.with
import net.minecraft.block.BlockSlab
import net.minecraft.block.BlockSlab.EnumBlockHalf.TOP

class ObsidianTowerRoom_Regular_LibraryShelves(file: String) : ObsidianTowerRoom_General(file, guaranteesSpawnersOnLevel1 = true){
	private companion object{
		private val BOOKSHELF_OFFSETS = arrayOf(
			PosXZ(-4, -2) to EAST,
			PosXZ(-4, -1) to EAST,
			PosXZ(-4,  0) to EAST,
			PosXZ(-4,  1) to EAST,
			PosXZ(-4,  2) to EAST,
			PosXZ(-2, -4) to SOUTH,
			PosXZ(-2,  4) to NORTH
		).flatMap {
			e -> listOf(e, e.first.let { it.copy(x = -it.x) } to e.second.let { if (it == EAST) WEST else it })
		}
	}
	
	override fun generateContents(world: IStructureWorld, instance: Instance){
		val rand = world.rand
		
		for(x in intArrayOf(1, maxX - 1)){
			val length = rand.nextInt(0, 5)
			val offset = rand.nextInt(0, 5 - length)
			
			val start = centerZ - 2 + offset
			
			for(z in start until (start + length)){
				world.setBlock(Pos(x, 2, z), Blocks.BOOKSHELF)
				world.setBlock(Pos(x, 3, z), ModBlocks.GLOOMROCK_SMOOTH_SLAB)
			}
			
			if (rand.nextInt(5) != 0){
				val exclusionZone = when{
					length > 0         -> (offset - 1) until (offset + length + 1)
					rand.nextBoolean() -> offset..offset
					else               -> IntRange.EMPTY
				}
				
				for(zOffset in (0 until 5).minus(exclusionZone)){
					val z = centerZ - 2 + zOffset
					
					world.setState(Pos(x, 3, z), ModBlocks.GLOOMROCK_SMOOTH_SLAB.with(BlockSlab.HALF, TOP))
					world.setBlock(Pos(x, 4, z), Blocks.BOOKSHELF)
					world.setBlock(Pos(x, 5, z), ModBlocks.GLOOMROCK_SMOOTH_SLAB)
				}
			}
		}
		
		for(corner in arrayOf(
			PosXZ(-2, -4),
			PosXZ(-2,  4),
			PosXZ( 2, -4),
			PosXZ( 2,  4)
		)){
			val pos = corner.add(centerX, centerZ)
			
			when(rand.nextInt(3)){
				0 -> {
					world.setBlock(pos.withY(2), Blocks.BOOKSHELF)
					world.setBlock(pos.withY(3), ModBlocks.GLOOMROCK_SMOOTH_SLAB)
				}
				
				1 -> {
					world.setState(pos.withY(3), ModBlocks.GLOOMROCK_SMOOTH_SLAB.with(BlockSlab.HALF, TOP))
					world.setBlock(pos.withY(4), Blocks.BOOKSHELF)
					world.setBlock(pos.withY(5), ModBlocks.GLOOMROCK_SMOOTH_SLAB)
				}
			}
		}
		
		if (rand.nextInt(7) != 0 || instance.context?.spawnerLevel == LEVEL_1){
			placeSpawner(world, rand.nextItem(BOOKSHELF_OFFSETS).first.add(centerX, centerZ).withY(1), instance)
		}
		
		if (rand.nextInt(4) == 0){
			val picks = BOOKSHELF_OFFSETS.shuffled(rand)
			val heights = intArrayOf(1, 4).apply { if (rand.nextBoolean()) reverse() }
			
			outer@for((pick, facing) in picks) for(height in heights){
				val pos = pick.add(centerX, centerZ).withY(height)
				
				if (world.getBlock(pos) === Blocks.BOOKSHELF && world.getBlock(pos.up()) !== Blocks.BOOKSHELF){
					placeFurnace(world, pos, facing)
					break@outer
				}
			}
		}
	}
}
