package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHardnessWithResistance
import chylex.hee.game.block.BlockSimple.Builder.Companion.setHarvestTool
import net.minecraft.block.BlockRotatedPillar

class BlockPillarCustom(builder: BlockSimple.Builder) : BlockRotatedPillar(builder.material, builder.mapColor){
	init{
		setHarvestTool(builder.harvestTool)
		setHardnessWithResistance(builder.harvestHardness, builder.explosionResistance)
		
		slipperiness = builder.slipperiness
		
		lightValue = builder.lightLevel
		builder.lightOpacity?.let { lightOpacity = it }
		
		soundType = builder.soundType
	}
}
