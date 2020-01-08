package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.BlockSkull
import chylex.hee.system.migration.vanilla.BlockSkullWall

class BlockSkullCustom(type: ISkullType, builder: BlockBuilder) : BlockSkull(type, builder.p){
	class Wall(type: ISkullType, builder: BlockBuilder) : BlockSkullWall(type, builder.p)
}
