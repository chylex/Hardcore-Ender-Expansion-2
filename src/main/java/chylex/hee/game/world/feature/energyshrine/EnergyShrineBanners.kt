package chylex.hee.game.world.feature.energyshrine
import chylex.hee.system.util.TagCompound
import chylex.hee.system.util.nextItem
import net.minecraft.item.DyeColor
import net.minecraft.item.DyeColor.BLACK
import net.minecraft.item.DyeColor.BLUE
import net.minecraft.item.DyeColor.BROWN
import net.minecraft.item.DyeColor.CYAN
import net.minecraft.item.DyeColor.GREEN
import net.minecraft.item.DyeColor.LIGHT_BLUE
import net.minecraft.item.DyeColor.LIGHT_GRAY
import net.minecraft.item.DyeColor.LIME
import net.minecraft.item.DyeColor.MAGENTA
import net.minecraft.item.DyeColor.ORANGE
import net.minecraft.item.DyeColor.PINK
import net.minecraft.item.DyeColor.PURPLE
import net.minecraft.item.DyeColor.RED
import net.minecraft.item.DyeColor.WHITE
import net.minecraft.item.DyeColor.YELLOW
import net.minecraft.tileentity.BannerPattern
import net.minecraft.tileentity.BannerPattern.BORDER
import net.minecraft.tileentity.BannerPattern.BRICKS
import net.minecraft.tileentity.BannerPattern.FLOWER
import net.minecraft.tileentity.BannerPattern.GRADIENT_UP
import net.minecraft.tileentity.BannerPattern.MOJANG
import net.minecraft.tileentity.BannerPattern.SKULL
import net.minecraft.tileentity.BannerPattern.SQUARE_BOTTOM_LEFT
import net.minecraft.tileentity.BannerPattern.SQUARE_BOTTOM_RIGHT
import net.minecraft.tileentity.BannerPattern.SQUARE_TOP_LEFT
import net.minecraft.tileentity.BannerPattern.SQUARE_TOP_RIGHT
import net.minecraft.tileentity.BannerPattern.STRIPE_CENTER
import net.minecraft.tileentity.BannerPattern.STRIPE_MIDDLE
import java.util.Random

object EnergyShrineBanners{
	fun generate(rand: Random, colors: BannerColors): Pair<DyeColor, TagCompound>{
		val baseColor = colors.base
		val fadeColor = colors.fade
		
		val patterns = BannerPattern.Builder().apply {
			func_222477_a(STRIPE_CENTER, BLACK)
			func_222477_a(BRICKS, baseColor)
			
			// primary shapes
			
			when(rand.nextInt(10)){
				in 0..3 -> func_222477_a(SQUARE_TOP_LEFT, baseColor)
				in 4..7 -> func_222477_a(SQUARE_TOP_RIGHT, baseColor)
				// 8..9
			}
			
			when(rand.nextInt(8)){
				in 0..2 -> func_222477_a(SQUARE_BOTTOM_LEFT, baseColor)
				in 3..5 -> func_222477_a(SQUARE_BOTTOM_RIGHT, baseColor)
				// 6..7
			}
			
			if (rand.nextInt(3) != 0){
				func_222477_a(STRIPE_MIDDLE, baseColor)
			}
			
			if (rand.nextInt(3) == 0){
				func_222477_a(BORDER, baseColor)
			}
			
			// special shapes
			
			if (rand.nextInt(6) == 0){
				func_222477_a(SKULL, baseColor)
			}
			
			if (rand.nextInt(5) == 0){
				func_222477_a(MOJANG, baseColor)
			}
			
			if (rand.nextInt(4) == 0){
				func_222477_a(FLOWER, baseColor)
			}
			
			// fade gradient
			
			func_222477_a(GRADIENT_UP, fadeColor)
		}
		
		return baseColor to TagCompound().apply {
			put("Patterns", patterns.func_222476_a())
		}
	}
	
	class BannerColors(
		val base: DyeColor,
		val fade: DyeColor
	){
		companion object{
			val DEFAULT = BannerColors(WHITE, WHITE)
			
			private val BASE_COLORS = arrayOf(WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, LIGHT_GRAY)
			private val FADE_COLORS = arrayOf(WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK)
			
			fun random(rand: Random): BannerColors{
				val base = rand.nextItem(BASE_COLORS)
				
				val remainingFadeColors = FADE_COLORS.toMutableList().apply {
					remove(base)
					
					if (base == WHITE || base == LIGHT_GRAY){
						remove(WHITE)
						remove(BLACK)
					}
				}
				
				return BannerColors(base, rand.nextItem(remainingFadeColors))
			}
		}
	}
}
