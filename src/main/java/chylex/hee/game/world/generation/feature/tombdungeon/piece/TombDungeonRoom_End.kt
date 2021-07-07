package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.block.util.with
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.technical.EntityTechnicalTrigger
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.ITriggerHandler
import chylex.hee.game.entity.technical.EntityTechnicalTrigger.Types.TOMB_DUNGEON_END
import chylex.hee.game.entity.util.posVec
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.territory.TerritoryType.OBSIDIAN_TOWERS
import chylex.hee.game.territory.storage.ForgottenTombsEndData
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.generation.trigger.EntityStructureTrigger
import chylex.hee.game.world.generation.trigger.TileEntityStructureTrigger
import chylex.hee.game.world.util.Facing4
import chylex.hee.game.world.util.allInBoxMutable
import chylex.hee.game.world.util.allInCenteredBox
import chylex.hee.game.world.util.floodFill
import chylex.hee.game.world.util.getBlock
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextInt
import chylex.hee.util.math.BoundingBox
import chylex.hee.util.math.Pos
import chylex.hee.util.math.addY
import chylex.hee.util.nbt.TagCompound
import net.minecraft.util.Direction
import net.minecraft.util.Direction.Axis
import net.minecraft.util.Direction.EAST
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH
import net.minecraft.util.Direction.WEST
import net.minecraft.util.Rotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.Random
import kotlin.math.abs

class TombDungeonRoom_End(file: String) : TombDungeonRoom(file, isFancy = true) {
	override val secretAttachWeight = 0
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, maxY - 8, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		world.placeCube(Pos(1, 2, 1), Pos(maxX - 1, maxY - 1, maxZ - 1), Air)
		super.generate(world, instance)
		
		val portalCenter = Pos(centerX, 1, 24)
		val tombOffset = portalCenter.up(2)
		
		for (pos in portalCenter.allInCenteredBox(1, 0, 1)) {
			world.addTrigger(pos, TileEntityStructureTrigger(ModBlocks.VOID_PORTAL_INNER.with(BlockVoidPortalInner.TYPE, BlockVoidPortalInner.Type.RETURN_INACTIVE), TagCompound()))
		}
		
		val tombs = arrayOf(
			Tomb(offsetX1 = -10, offsetX2 = -9, offsetZ1 = -2, offsetZ2 =  2),
			Tomb(offsetX1 =   9, offsetX2 = 10, offsetZ1 = -2, offsetZ2 =  2),
			Tomb(offsetX1 = -10, offsetX2 = -9, offsetZ1 = 10, offsetZ2 = 14),
			Tomb(offsetX1 =   9, offsetX2 = 10, offsetZ1 = 10, offsetZ2 = 14),
			Tomb(offsetX1 =  -2, offsetX2 =  2, offsetZ1 = 20, offsetZ2 = 21)
		)
		
		placeTokenHolders(world, tombs, tombOffset)
		placeChests(world, instance, tombs, tombOffset)
		placeHoles(world, tombs, tombOffset)
		
		world.addTrigger(Pos(centerX, 0, centerZ), EntityStructureTrigger(TOMB_DUNGEON_END))
	}
	
	override fun placeCobwebs(world: IStructureWorld, chancePerXZ: Float) {}
	
	private fun placeTokenHolders(world: IStructureWorld, tombs: Array<Tomb>, tombOffset: BlockPos) {
		val rand = world.rand
		val tokenTombs = tombs.asList().shuffled(rand).take(2)
		
		for (tokenTomb in tokenTombs) {
			val xOffset = (tokenTomb.offsetX1 + tokenTomb.offsetX2) * 0.5
			val zOffset = (tokenTomb.offsetZ1 + tokenTomb.offsetZ2) * 0.5
			
			val nudge = if (tokenTomb.isShortX) EAST else SOUTH
			val trigger = EntityStructureTrigger({ realWorld -> EntityTokenHolder(realWorld, NORMAL, OBSIDIAN_TOWERS) }, nudgeFacing = nudge, nudgeAmount = 0.5, yOffset = 0.35)
			
			world.addTrigger(tombOffset.add(xOffset, 0.0, zOffset), trigger)
		}
	}
	
	private fun placeChests(world: IStructureWorld, instance: Instance, tombs: Array<Tomb>, tombOffset: BlockPos) {
		val rand = world.rand
		val chestTombs = tombs.flatMap { listOf(it to false, it to true) }.shuffled(rand).take(4)
		
		for ((chestTomb, offsetType) in chestTombs) {
			val x: IntArray
			val z: IntArray
			val facing: Direction
			
			if (chestTomb.isShortX) {
				x = intArrayOf(chestTomb.offsetX1, chestTomb.offsetX2)
				z = (if (offsetType) chestTomb.offsetZ1 else chestTomb.offsetZ2).let { intArrayOf(it, it) }
				facing = if (offsetType) SOUTH else NORTH
			}
			else {
				z = intArrayOf(chestTomb.offsetZ1, chestTomb.offsetZ2)
				x = (if (offsetType) chestTomb.offsetX1 else chestTomb.offsetX2).let { intArrayOf(it, it) }
				facing = if (offsetType) EAST else WEST
			}
			
			val picks = when (rand.nextInt(6)) {
				0    -> intArrayOf(0)
				1    -> intArrayOf(1)
				else -> intArrayOf(0, 1)
			}
			
			for (pick in picks) {
				placeChest(world, instance, tombOffset.add(x[pick], 0, z[pick]), facing)
			}
		}
	}
	
	private fun placeHoles(world: IStructureWorld, tombs: Array<Tomb>, tombOffset: BlockPos) {
		val rand = world.rand
		val tombsWithHoles = tombs.flatMap { listOf(it, it) }.shuffled(rand).take(rand.nextInt(5, 6))
		
		for (tomb in tombsWithHoles) {
			if (tomb.isShortX) {
				world.setAir(tombOffset.add(
					if (rand.nextBoolean()) tomb.offsetX1 - 1 else tomb.offsetX2 + 1,
					rand.nextInt(0, 1),
					rand.nextInt(tomb.offsetZ1 + 1, tomb.offsetZ2 - 1)
				))
			}
			else {
				world.setAir(tombOffset.add(
					rand.nextInt(tomb.offsetX1 + 1, tomb.offsetX2 - 1),
					rand.nextInt(0, 1),
					if (rand.nextBoolean()) tomb.offsetZ1 - 1 else tomb.offsetZ2 + 1
				))
			}
		}
	}
	
	private class Tomb(val offsetX1: Int, val offsetX2: Int, val offsetZ1: Int, val offsetZ2: Int) {
		val isShortX = abs(offsetX1 - offsetX2) < abs(offsetZ1 - offsetZ2)
	}
	
	class Trigger : ITriggerHandler {
		override fun check(world: World): Boolean {
			return !world.isRemote
		}
		
		override fun update(entity: EntityTechnicalTrigger) {
			val instance = TerritoryInstance.fromPos(entity) ?: return
			val data = instance.getStorageComponent<ForgottenTombsEndData>() ?: return
			val world = entity.world
			
			val roomSize = TombDungeonPieces.PIECE_ROOM_END.size
			val roomAABB = roomSize
				.rotate(if (entity.horizontalFacing.axis == Axis.X) Rotation.CLOCKWISE_90 else Rotation.NONE)
				.toCenteredBoundingBox(entity.posVec.addY(roomSize.y * 0.5))
			
			val graveDirtAreas = mutableListOf<BoundingBox>()
			val graveDirtBlocks = mutableSetOf<BlockPos>()
			
			val roomMinPos = Pos(roomAABB.minX, roomAABB.minY, roomAABB.minZ)
			val roomMaxPos = Pos(roomAABB.maxX, roomAABB.maxY, roomAABB.maxZ)
			
			for (pos in roomMinPos.allInBoxMutable(roomMaxPos)) {
				if (!graveDirtBlocks.contains(pos) && pos.getBlock(world) is BlockGraveDirt) {
					val area = pos.floodFill(Facing4, limit = 25) { it.getBlock(world) is BlockGraveDirt }
					val minPos = area.minWithOrNull(compareBy(BlockPos::getX, BlockPos::getZ))
					val maxPos = area.maxWithOrNull(compareBy(BlockPos::getX, BlockPos::getZ))
					
					graveDirtAreas.add(BoundingBox(minPos!!, maxPos!!))
					graveDirtBlocks.addAll(area)
				}
			}
			
			data.setupRoom(roomAABB, graveDirtAreas)
			entity.remove()
		}
		
		override fun nextTimer(rand: Random): Int {
			return 10
		}
	}
}
