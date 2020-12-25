package chylex.hee.game.block

import chylex.hee.game.block.properties.BlockBuilder
import chylex.hee.system.migration.BlockSkull
import chylex.hee.system.migration.BlockSkullWall

class BlockSkullCustom(type: ISkullType, builder: BlockBuilder) : BlockSkull(type, builder.p) {
	class Wall(type: ISkullType, builder: BlockBuilder) : BlockSkullWall(type, builder.p)
}
