package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.Block

open class HeeBlock(builder: BlockBuilder) : Block(builder.p), IHeeBlock
