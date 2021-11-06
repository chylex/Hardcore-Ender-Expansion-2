package chylex.hee.game.block.properties

import net.minecraft.block.Block

fun interface IBlockStateModelSupplier {
	fun generate(block: Block): IBlockStateModel
}
