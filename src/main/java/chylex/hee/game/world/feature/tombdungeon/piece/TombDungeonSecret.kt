package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.withFacing
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import java.util.Random

abstract class TombDungeonSecret(file: String) : TombDungeonAbstractPieceFromFile(file, isFancy = true){
	final override val sidePathAttachWeight = 0
	final override val secretAttachWeight = 0
	
	val entranceFacing
		get() = connections.single().facing
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		
		val rand = world.rand
		
		val crackedBricks = mutableSetOf<BlockPos>()
		var canUseStair = true
		
		for(attempt in 1..(14 + size.y)){
			val pos = pickRandomEntrancePoint(rand)
			
			when(rand.nextInt(0, 10)){
				in 0..7 -> {
					world.setBlock(pos, ModBlocks.DUSTY_STONE_CRACKED_BRICKS)
					crackedBricks.add(pos)
				}
				
				8 -> world.setBlock(pos, ModBlocks.DUSTY_STONE_CRACKED)
				9 -> world.setBlock(pos, ModBlocks.DUSTY_STONE_DAMAGED)
				
				10 -> if (canUseStair){
					world.setState(pos, ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(NORTH))
					canUseStair = false
				}
			}
		}
		
		if (crackedBricks.size < 3 && canUseStair){
			world.setState(pickRandomEntrancePoint(rand), ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(NORTH))
		}
		
		pickChestPosition(rand)?.let { (chestPos, chestFacing) -> placeChest(world, instance, chestPos, chestFacing, secret = true) }
	}
	
	protected abstract fun pickRandomEntrancePoint(rand: Random): BlockPos
	protected abstract fun pickChestPosition(rand: Random): Pair<BlockPos, Direction>?
}
