package chylex.hee.game.world.feature.tombdungeon.piece

import chylex.hee.game.block.withFacing
import chylex.hee.game.world.Pos
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnection
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.STAIR_BOTTOM
import chylex.hee.game.world.feature.tombdungeon.connection.TombDungeonConnectionType.STAIR_TOP
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.init.ModBlocks
import chylex.hee.system.migration.BlockStairs
import chylex.hee.system.migration.Facing.EAST
import chylex.hee.system.migration.Facing.NORTH
import chylex.hee.system.migration.Facing.SOUTH
import chylex.hee.system.migration.Facing.WEST
import chylex.hee.system.random.nextInt
import net.minecraft.state.properties.StairsShape
import net.minecraft.util.math.BlockPos
import java.util.Random

sealed class TombDungeonCorridor_Stairs(file: String) : TombDungeonAbstractPieceFromFile(file, isFancy = false) {
	override val sidePathAttachWeight = 0
	override val secretAttachWeight = 0
	
	protected fun messUp(world: IStructureWorld, rand: Random, pos: BlockPos) {
		if (rand.nextInt(11) <= 3) {
			val dir = when {
				pos.x == 1         -> EAST
				pos.x == maxX - 1  -> WEST
				rand.nextBoolean() -> EAST
				else               -> WEST
			}
			
			world.setState(pos, ModBlocks.DUSTY_STONE_BRICK_STAIRS.withFacing(dir).with(BlockStairs.SHAPE, if (dir == EAST) StairsShape.OUTER_LEFT else StairsShape.OUTER_RIGHT))
		}
		else if (rand.nextInt(9) <= 1) {
			world.setBlock(pos, ModBlocks.DUSTY_STONE_BRICK_SLAB)
		}
		else if (rand.nextInt(49) == 0) {
			world.setAir(pos)
		}
	}
	
	class Start(file: String) : TombDungeonCorridor_Stairs(file) {
		override val connections = arrayOf<IStructurePieceConnection>(
			TombDungeonConnection(STAIR_TOP, Pos(centerX, 2, 0), NORTH),
			TombDungeonConnection(STAIR_BOTTOM, Pos(centerX, 0, maxZ), SOUTH)
		)
		
		override fun generate(world: IStructureWorld, instance: Instance) {
			super.generate(world, instance)
			
			val rand = world.rand
			
			for(z in 0..maxZ) {
				messUp(world, rand, Pos(rand.nextInt(1, maxX - 1), 2 - z, z))
			}
		}
	}
	
	class Middle(file: String) : TombDungeonCorridor_Stairs(file) {
		override val connections = arrayOf<IStructurePieceConnection>(
			TombDungeonConnection(STAIR_TOP, Pos(centerX, 1, 0), NORTH),
			TombDungeonConnection(STAIR_BOTTOM, Pos(centerX, 0, maxZ), SOUTH)
		)
		
		override fun generate(world: IStructureWorld, instance: Instance) {
			super.generate(world, instance)
			
			val rand = world.rand
			messUp(world, rand, Pos(rand.nextInt(1, maxX - 1), 1, 0))
		}
	}
	
	class End(file: String) : TombDungeonCorridor_Stairs(file) {
		override val connections = arrayOf<IStructurePieceConnection>(
			TombDungeonConnection(STAIR_TOP, Pos(centerX, 0, 0), NORTH),
			TombDungeonConnection(STAIR_BOTTOM, Pos(centerX, 0, maxZ), SOUTH)
		)
	}
}
