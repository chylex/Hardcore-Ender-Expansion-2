package chylex.hee.system.util
import chylex.hee.game.world.util.PosXZ
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.BlockFaceShape
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockPos.MutableBlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

inline fun Pos(x: Int, y: Int, z: Int) = BlockPos(x, y, z)
inline fun Pos(x: Double, y: Double, z: Double) = BlockPos(x, y, z)
inline fun Pos(vector: Vec3d) = BlockPos(vector)
inline fun Pos(entity: Entity) = BlockPos(entity)
inline fun Pos(packed: Long) = BlockPos.fromLong(packed)

inline val BlockPos.xz
	get() = PosXZ(this)

operator fun BlockPos.component1() = x
operator fun BlockPos.component2() = y
operator fun BlockPos.component3() = z

operator fun ChunkPos.component1() = x
operator fun ChunkPos.component2() = z

inline val BlockPos.center
	get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)

// Constants

const val FLAG_NONE             = 0
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

inline fun BlockPos.getMaterial(world: IBlockAccess): Material{
	return world.getBlockState(this).material
}

inline fun BlockPos.getState(world: IBlockAccess): IBlockState{
	return world.getBlockState(this)
}

inline fun <reified T : TileEntity> BlockPos.getTile(world: IBlockAccess): T?{
	return world.getTileEntity(this) as? T
}

inline fun BlockPos.isLoaded(world: World): Boolean{
	return world.isBlockLoaded(this)
}

// Block setters

inline fun BlockPos.setAir(world: World): Boolean{
	return world.setBlockToAir(this)
}

inline fun BlockPos.setBlock(world: World, block: Block): Boolean{
	return world.setBlockState(this, block.defaultState)
}

inline fun BlockPos.setBlock(world: World, block: Block, flags: Int): Boolean{
	return world.setBlockState(this, block.defaultState, flags)
}

inline fun BlockPos.setState(world: World, state: IBlockState): Boolean{
	return world.setBlockState(this, state)
}

inline fun BlockPos.setState(world: World, state: IBlockState, flags: Int): Boolean{
	return world.setBlockState(this, state, flags)
}

inline fun BlockPos.updateState(world: World, expectedBlock: Block, stateMapper: (IBlockState) -> IBlockState): Boolean{
	val currentState = this.getState(world)
	return currentState.block === expectedBlock && this.setState(world, stateMapper(currentState))
}

inline fun BlockPos.updateState(world: World, expectedBlock: Block, flags: Int, stateMapper: (IBlockState) -> IBlockState): Boolean{
	val currentState = this.getState(world)
	return currentState.block === expectedBlock && this.setState(world, stateMapper(currentState), flags)
}

inline fun BlockPos.breakBlock(world: World, drops: Boolean): Boolean{
	return world.destroyBlock(this, drops)
}

// Block properties

inline fun BlockPos.blocksMovement(world: IBlockAccess): Boolean{
	return this.getState(world).material.blocksMovement()
}

inline fun BlockPos.isReplaceable(world: IBlockAccess): Boolean{
	return this.getBlock(world).isReplaceable(world, this)
}

inline fun BlockPos.getHardness(world: World): Float{
	return this.getState(world).getBlockHardness(world, this)
}

inline fun BlockPos.getFaceShape(world: World, side: EnumFacing): BlockFaceShape{
	return this.getState(world).getBlockFaceShape(world, this, side)
}

// Block predicates

/**
 * Offsets the block position in [offsetRange] distances a the direction of [facing] until [testPredicate] returns true.
 * Returns the first [BlockPos] for which [testPredicate] returned true, or null if [testPredicate] didn't return true for any blocks within the [offsetRange].
 */
inline fun BlockPos.offsetUntil(facing: EnumFacing, offsetRange: IntRange, testPredicate: (BlockPos) -> Boolean): BlockPos?{
	for(offset in offsetRange){
		val testPos = this.offset(facing, offset)
		
		if (testPredicate(testPos)){
			return testPos
		}
	}
	
	return null
}

// Areas

fun BlockPos.allInBox(otherBound: BlockPos): Iterable<BlockPos>{
	return BlockPos.getAllInBox(this, otherBound)
}

fun BlockPos.allInCenteredBox(offsetX: Int, offsetY: Int, offsetZ: Int): Iterable<BlockPos>{
	return BlockPos.getAllInBox(this.x - offsetX, this.y - offsetY, this.z - offsetZ, this.x + offsetX, this.y + offsetY, this.z + offsetZ)
}

fun BlockPos.allInBoxMutable(otherBound: BlockPos): Iterable<MutableBlockPos>{
	return BlockPos.getAllInBoxMutable(this, otherBound)
}

fun BlockPos.allInCenteredBoxMutable(offsetX: Int, offsetY: Int, offsetZ: Int): Iterable<MutableBlockPos>{
	return BlockPos.getAllInBoxMutable(this.x - offsetX, this.y - offsetY, this.z - offsetZ, this.x + offsetX, this.y + offsetY, this.z + offsetZ)
}

fun BlockPos.min(other: BlockPos): BlockPos{
	return Pos(min(x, other.x), min(y, other.y), min(z, other.z))
}

fun BlockPos.max(other: BlockPos): BlockPos{
	return Pos(max(x, other.x), max(y, other.y), max(z, other.z))
}

// Distance calculations

fun BlockPos.distanceSqTo(x: Int, y: Int, z: Int): Double{
	return (square(x - this.x) + square(y - this.y) + square(z - this.z)).toDouble()
}

fun BlockPos.distanceSqTo(pos: BlockPos): Double{
	return distanceSqTo(pos.x, pos.y, pos.z)
}

fun BlockPos.distanceSqTo(vec: Vec3d): Double{
	return square(vec.x - (this.x + 0.5)) + square(vec.y - (this.y + 0.5)) + square(vec.z - (this.z + 0.5))
}

fun BlockPos.distanceSqTo(entity: Entity): Double{
	return square(entity.posX - (this.x + 0.5)) + square(entity.posY - (this.y + 0.5)) + square(entity.posZ - (this.z + 0.5))
}

inline fun BlockPos.distanceTo(x: Int, y: Int, z: Int) = sqrt(distanceSqTo(x, y, z))
inline fun BlockPos.distanceTo(pos: BlockPos) = sqrt(distanceSqTo(pos))
inline fun BlockPos.distanceTo(vec: Vec3d) = sqrt(distanceSqTo(vec))
inline fun BlockPos.distanceTo(entity: Entity) = sqrt(distanceSqTo(entity))

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

inline fun BlockPos.isAnyPlayerWithinRange(world: World, range: Int) = isAnyPlayerWithinRange(world, range.toDouble())
