package chylex.hee.game.world.feature.tombdungeon.piece
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.ROOM_ENTRANCE
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.structure.trigger.EntityStructureTrigger
import chylex.hee.game.world.structure.trigger.TileEntityStructureTrigger
import chylex.hee.game.world.territory.TerritoryType.OBSIDIAN_TOWERS
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.util.Pos
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.allInCenteredBox
import chylex.hee.system.util.with
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import kotlin.math.abs

class TombDungeonRoom_End(file: String) : TombDungeonRoom(file, isFancy = true){
	override val secretAttachWeight = 0
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(ROOM_ENTRANCE, Pos(centerX, 6, maxZ), SOUTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		world.placeCube(Pos(1, 2, 1), Pos(maxX - 1, maxY - 1, maxZ - 1), Air)
		super.generate(world, instance)
		
		val portalCenter = Pos(centerX, 1, centerZ - 12)
		val tombOffset = portalCenter.up(2)
		
		for(pos in portalCenter.allInCenteredBox(1, 0, 1)){
			world.addTrigger(pos, TileEntityStructureTrigger(ModBlocks.VOID_PORTAL_INNER.with(BlockVoidPortalInner.TYPE, BlockVoidPortalInner.Type.RETURN_INACTIVE), TagCompound()))
		}
		
		val tombs = arrayOf(
			Tomb(offsetX1 = -10, offsetZ1 = -2, offsetX2 = -9, offsetZ2 =  2),
			Tomb(offsetX1 =   9, offsetZ1 = -2, offsetX2 = 10, offsetZ2 =  2),
			Tomb(offsetX1 =   3, offsetZ1 = -9, offsetX2 =  7, offsetZ2 = -8),
			Tomb(offsetX1 =  -7, offsetZ1 = -9, offsetX2 = -3, offsetZ2 = -8)
		)
		
		placeTokenHolders(world, tombs, tombOffset)
		placeChests(world, tombs, tombOffset)
	}
	
	private fun placeTokenHolders(world: IStructureWorld, tombs: Array<Tomb>, tombOffset: BlockPos){
		val rand = world.rand
		val tokenTombs = tombs.asList().shuffled(rand).take(2)
		
		for(tokenTomb in tokenTombs){
			val xOffset = (tokenTomb.offsetX1 + tokenTomb.offsetX2) * 0.5
			val zOffset = (tokenTomb.offsetZ1 + tokenTomb.offsetZ2) * 0.5
			
			val nudge = if (tokenTomb.isShortX) EAST else SOUTH
			val trigger = EntityStructureTrigger({ realWorld -> EntityTokenHolder(realWorld, NORMAL, OBSIDIAN_TOWERS) }, nudgeFacing = nudge, nudgeAmount = 0.5, yOffset = 0.35)
			
			world.addTrigger(tombOffset.add(xOffset, 0.0, zOffset), trigger)
		}
	}
	
	private fun placeChests(world: IStructureWorld, tombs: Array<Tomb>, tombOffset: BlockPos){
		val rand = world.rand
		val chestTombs = tombs.flatMap { listOf(it to false, it to true) }.shuffled(rand).take(4)
		
		for((chestTomb, offsetType) in chestTombs){
			val x: IntArray
			val z: IntArray
			val facing: Direction
			
			if (chestTomb.isShortX){
				x = intArrayOf(chestTomb.offsetX1, chestTomb.offsetX2)
				z = (if (offsetType) chestTomb.offsetZ1 else chestTomb.offsetZ2).let { intArrayOf(it, it) }
				facing = if (offsetType) SOUTH else NORTH
			}
			else{
				z = intArrayOf(chestTomb.offsetZ1, chestTomb.offsetZ2)
				x = (if (offsetType) chestTomb.offsetX1 else chestTomb.offsetX2).let { intArrayOf(it, it) }
				facing = if (offsetType) EAST else WEST
			}
			
			val picks = when(rand.nextInt(6)){
				0 -> intArrayOf(0)
				1 -> intArrayOf(1)
				else -> intArrayOf(0, 1)
			}
			
			for(pick in picks){
				placeChest(world, tombOffset.add(x[pick], 0, z[pick]), facing)
			}
		}
	}
	
	private class Tomb(val offsetX1: Int, val offsetZ1: Int, val offsetX2: Int, val offsetZ2: Int){
		val isShortX = abs(offsetX1 - offsetX2) < abs(offsetZ1 - offsetZ2)
	}
}
