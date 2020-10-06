package chylex.hee.game.block
import chylex.hee.client.render.block.IBlockLayerCutout
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.BlockFlowerPot
import chylex.hee.system.migration.supply
import net.minecraft.block.Block
import net.minecraft.block.Blocks

open class BlockFlowerPotCustom(builder: BlockBuilder, flower: Block) : BlockFlowerPot(supply(Blocks.FLOWER_POT as BlockFlowerPot), supply(flower), builder.p), IBlockLayerCutout{
	init{
		@Suppress("LeakingThis")
		(Blocks.FLOWER_POT as BlockFlowerPot).addPlant(flower.registryName!!, supply(this))
	}
}
