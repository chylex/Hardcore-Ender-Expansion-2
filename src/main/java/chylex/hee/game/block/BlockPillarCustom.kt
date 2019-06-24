package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.game.block.info.BlockBuilder.Companion.setupBlockProperties
import net.minecraft.block.BlockRotatedPillar

class BlockPillarCustom(builder: BlockBuilder) : BlockRotatedPillar(builder.material, builder.color){
	init{
		setupBlockProperties(builder, replaceMaterialAndColor = false)
	}
}
