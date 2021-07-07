package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import net.minecraft.block.SkullBlock
import net.minecraft.block.WallSkullBlock

class BlockSkullCustom(type: ISkullType, builder: BlockBuilder) : SkullBlock(type, builder.p) {
	class Wall(type: ISkullType, builder: BlockBuilder) : WallSkullBlock(type, builder.p)
}
