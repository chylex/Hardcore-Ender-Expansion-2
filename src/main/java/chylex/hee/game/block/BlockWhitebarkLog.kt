package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder
import chylex.hee.system.migration.vanilla.BlockLog

class BlockWhitebarkLog(builder: BlockBuilder) : BlockLog(builder.color /* UPDATE could use different color for vertical */, builder.p)
