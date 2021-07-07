package chylex.hee.game.world.generation.feature.energyshrine

import chylex.hee.game.world.generation.feature.energyshrine.EnergyShrineBanners.BannerColors
import chylex.hee.init.ModBlocks
import net.minecraft.block.Block

data class EnergyShrineRoomData(val cornerBlock: Block, val bannerColors: BannerColors) {
	companion object {
		val DEFAULT = EnergyShrineRoomData(ModBlocks.GLOOMROCK_SMOOTH_WHITE, BannerColors.DEFAULT)
	}
}
