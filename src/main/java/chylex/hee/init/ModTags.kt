package chylex.hee.init

import chylex.hee.game.Resource
import net.minecraft.block.Block
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ITag.INamedTag

object ModTags {
	@JvmField val GLOOMROCK_PARTICLES = tag("gloomrock_particles")
	@JvmField val VOID_PORTAL_FRAME_CRAFTED = tag("void_portal_frame_crafted")
	
	private fun tag(name: String): INamedTag<Block> {
		return BlockTags.createOptional(Resource.Custom(name))
	}
}
