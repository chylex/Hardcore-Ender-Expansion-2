package chylex.hee.game.item
import chylex.hee.client.gui.GuiPortalTokenStorage
import chylex.hee.client.render.util.NO_TINT
import chylex.hee.client.util.MC
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.storage.TerritoryGlobalStorage
import chylex.hee.game.world.util.BlockEditor
import chylex.hee.init.ModItems
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.migration.forge.Side
import chylex.hee.system.migration.forge.Sided
import chylex.hee.system.util.color.IntColor.Companion.RGB
import chylex.hee.system.util.facades.Resource
import chylex.hee.system.util.getEnum
import chylex.hee.system.util.getIntegerOrNull
import chylex.hee.system.util.hasKey
import chylex.hee.system.util.heeTag
import chylex.hee.system.util.heeTagOrNull
import chylex.hee.system.util.setEnum
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.util.text.translation.I18n as I18nServer

class ItemPortalToken : Item(){
	companion object{
		private const val TYPE_TAG = "Type"
		private const val TERRITORY_TYPE_TAG = "Territory"
		private const val TERRITORY_INDEX_TAG = "Index"
		
		const val MAX_STACK_SIZE = 16
	}
	
	enum class TokenType(val propertyValue: Float, private val translationKeySuffix: String? = null){
		NORMAL  (0F),
		RARE    (1F, "rare"),
		SOLITARY(2F, "solitary");
		
		val translationKey
			get() = translationKeySuffix?.let { "item.hee.portal_token.tooltip.$it" }
	}
	
	init{
		maxStackSize = MAX_STACK_SIZE
		
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
	
	// Token data
	
	fun getTokenType(stack: ItemStack): TokenType{
		return stack.heeTagOrNull?.getEnum<TokenType>(TYPE_TAG) ?: NORMAL
	}
	
	fun getTerritoryType(stack: ItemStack): TerritoryType?{
		return stack.heeTagOrNull?.getEnum<TerritoryType>(TERRITORY_TYPE_TAG)
	}
	
	fun hasTerritoryInstance(stack: ItemStack): Boolean{
		return stack.heeTagOrNull.hasKey(TERRITORY_INDEX_TAG)
	}
	
	fun getOrCreateTerritoryInstance(stack: ItemStack): TerritoryInstance?{
		val territory = getTerritoryType(stack) ?: return null
		
		val index = with(stack.heeTag){
			getIntegerOrNull(TERRITORY_INDEX_TAG) ?: TerritoryGlobalStorage.get().assignNewIndex(territory).also { setInteger(TERRITORY_INDEX_TAG, it) }
		}
		
		return TerritoryInstance(territory, index)
	}
	
	// Creative mode
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		val territory = getTerritoryType(heldItem)
		
		if (world.isRemote || !player.isCreative || player.dimension != 1 || territory == null){
			return super.onItemRightClick(world, player, hand)
		}
		
		val index = heldItem.heeTagOrNull?.getIntegerOrNull(TERRITORY_INDEX_TAG) ?: TerritoryGlobalStorage.get().assignNewIndex(territory)
		val instance = TerritoryInstance(territory, index)
		
		if (!EntityPortalContact.shouldTeleport(player)){
			return ActionResult(FAIL, heldItem)
		}
		
		BlockVoidPortalInner.teleportEntity(player, instance.prepareSpawnPoint(world, player, clearanceRadius = 1))
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun onItemUse(player: EntityPlayer, world: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult{
		if (!player.isCreative || player.isSneaking){
			return PASS
		}
		
		if (world.isRemote){
			return SUCCESS
		}
		
		val targetPos = pos.up()
		
		val heldItem = player.getHeldItem(hand)
		val territoryType = getTerritoryType(heldItem)
		
		if (territoryType == null || facing != UP || !BlockEditor.canEdit(targetPos, player, heldItem)){
			return FAIL
		}
		
		world.spawnEntity(EntityTokenHolder(world, targetPos, getTokenType(heldItem), territoryType))
		return SUCCESS
	}
	
	// Client
	
	override fun getItemStackDisplayName(stack: ItemStack): String{
		return I18nServer.translateToLocalFormatted("item.hee.portal_token_concrete.name", I18nServer.translateToLocal(getTerritoryType(stack)?.translationKey ?: TerritoryType.FALLBACK_TRANSLATION_KEY))
	}
	
	override fun getSubItems(tab: CreativeTabs, items: NonNullList<ItemStack>){
		if (isInCreativeTab(tab)){
			for(territory in TerritoryType.ALL){
				if (!territory.isSpawn){
					items.add(forTerritory(NORMAL, territory))
				}
			}
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<String>, flags: ITooltipFlag){
		if ((MC.currentScreen as? GuiPortalTokenStorage)?.canActivateToken(stack) == true){
			lines.add(I18n.format("item.hee.portal_token.tooltip.activate"))
		}
		else if (MC.player?.let { it.isCreative && it.dimension == 1 } == true){
			lines.add(I18n.format("item.hee.portal_token.tooltip.creative.${if (hasTerritoryInstance(stack)) "teleport" else "generate"}"))
		}
		
		getTokenType(stack).translationKey?.let {
			lines.add(I18n.format(it))
		}
		
		if (flags.isAdvanced){
			stack.heeTagOrNull?.getIntegerOrNull(TERRITORY_INDEX_TAG)?.let {
				lines.add("")
				lines.add(I18n.format("item.hee.portal_token.tooltip.advanced", it))
			}
		}
		
		super.addInformation(stack, world, lines, flags)
	}
	
	@Sided(Side.CLIENT)
	object Color : IItemColor{
		private val WHITE = RGB(255u).i
		
		private fun getColors(stack: ItemStack): TerritoryColors?{
			return ModItems.PORTAL_TOKEN.getTerritoryType(stack)?.desc?.colors
		}
		
		override fun colorMultiplier(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> getColors(stack)?.tokenTop?.i ?: WHITE
			2 -> getColors(stack)?.tokenBottom?.i ?: WHITE
			else -> NO_TINT
		}
	}
}
