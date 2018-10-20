package chylex.hee.game.block
import chylex.hee.game.block.BlockSimple.Builder.Companion.setupBlockProperties
import net.minecraft.block.BlockRotatedPillar

class BlockPillarCustom(builder: BlockSimple.Builder) : BlockRotatedPillar(builder.material, builder.mapColor){
	init{
		setupBlockProperties(builder, replaceMaterialAndColor = false)
	}
}
