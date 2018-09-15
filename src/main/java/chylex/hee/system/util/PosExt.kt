package chylex.hee.system.util
import net.minecraft.block.Block
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import kotlin.math.pow
import kotlin.math.sqrt

inline fun Pos(x: Int, y: Int, z: Int) = BlockPos(x, y, z)
inline fun Pos(x: Double, y: Double, z: Double) = BlockPos(x, y, z)
inline fun Pos(vector: Vec3d) = BlockPos(vector)
inline fun Pos(entity: Entity) = BlockPos(entity)
inline fun Pos(packed: Long) = BlockPos.fromLong(packed)

// Constants

const val FLAG_NOTIFY_NEIGHBORS = 1
const val FLAG_SYNC_CLIENT      = 2
const val FLAG_SKIP_RENDER      = 4
const val FLAG_RENDER_IMMEDIATE = 8

// Block getters

inline fun BlockPos.isAir(world: IBlockAccess): Boolean{
	return world.isAirBlock(this)
}

inline fun BlockPos.getBlock(world: IBlockAccess): Block{
	return world.getBlockState(this).block
}

inline fun BlockPos.getState(world: IBlockAccess): IBlockState{
	return world.getBlockState(this)
}

inline fun <reified T : TileEntity> BlockPos.getTile(world: IBlockAccess): T?{
	return world.getTileEntity(this) as? T
}

// Block setters

inline fun BlockPos.setAir(world: World){
	world.setBlockToAir(this)
}

inline fun BlockPos.setBlock(world: World, block: Block){
	world.setBlockState(this, block.defaultState)
}

inline fun BlockPos.setBlock(world: World, block: Block, flags: Int){
	world.setBlockState(this, block.defaultState, flags)
}

inline fun BlockPos.setState(world: World, state: IBlockState){
	world.setBlockState(this, state)
}

inline fun BlockPos.setState(world: World, state: IBlockState, flags: Int){
	world.setBlockState(this, state, flags)
}

inline fun BlockPos.breakBlock(world: World, drops: Boolean): Boolean{
	return world.destroyBlock(this, drops)
}

// Block properties

inline fun BlockPos.getFaceShape(world: World, side: EnumFacing): BlockFaceShape{
	return this.getState(world).getBlockFaceShape(world, this, side)
}

// Block predicates

/**
 * Offsets the block position in [offsetRange] distances a the direction of [facing] until [testPredicate] returns true.
 * Returns the first [BlockPos] for which [testPredicate] returned true, or null if [testPredicate] didn't return true for any blocks within the [offsetRange].
 */
inline fun BlockPos.offsetUntil(facing: EnumFacing, offsetRange: IntRange, testPredicate: (BlockPos) -> Boolean): BlockPos?{
	var testPos = this
	
	for(offset in offsetRange){
		testPos = testPos.offset(facing)
		
		if (testPredicate(testPos)){
			return testPos
		}
	}
	
	return null
}

// Distance calculations

fun BlockPos.distanceSqTo(x: Int, y: Int, z: Int): Double{
	return (x - this.x).toDouble().pow(2) + (y - this.y).toDouble().pow(2) + (z - this.z).toDouble().pow(2)
}

fun BlockPos.distanceSqTo(pos: BlockPos): Double{
	return distanceSqTo(pos.x, pos.y, pos.z)
}

fun BlockPos.distanceSqTo(entity: Entity): Double{
	return (entity.posX - (this.x + 0.5)).pow(2) + (entity.posY - (this.y + 0.5)).pow(2) + (entity.posZ - (this.z + 0.5)).pow(2)
}

inline fun BlockPos.distanceTo(x: Int, y: Int, z: Int): Double = sqrt(distanceSqTo(x, y, z))
inline fun BlockPos.distanceTo(pos: BlockPos): Double = sqrt(distanceSqTo(pos))
inline fun BlockPos.distanceTo(entity: Entity): Double = sqrt(distanceSqTo(entity))

// Distance utilities

inline fun <reified T : TileEntity> BlockPos.closestTickingTile(world: World, maxDistance: Double = Double.MAX_VALUE): T?{
	var closestTile: T? = null
	var closestDistSq = if (maxDistance == Double.MAX_VALUE) maxDistance else square(maxDistance)
	
	for(tile in world.tickableTileEntities){
		if (tile is T){
			val distSq = this.distanceSq(tile.pos)
			
			if (distSq < closestDistSq){
				closestTile = tile
				closestDistSq = distSq
			}
		}
	}
	
	return closestTile
}

// Entity checks

fun BlockPos.isAnyPlayerWithinRange(world: World, range: Double): Boolean{
	return world.isAnyPlayerWithinRangeAt(x + 0.5, y + 0.5, z + 0.5, range)
}

inline fun BlockPos.isAnyPlayerWithinRange(world: World, range: Int): Boolean = isAnyPlayerWithinRange(world, range.toDouble())
