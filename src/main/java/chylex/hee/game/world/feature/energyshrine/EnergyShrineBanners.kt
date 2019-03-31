package chylex.hee.game.world.feature.energyshrine
import chylex.hee.system.util.NBTList.Companion.setList
import chylex.hee.system.util.NBTObjectList
import chylex.hee.system.util.nextItem
import net.minecraft.item.EnumDyeColor
import net.minecraft.item.EnumDyeColor.BLACK
import net.minecraft.item.EnumDyeColor.BLUE
import net.minecraft.item.EnumDyeColor.BROWN
import net.minecraft.item.EnumDyeColor.CYAN
import net.minecraft.item.EnumDyeColor.GREEN
import net.minecraft.item.EnumDyeColor.LIGHT_BLUE
import net.minecraft.item.EnumDyeColor.LIME
import net.minecraft.item.EnumDyeColor.MAGENTA
import net.minecraft.item.EnumDyeColor.ORANGE
import net.minecraft.item.EnumDyeColor.PINK
import net.minecraft.item.EnumDyeColor.PURPLE
import net.minecraft.item.EnumDyeColor.RED
import net.minecraft.item.EnumDyeColor.SILVER
import net.minecraft.item.EnumDyeColor.WHITE
import net.minecraft.item.EnumDyeColor.YELLOW
import net.minecraft.nbt.NBTTagCompound
import java.util.Random

object EnergyShrineBanners{
	fun generate(rand: Random, colors: BannerColors): NBTTagCompound{
		val (baseColor, fadeColor) = colors.damageValues
		
		val patterns = NBTObjectList<NBTTagCompound>().apply {
			append(pattern("cs", BLACK.dyeDamage))
			append(pattern("bri", baseColor))
			
			// primary shapes
			
			when(rand.nextInt(10)){
				in 0..3 -> append(pattern("tl", baseColor))
				in 4..7 -> append(pattern("tr", baseColor))
				// 8..9
			}
			
			when(rand.nextInt(8)){
				in 0..2 -> append(pattern("bl", baseColor))
				in 3..5 -> append(pattern("br", baseColor))
				// 6..7
			}
			
			if (rand.nextInt(3) != 0){
				append(pattern("ms", baseColor))
			}
			
			if (rand.nextInt(3) == 0){
				append(pattern("bo", baseColor))
			}
			
			// special shapes
			
			if (rand.nextInt(6) == 0){
				append(pattern("sku", baseColor))
			}
			
			if (rand.nextInt(5) == 0){
				append(pattern("moj", baseColor))
			}
			
			if (rand.nextInt(4) == 0){
				append(pattern("flo", baseColor))
			}
			
			// fade gradient
			
			append(pattern("gru", fadeColor))
		}
		
		return NBTTagCompound().apply {
			setInteger("Base", baseColor)
			setList("Patterns", patterns)
		}
	}
	
	private fun pattern(name: String, color: Int) = NBTTagCompound().apply {
		setString("Pattern", name)
		setInteger("Color", color)
	}
	
	class BannerColors(
		private val base: EnumDyeColor,
		private val fade: EnumDyeColor
	){
		val damageValues
			get() = Pair(base.dyeDamage, fade.dyeDamage)
		
		companion object{
			val DEFAULT = BannerColors(WHITE, WHITE)
			
			private val BASE_COLORS = arrayOf(WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, SILVER)
			private val FADE_COLORS = arrayOf(WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK)
			
			fun random(rand: Random): BannerColors{
				val base = rand.nextItem(BASE_COLORS)
				
				val remainingFadeColors = FADE_COLORS.toMutableList().apply {
					remove(base)
					
					if (base == WHITE || base == SILVER){
						remove(WHITE)
						remove(BLACK)
					}
				}
				
				return BannerColors(base, rand.nextItem(remainingFadeColors))
			}
		}
	}
}
