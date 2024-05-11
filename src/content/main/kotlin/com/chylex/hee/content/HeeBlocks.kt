package com.chylex.hee.content

import com.chylex.hee.content.block.GloomrockBlock
import com.chylex.hee.core.Constants
import net.neoforged.neoforge.registries.DeferredRegister

object HeeBlocks {
	private val REGISTRY = DeferredRegister.createBlocks(Constants.MOD_ID)
	
	val GLOOMROCK = GloomrockBlock.register(REGISTRY)
}
