@file:Suppress("NOTHING_TO_INLINE")

package chylex.hee.game.world
import chylex.hee.game.world.math.PosXZ
import chylex.hee.system.math.ceilToInt
import chylex.hee.system.math.square
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.MutableBlockPos
import com.google.common.base.Function
import com.google.common.collect.Iterables
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.fluid.IFluidState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockReader
import net.minecraft.world.IWorldReader
import net.minecraft.world.IWorldWriter
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.BlockFlags
import java.util.Stack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

inline fun Pos(x: Int, y: Int, z: Int) = BlockPos(x, y, z)
inline fun Pos(x: Double, y: Double, z: Double) = BlockPos(x, y, z)
inline fun Pos(vector: Vec3d) = BlockPos(vector)
inline fun Pos(entity: Entity) = BlockPos(entity)
inline fun Pos(packed: Long): BlockPos = BlockPos.fromLong(packed)

inline val BlockPos.xz
	get() = PosXZ(this)

operator fun BlockPos.component1() = x
operator fun BlockPos.component2() = y
operator fun BlockPos.component3() = z

operator fun ChunkPos.component1() = x
operator fun ChunkPos.component2() = z

val BlockPos.center
	get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)

// Constants

const val FLAG_NONE             = 0
const val FLAG_NOTIFY_NEIGHBORS = BlockFlags.NOTIFY_NEIGHBORS
const val FLAG_SYNC_CLIENT      = BlockFlags.BLOCK_UPDATE
const val FLAG_SKIP_RENDER      = BlockFlags.NO_RERENDER
const val FLAG_RENDER_IMMEDIATE = BlockFlags.RERENDER_MAIN_THREAD
const val FLAG_REPLACE_NO_DROPS = BlockFlags.NO_NEIGHBOR_DROPS

// Block getters

inline fun BlockPos.isAir(world: IWorldReader): Boolean{
	return world.isAirBlock(this)
}

inline fun BlockPos.getBlock(world: IBlockReader): Block{
	return world.getBlockState(this).block
}

inline fun BlockPos.getMaterial(world: IBlockReader): Material{
	return world.getBlockState(this).material
}

inline fun BlockPos.getState(world: IBlockReader): BlockState{
	return world.getBlockState(this)
}

inline fun <reified T : TileEntity> BlockPos.getTile(world: IBlockReader): T?{
	return world.getTileEntity(this) as? T
}

inline fun BlockPos.isLoaded(world: IWorldReader): Boolean{
	return world.isBlockLoaded(this)
}

inline fun BlockPos.getFluidState(world: IBlockReader): IFluidState{
	return world.getFluidState(this)
}

// Block setters

inline fun BlockPos.setAir(world: IWorldWriter): Boolean{
	return world.setBlockState(this, Blocks.AIR.defaultState, BlockFlags.DEFAULT)
}

inline fun BlockPos.setBlock(world: IWorldWriter, block: Block): Boolean{
	return world.setBlockState(this, block.defaultState, BlockFlags.DEFAULT)
}

inline fun BlockPos.setBlock(world: IWorldWriter, block: Block, flags: Int): Boolean{
	return world.setBlockState(this, block.defaultState, flags)
}

inline fun BlockPos.setState(world: IWorldWriter, state: BlockState): Boolean{
	return world.setBlockState(this, state, BlockFlags.DEFAULT)
}

inline fun BlockPos.setState(world: IWorldWriter, state: BlockState, flags: Int): Boolean{
	return world.setBlockState(this, state, flags)
}

inline fun BlockPos.removeBlock(world: IWorldWriter): Boolean{
	return world.removeBlock(this, false)
}

inline fun BlockPos.breakBlock(world: IWorldWriter, drops: Boolean): Boolean{
	return world.destroyBlock(this, drops)
}

// Block properties

fun BlockPos.blocksMovement(world: IBlockReader): Boolean{
	return this.getMaterial(world).blocksMovement()
}

fun BlockPos.getHardness(world: IBlockReader): Float{
	return this.getState(world).getBlockHardness(world, this)
}

fun BlockPos.isFullBlock(world: IBlockReader): Boolean{
	return this.getState(world).isOpaqueCube(world, this)
}

fun BlockPos.isTopSolid(world: IBlockReader): Boolean{
	return Block.doesSideFillSquare(this.getState(world).getCollisionShape(world, this), UP)
}

// Position predicates

/**
 * Offsets the block position in the direction of [facing] by distances defined by [offsetRange] until [testPredicate] returns true.
 * Returns the first [BlockPos] for which [testPredicate] returned true, or null if [testPredicate] didn't return true for any blocks within the [offsetRange].
 */
inline fun BlockPos.offsetUntil(facing: Direction, offsetRange: IntProgression, testPredicate: (BlockPos) -> Boolean): BlockPos?{
	for(offset in offsetRange){
		val testPos = this.offset(facing, offset)
		
		if (testPredicate(testPos)){
			return testPos
		}
	}
	
	return null
}

/**
 * Offsets the block position in the direction of [facing] by distances defined by [offsetRange] until [testPredicate] returns true.
 * Returns the last [BlockPos] for which [testPredicate] didn't return true, or null if [testPredicate] didn't return true for any blocks within the [offsetRange].
 */
inline fun BlockPos.offsetUntilExcept(facing: Direction, offsetRange: IntProgression, testPredicate: (BlockPos) -> Boolean): BlockPos?{
	return this.offsetUntil(facing, offsetRange, testPredicate)?.offset(facing.opposite, offsetRange.step)
}

/**
 * Offsets the block position in the direction of [facing] by distances defined by [offsetRange] until [testPredicate] returns false.
 * Returns the last [BlockPos] for which [testPredicate] returned true, or the original [BlockPos] if [testPredicate] returned false immediately.
 */
inline fun BlockPos.offsetWhile(facing: Direction, offsetRange: IntProgression, testPredicate: (BlockPos) -> Boolean): BlockPos{
	var last = this
	
	for(offset in offsetRange){
		val testPos = this.offset(facing, offset)
		
		if (testPredicate(testPos)){
			last = testPos
		}
		else{
			break
		}
	}
	
	return last
}

// Areas (Box)

private val makeImmutable = Function<BlockPos, BlockPos> { pos -> pos!!.toImmutable() }
private val castMutable = Function<BlockPos, MutableBlockPos> { pos -> pos as MutableBlockPos }

fun BlockPos.allInBox(otherBound: BlockPos): Iterable<BlockPos>{
	return Iterables.transform(BlockPos.getAllInBoxMutable(this, otherBound), makeImmutable)
}

fun BlockPos.allInBoxMutable(otherBound: BlockPos): Iterable<MutableBlockPos>{
	return Iterables.transform(BlockPos.getAllInBoxMutable(this, otherBound), castMutable)
}

fun BlockPos.allInCenteredBox(offsetX: Int, offsetY: Int, offsetZ: Int): Iterable<BlockPos>{
	return Iterables.transform(BlockPos.getAllInBoxMutable(this.x - offsetX, this.y - offsetY, this.z - offsetZ, this.x + offsetX, this.y + offsetY, this.z + offsetZ), makeImmutable)
}

fun BlockPos.allInCenteredBoxMutable(offsetX: Int, offsetY: Int, offsetZ: Int): Iterable<MutableBlockPos>{
	return Iterables.transform(BlockPos.getAllInBoxMutable(this.x - offsetX, this.y - offsetY, this.z - offsetZ, this.x + offsetX, this.y + offsetY, this.z + offsetZ), castMutable)
}

fun BlockPos.allInCenteredBox(offsetX: Double, offsetY: Double, offsetZ: Double): Iterable<BlockPos>{
	return this.allInCenteredBox(offsetX.ceilToInt(), offsetY.ceilToInt(), offsetZ.ceilToInt())
}

fun BlockPos.allInCenteredBoxMutable(offsetX: Double, offsetY: Double, offsetZ: Double): Iterable<MutableBlockPos>{
	return this.allInCenteredBoxMutable(offsetX.ceilToInt(), offsetY.ceilToInt(), offsetZ.ceilToInt())
}

// Areas (Sphere)

private fun <T : BlockPos> BlockPos.allInCenteredSphereInternal(radiusSq: Double, iterable: Iterable<T>): Iterable<T>{
	return Iterables.filter(iterable){ it!!.distanceSqTo(this) <= radiusSq }
}

private fun getSphereRadiusSq(radius: Int, avoidNipples: Boolean): Double{
	return if (avoidNipples)
		square(radius + 0.5)
	else
		square(radius.toDouble())
}

fun BlockPos.allInCenteredSphere(radius: Double): Iterable<BlockPos>{
	return this.allInCenteredSphereInternal(square(radius), this.allInCenteredBox(radius, radius, radius))
}

fun BlockPos.allInCenteredSphereMutable(radius: Double): Iterable<MutableBlockPos>{
	return this.allInCenteredSphereInternal(square(radius), this.allInCenteredBoxMutable(radius, radius, radius))
}

fun BlockPos.allInCenteredSphere(radius: Int, avoidNipples: Boolean = false): Iterable<BlockPos>{
	return this.allInCenteredSphereInternal(getSphereRadiusSq(radius, avoidNipples), this.allInCenteredBox(radius, radius, radius))
}

fun BlockPos.allInCenteredSphereMutable(radius: Int, avoidNipples: Boolean = false): Iterable<MutableBlockPos>{
	return this.allInCenteredSphereInternal(getSphereRadiusSq(radius, avoidNipples), this.allInCenteredBoxMutable(radius, radius, radius))
}

// Areas (Flood-Fill)

inline fun BlockPos.floodFill(facings: Iterable<Direction>, limit: Int = Int.MAX_VALUE, condition: (BlockPos) -> Boolean): List<BlockPos>{
	val found = mutableListOf<BlockPos>()
	
	val stack = Stack<BlockPos>().apply { push(this@floodFill) }
	val visited = mutableSetOf(this)
	
	while(stack.isNotEmpty()){
		val current = stack.pop()
		
		if (condition(current)){
			found.add(current)
			
			if (found.size >= limit){
				break
			}
			
			for(facing in facings){
				val offset = current.offset(facing)
				
				if (!visited.contains(offset)){
					stack.push(offset)
					visited.add(offset)
				}
			}
		}
	}
	
	return found
}

// Logic functions

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

inline fun <reified T : TileEntity> BlockPos.closestTickingTile(world: World, maxDistance: Double): T?{
	var closestTile: T? = null
	var closestDistSq = square(maxDistance)
	
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

inline fun <reified T : TileEntity> BlockPos.closestTickingTile(world: World, maxDistance: Double, predicate: (T) -> Boolean): T?{
	var closestTile: T? = null
	var closestDistSq = square(maxDistance)
	
	for(tile in world.tickableTileEntities){
		if (tile is T && predicate(tile)){
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
	return world.isPlayerWithin(x + 0.5, y + 0.5, z + 0.5, range)
}

inline fun BlockPos.isAnyPlayerWithinRange(world: World, range: Int) = isAnyPlayerWithinRange(world, range.toDouble())
