package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_INSIDE
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_EXIT
import chylex.hee.game.world.generation.feature.tombdungeon.piece.TombDungeonRoom_Tomb.MobSpawnerTrigger
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH

class TombDungeonRoom_Tomb_Mass(width: Int, depth: Int, private val border: Boolean, private val split: Boolean, override val isFancy: Boolean) : TombDungeonAbstractPiece() {
	override val size = Size(width + 2, 6, depth + 2)
	
	override val sidePathAttachWeight = 6
	override val secretAttachWeight = 2
	override val secretAttachY = 1
	
	override val connections = if (border || split) arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(TOMB_ENTRANCE_INSIDE, Pos(size.centerX, secretAttachY, size.maxZ), SOUTH),
		TombDungeonConnection(TOMB_EXIT, Pos(size.centerX, secretAttachY, 0), NORTH)
	)
	else arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(TOMB_ENTRANCE_INSIDE, Pos(size.centerX, secretAttachY, size.maxZ), SOUTH)
	)
	
	init {
		require(width % 2 != 0) { "mass tomb width must not be even" }
	}
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		super.generate(world, instance)
		
		val rand = world.rand
		val level = instance.context
		
		val centerX = size.centerX
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		val distance = if (border) 2 else 1
		val palette = if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_GRAVE else TombDungeonPieces.PALETTE_ENTRY_PLAIN_GRAVE
		
		world.placeCube(Pos(1, 2, 1), Pos(maxX - 1, maxY - 1, maxZ - 1), Air)
		world.placeCube(Pos(distance, 1, distance), Pos(maxX - distance, 1, maxZ - distance), palette.thenSetting(BlockGraveDirt.FULL, false))
		
		if (border) {
			world.placeWalls(Pos(1, 1, 1), Pos(maxX - 1, 1, maxZ - 1), Single(ModBlocks.DUSTY_STONE))
		}
		
		if (split) {
			world.placeCube(Pos(centerX, 1, distance), Pos(centerX, 1, maxZ - distance), Single(ModBlocks.DUSTY_STONE))
		}
		else {
			world.setBlock(Pos(centerX, 1, maxZ - 1), ModBlocks.DUSTY_STONE)
		}
		
		if (rand.nextInt(6) == 0 && (border || split) && instance.findAvailableConnections().any { it.type === TOMB_EXIT }) {
			placeJars(world, instance, listOf(Pos(centerX, 2, 1)))
			
			if (level != null && rand.nextInt(9) != 0) {
				placeSpawnerTrigger(world, level)
			}
		}
		else if (level != null && rand.nextInt(3) != 0) {
			placeSpawnerTrigger(world, level)
		}
		
		placeCobwebs(world, instance)
	}
	
	private fun placeSpawnerTrigger(world: IStructureWorld, level: TombDungeonLevel) {
		val area = (size.x - 2) * (size.z - 2)
		val (undreads, spiderlings) = level.pickUndreadAndSpiderlingSpawns(world.rand, if (area <= 30) MobAmount.LOW else MobAmount.MEDIUM)
		MobSpawnerTrigger.place(world, entrance = connections.first().offset, width = size.maxX, depth = size.maxZ, undreads, spiderlings)
	}
}
