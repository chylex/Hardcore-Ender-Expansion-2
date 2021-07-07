package chylex.hee.game.block

import chylex.hee.util.forge.supply
import net.minecraft.block.Block
import net.minecraft.block.StairsBlock

open class BlockStairsCustom(block: Block) : StairsBlock(supply(block.defaultState), Properties.from(block))
