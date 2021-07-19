package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.game.block.properties.BlockModel
import net.minecraft.util.math.AxisAlignedBB

open class BlockPortalFrame(builder: BlockBuilder) : BlockSimpleShaped(builder, AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.8125, 1.0)) {
	override val model
		get() = BlockModel.PortalFrame(this, "plain")
}
