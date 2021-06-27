package chylex.hee.game.block

import chylex.hee.system.migration.BlockStairs
import chylex.hee.system.migration.supply
import net.minecraft.block.Block

open class BlockStairsCustom(block: Block) : BlockStairs(supply(block.defaultState), Properties.from(block))
