package com.chylex.hee

import com.chylex.hee.content.Test
import com.chylex.hee.core.Constants
import com.chylex.hee.core.HeeBlock
import net.neoforged.fml.common.Mod

@Mod(Constants.MOD_ID)
object HardcoreEnderExpansion {
	init {
		Test.test()
		HeeBlock()
	}
}
