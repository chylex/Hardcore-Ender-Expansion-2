package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.Block

open class BlockSimple(builder: BlockBuilder) : Block(builder.p), IHeeBlock
