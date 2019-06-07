package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.CORRIDOR
import chylex.hee.game.world.structure.IBlockPicker.Single
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.util.EnumFacing.EAST
import net.minecraft.util.EnumFacing.NORTH
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineCorridor_StraightLit(length: Int) : EnergyShrineAbstractPiece(){
	override val size = Size(6, 6, length)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH),
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX - 1, 0, 0), NORTH)
	)
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.placeCube(Pos(2, 1, 1), Pos(size.maxX - 2, size.maxY - 2, size.maxZ - 1), Air)
		
		world.placeCube(Pos(1, 1, 1), Pos(1, size.maxY - 2, size.maxZ - 1), Single(ModBlocks.GLOOMROCK_BRICKS))
		world.placeCube(Pos(size.maxX - 1, 1, 1), Pos(size.maxX - 1, size.maxY - 2, size.maxZ - 1), Single(ModBlocks.GLOOMROCK_BRICKS))
		
		world.setState(Pos(1, 2, size.centerZ), ModBlocks.GLOOMTORCH.withFacing(EAST))
		world.setState(Pos(size.maxX - 1, 2, size.centerZ), ModBlocks.GLOOMTORCH.withFacing(WEST))
	}
}
