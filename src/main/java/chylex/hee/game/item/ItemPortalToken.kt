package chylex.hee.game.item
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.init.ModItems
import chylex.hee.system.Resource
import chylex.hee.system.util.color.RGB
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.setEnum
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.minecraft.util.text.translation.I18n as I18nServer

class ItemPortalToken : Item(){
	private companion object{
		private const val TYPE_TAG = "Type"
		private const val TERRITORY_TYPE_TAG = "Territory"
		private const val TERRITORY_INDEX_TAG = "Index"
	}
	
	enum class TokenType(val propertyValue: Float, private val translationKeySuffix: String? = null){
		NORMAL  (0F),
		RARE    (1F, "rare"),
		SOLITARY(2F, "solitary");
		
		val translationKey
			get() = translationKeySuffix?.let { "item.hee.portal_token.tooltip.$it" }
	}
	
	init{
		maxStackSize = 1
		
		addPropertyOverride(Resource.Custom("token_type")){
			stack, _, _ -> getTokenType(stack).propertyValue
		}
	}
	
	// Token construction
	
	fun forTerritory(type: TokenType, territory: TerritoryType) = ItemStack(this).also {
		with(it.heeTag){
			setEnum(TYPE_TAG, type)
			setEnum(TERRITORY_TYPE_TAG, territory)
		}
	}
	
	fun assignTerritoryInstance(stack: ItemStack, instance: TerritoryInstance){
		with(stack.heeTag){
			setEnum(TERRITORY_TYPE_TAG, instance.territory)
			setInteger(TERRITORY_INDEX_TAG, instance.index)
		}
	}
	
	// Token data
	
	fun getTokenType(stack: ItemStack): TokenType{
		return stack.heeTagOrNull?.getEnum<TokenType>(TYPE_TAG) ?: NORMAL
	}
	
	fun getTerritoryType(stack: ItemStack): TerritoryType?{
		return stack.heeTagOrNull?.getEnum<TerritoryType>(TERRITORY_TYPE_TAG)
	}
	
	fun getTerritoryInstance(stack: ItemStack): TerritoryInstance?{
		val territory = getTerritoryType(stack) ?: return null
		val index = stack.heeTag.getIntegerOrNull(TERRITORY_INDEX_TAG) ?: return null
		
		return TerritoryInstance(territory, index)
	}
	
	// Client
	
	override fun getItemStackDisplayName(stack: ItemStack): String{
		return I18nServer.translateToLocalFormatted("item.hee.portal_token_concrete.name", I18nServer.translateToLocal(getTerritoryType(stack)?.translationKey ?: TerritoryType.FALLBACK_TRANSLATION_KEY))
	}
	
	@SideOnly(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		getTokenType(stack).translationKey?.let {
			lines.add(I18n.format(it))
		}
		
		super.addInformation(stack, world, lines, flags)
	}
	
	@SideOnly(Side.CLIENT)
	object Color : IItemColor{
		private const val NONE = -1
		private val WHITE = RGB(255, 255, 255).toInt()
		
		private fun getColors(stack: ItemStack): TerritoryColors?{
			return ModItems.PORTAL_TOKEN.getTerritoryType(stack)?.desc?.colors
		}
		
		override fun colorMultiplier(stack: ItemStack, tintIndex: Int): Int = when(tintIndex){
			1 -> getColors(stack)?.tokenBottom?.toInt() ?: WHITE
			2 -> getColors(stack)?.tokenTop?.toInt() ?: WHITE
			else -> NONE
		}
	}
}
