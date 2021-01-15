package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.block.BlockGraveDirt
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.TombDungeonLevel.MobAmount
import chylex.hee.game.world.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.TOMB_ENTRANCE_INSIDE
import chylex.hee.game.world.feature.tombdungeon.piece.TombDungeonRoom_Tomb.UndreadSpawnerTrigger
import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.math.Size
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.Facing.SOUTH

class TombDungeonRoom_Tomb_Mass(width: Int, depth: Int, private val border: Boolean, private val split: Boolean, override val isFancy: Boolean) : TombDungeonAbstractPiece() {
	override val size = Size(width + 2, 6, depth + 2)
	
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 2
	override val secretAttachY = 1
	
	override val connections = arrayOf<IStructurePieceConnection>(
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
		
		if (rand.nextInt(3) != 0 && level != null) {
			val area = (size.x - 2) * (size.z - 2)
			val (undreads, spiderlings) = level.pickUndreadAndSpiderlingSpawns(rand, if (area <= 30) MobAmount.LOW else MobAmount.MEDIUM)
			UndreadSpawnerTrigger.place(world, entrance = connections.first().offset, width = maxX, depth = maxZ, undreads, spiderlings)
		}
		
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
		
		if (rand.nextInt(6) == 0 && (border || split)) {
			placeJars(world, instance, listOf(Pos(centerX, 2, 1)))
		}
		
		placeCobwebs(world, instance)
	}
}
