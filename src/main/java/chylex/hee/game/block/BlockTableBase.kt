package chylex.hee.game.block
import chylex.hee.game.block.info.BlockBuilder

class BlockTableBase(builder: BlockBuilder, tier: Int, firstTier: Int) : BlockAbstractTable(builder, tier, firstTier){
	override fun getTranslationKey(): String{
		return "block.hee.table_base"
	}
	
	// TODO
}
