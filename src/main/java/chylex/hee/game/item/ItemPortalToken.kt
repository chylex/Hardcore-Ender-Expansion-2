package chylex.hee.game.item
import chylex.hee.HEE
import chylex.hee.client.MC
import chylex.hee.client.color.NO_TINT
import chylex.hee.client.gui.GuiPortalTokenStorage
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.selectExistingEntities
import chylex.hee.game.inventory.heeTag
import chylex.hee.game.inventory.heeTagOrNull
import chylex.hee.game.item.ItemPortalToken.TokenType.NORMAL
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.world.BlockEditor
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.storage.TerritoryGlobalStorage
import chylex.hee.init.ModItems
import chylex.hee.system.color.IntColor.Companion.RGB
import chylex.hee.system.facades.Resource
import chylex.hee.system.forge.Side
import chylex.hee.system.forge.Sided
import chylex.hee.system.migration.ActionResult.FAIL
import chylex.hee.system.migration.ActionResult.PASS
import chylex.hee.system.migration.ActionResult.SUCCESS
import chylex.hee.system.migration.EntityPlayer
import chylex.hee.system.migration.Facing.UP
import chylex.hee.system.serialization.getEnum
import chylex.hee.system.serialization.getIntegerOrNull
import chylex.hee.system.serialization.hasKey
import chylex.hee.system.serialization.putEnum
import net.minecraft.client.renderer.color.IItemColor
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

class ItemPortalToken(properties: Properties) : Item(properties){
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
		addPropertyOverride(Resource.Custom("token_type")){
			stack, _, _ -> getTokenType(stack).propertyValue
		}
	}
	
	// Token construction
	
	fun forTerritory(type: TokenType, territory: TerritoryType) = ItemStack(this).also {
		with(it.heeTag){
			putEnum(TYPE_TAG, type)
			putEnum(TERRITORY_TYPE_TAG, territory)
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
			getIntegerOrNull(TERRITORY_INDEX_TAG) ?: TerritoryGlobalStorage.get().assignNewIndex(territory, getTokenType(stack)).also { putInt(TERRITORY_INDEX_TAG, it) }
		}
		
		return TerritoryInstance(territory, index)
	}
	
	// Creative mode
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack>{
		val heldItem = player.getHeldItem(hand)
		val territory = getTerritoryType(heldItem)
		
		if (world.isRemote || !player.isCreative || player.dimension !== HEE.dim || territory == null){
			return super.onItemRightClick(world, player, hand)
		}
		
		val index = heldItem.heeTagOrNull?.getIntegerOrNull(TERRITORY_INDEX_TAG) ?: TerritoryGlobalStorage.get().assignNewIndex(territory, getTokenType(heldItem))
		val instance = TerritoryInstance(territory, index)
		
		if (!EntityPortalContact.shouldTeleport(player)){
			return ActionResult(FAIL, heldItem)
		}
		
		BlockVoidPortalInner.teleportEntity(player, instance.prepareSpawnPoint(player, clearanceRadius = 1))
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun onItemUse(context: ItemUseContext): ActionResultType{
		val player = context.player ?: return FAIL
		val world = context.world
		
		if (!player.isCreative || player.isSneaking){
			return PASS
		}
		
		if (world.isRemote){
			return SUCCESS
		}
		
		val targetPos = context.pos.up()
		
		val heldItem = player.getHeldItem(context.hand)
		val territoryType = getTerritoryType(heldItem)
		
		if (territoryType == null || context.face != UP || !BlockEditor.canEdit(targetPos, player, heldItem) || world.selectExistingEntities.inBox<EntityTokenHolder>(AxisAlignedBB(targetPos)).any()){
			return FAIL
		}
		
		world.addEntity(EntityTokenHolder(world, targetPos, getTokenType(heldItem), territoryType))
		return SUCCESS
	}
	
	// Client
	
	override fun getDisplayName(stack: ItemStack): ITextComponent{
		return TranslationTextComponent("item.hee.portal_token_concrete", TranslationTextComponent(getTerritoryType(stack)?.translationKey ?: TerritoryType.FALLBACK_TRANSLATION_KEY))
	}
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>){
		if (isInGroup(tab)){
			for(territory in TerritoryType.ALL){
				if (!territory.isSpawn){
					items.add(forTerritory(NORMAL, territory))
				}
			}
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag){
		if ((MC.currentScreen as? GuiPortalTokenStorage)?.canActivateToken(stack) == true){
			lines.add(TranslationTextComponent("item.hee.portal_token.tooltip.activate"))
		}
		else if (MC.player?.let { it.isCreative && it.dimension === HEE.dim } == true){
			lines.add(TranslationTextComponent("item.hee.portal_token.tooltip.creative.${if (hasTerritoryInstance(stack)) "teleport" else "generate"}"))
		}
		
		getTokenType(stack).translationKey?.let {
			lines.add(TranslationTextComponent(it))
		}
		
		if (flags.isAdvanced){
			stack.heeTagOrNull?.getIntegerOrNull(TERRITORY_INDEX_TAG)?.let {
				lines.add(StringTextComponent(""))
				lines.add(TranslationTextComponent("item.hee.portal_token.tooltip.advanced", it))
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
		
		override fun getColor(stack: ItemStack, tintIndex: Int) = when(tintIndex){
			1 -> getColors(stack)?.tokenTop?.i ?: WHITE
			2 -> getColors(stack)?.tokenBottom?.i ?: WHITE
			else -> NO_TINT
		}
	}
}
