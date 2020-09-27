package chylex.hee.game.block
import chylex.hee.system.migration.BlockStairs
import net.minecraft.block.Block

class BlockStairsCustom(block: Block) : BlockStairs(block.defaultState, Properties.from(block))
