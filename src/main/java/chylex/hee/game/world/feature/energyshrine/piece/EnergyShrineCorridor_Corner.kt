package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnection
import chylex.hee.game.world.feature.energyshrine.connection.EnergyShrineConnectionType.CORRIDOR
import chylex.hee.game.world.structure.IBlockPicker.Single.Air
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.structure.piece.IStructurePieceConnection
import chylex.hee.game.world.util.Size
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.Pos
import chylex.hee.system.util.withFacing
import net.minecraft.util.EnumFacing.SOUTH
import net.minecraft.util.EnumFacing.UP
import net.minecraft.util.EnumFacing.WEST

class EnergyShrineCorridor_Corner(private val lit: Boolean) : EnergyShrineAbstractPiece(){
	override val size = Size(4, 6, 4)
	
	override val connections = arrayOf<IStructurePieceConnection>(
		EnergyShrineConnection(CORRIDOR, Pos(size.centerX, 0, size.maxZ), SOUTH),
		EnergyShrineConnection(CORRIDOR, Pos(0, 0, size.centerZ), WEST)
	)
	
	override val ceilingBlock
		get() = ModBlocks.GLOOMROCK_BRICKS
	
	override fun generate(world: IStructureWorld, instance: Instance){
		super.generate(world, instance)
		world.placeCube(Pos(1, 1, 1), Pos(size.maxX - 1, size.maxY - 2, size.maxZ - 1), Air)
		
		if (lit){
			world.setState(Pos(0, 2, size.maxZ), ModBlocks.GLOOMTORCH.withFacing(UP))
		}
	}
}
