package chylex.hee.game.block
import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.BlockFlowerPot
import net.minecraft.block.Block

open class BlockFlowerPotCustom(builder: BlockBuilder, flower: Block) : BlockFlowerPot(flower, builder.p)
