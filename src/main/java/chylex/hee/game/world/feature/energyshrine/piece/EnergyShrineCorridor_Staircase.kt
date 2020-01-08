package chylex.hee.game.world.feature.energyshrine.piece
import chylex.hee.game.world.feature.energyshrine.EnergyShrinePieces
import chylex.hee.game.world.structure.IStructurePieceFromFile
import chylex.hee.game.world.structure.IStructurePieceFromFile.Delegate
import chylex.hee.game.world.structure.IStructureWorld
import chylex.hee.game.world.util.PosXZ
import chylex.hee.init.ModBlocks
import chylex.hee.system.util.ceilToInt
import chylex.hee.system.util.facades.Facing6
import chylex.hee.system.util.nextFloat
import chylex.hee.system.util.nextInt
import chylex.hee.system.util.nextItemOrNull
import chylex.hee.system.util.toRadians
import chylex.hee.system.util.withFacing
import net.minecraft.util.math.BlockPos
import java.util.Random

abstract class EnergyShrineCorridor_Staircase(file: String) : EnergyShrineAbstractPiece(), IStructurePieceFromFile by Delegate("energyshrine/$file", EnergyShrinePieces.PALETTE){
	override fun generate(world: IStructureWorld, instance: Instance){
		generator.generate(world)
		
		val rand = world.rand
		
		val count = (maxX * 0.6F).ceilToInt()
		val attempts = 25
		
		for(index in 0..count){
			val progress = index.toFloat() / count
			
			for(attempt in 1..attempts){
				val angle = ((progress * 85.0) + rand.nextFloat(0.0, 5.0)).toRadians()
				val torchXZ = nextRandomXZ(rand, angle)
				
				if (world.getBlock(torchXZ.withY(maxY)) !== ModBlocks.GLOOMROCK_SMOOTH){
					continue
				}
				
				val yOffset = ((attempt.toFloat() / attempts) * (maxY - 6)).ceilToInt()
				val torchPos = torchXZ.withY(yOffset + rand.nextInt(2, 5))
				
				if ((0..3).all { world.isAir(torchPos.down(it)) }){
					val attachmentCandidates = Facing6.filter { canAttachTorchTo(world, torchPos.offset(it)) }
					val attachTo = rand.nextItemOrNull(attachmentCandidates)
					
					if (attachTo != null){
						world.setState(torchPos, ModBlocks.GLOOMTORCH.withFacing(attachTo.opposite))
						break
					}
				}
			}
		}
	}
	
	private fun canAttachTorchTo(world: IStructureWorld, pos: BlockPos): Boolean{
		return pos.x in 0..maxX && pos.y in 0..maxY && pos.z in 0..maxZ && world.getState(pos).isSolid // UPDATE probably replace isSolid now that isFullBlock is gone?
	}
	
	protected abstract fun nextRandomXZ(rand: Random, angle: Double): PosXZ
}
