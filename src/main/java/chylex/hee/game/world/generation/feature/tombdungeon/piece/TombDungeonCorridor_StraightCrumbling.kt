package chylex.hee.game.world.generation.feature.tombdungeon.piece

import chylex.hee.game.world.generation.IBlockPicker.Single
import chylex.hee.game.world.generation.IBlockPicker.Single.Air
import chylex.hee.game.world.generation.feature.tombdungeon.TombDungeonPieces
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.generation.feature.tombdungeon.connection.TombDungeonConnectionType.CORRIDOR
import chylex.hee.game.world.generation.structure.IStructureWorld
import chylex.hee.game.world.generation.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.random.nextInt
import chylex.hee.util.math.Pos
import chylex.hee.util.math.Size
import net.minecraft.util.Direction.NORTH
import net.minecraft.util.Direction.SOUTH

class TombDungeonCorridor_StraightCrumbling(length: Int, fallHeight: Int, override val isFancy: Boolean) : TombDungeonAbstractPiece() {
	override val size = Size(5, 6 + fallHeight, length)
	
	private val floorY
		get() = size.maxY - 4
	
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 2
	
	override val secretAttachY
		get() = floorY
	
	override val connections = arrayOf<IStructurePieceConnection>(
		TombDungeonConnection(CORRIDOR, Pos(size.centerX, floorY, size.maxZ), SOUTH),
		TombDungeonConnection(CORRIDOR, Pos(size.centerX, floorY, 0), NORTH)
	)
	
	init {
		require(length > 2) { "tomb dungeon crumbling corridor must be at least 3 blocks long" }
	}
	
	override fun generate(world: IStructureWorld, instance: Instance) {
		val rand = world.rand
		val maxX = size.maxX
		val maxY = size.maxY
		val maxZ = size.maxZ
		
		world.placeCubeHollow(Pos(0, 0, 0), Pos(maxX, floorY, maxZ), Single(ModBlocks.DUSTY_STONE))
		world.placeWalls(Pos(0, floorY + 1, 0), Pos(maxX, maxY, maxZ), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_WALL else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		world.placeCube(Pos(1, maxY, 1), Pos(maxX - 1, maxY, maxZ - 1), if (isFancy) TombDungeonPieces.PALETTE_ENTRY_FANCY_CEILING else TombDungeonPieces.PALETTE_ENTRY_PLAIN_WALL_CEILING)
		world.placeCube(Pos(1, 1, 1), Pos(maxX - 1, floorY - 1, maxZ - 1), Air)
		world.placeCube(Pos(1, floorY + 1, 1), Pos(maxX - 1, maxY - 1, maxZ - 1), Air)
		
		for (attempt in 1..(2 * (size.z - 2))) {
			val cobwebPos = Pos(
				rand.nextInt(1, maxX - 1),
				rand.nextInt(1, floorY - 1),
				rand.nextInt(1, maxZ - 1)
			)
			
			world.setBlock(cobwebPos, ModBlocks.ANCIENT_COBWEB)
		}
		
		placeConnections(world, instance)
	}
}
