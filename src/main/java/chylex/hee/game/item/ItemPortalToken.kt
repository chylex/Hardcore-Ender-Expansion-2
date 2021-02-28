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
import chylex.hee.game.item.components.UseOnBlockComponent
import chylex.hee.game.mechanics.portal.DimensionTeleporter
import chylex.hee.game.mechanics.portal.EntityPortalContact
import chylex.hee.game.world.BlockEditor
import chylex.hee.game.world.territory.TerritoryInstance
import chylex.hee.game.world.territory.TerritoryType
import chylex.hee.game.world.territory.properties.TerritoryColors
import chylex.hee.game.world.territory.storage.TerritoryGlobalStorage
import chylex.hee.game.world.territory.storage.data.VoidData
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
import net.minecraft.entity.Entity
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.NonNullList
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraft.world.dimension.DimensionType

class ItemPortalToken(properties: Properties) : ItemWithComponents(properties), UseOnBlockComponent {
	companion object {
		private const val TYPE_TAG = "Type"
		private const val TERRITORY_TYPE_TAG = "Territory"
		private const val TERRITORY_INDEX_TAG = "Index"
		private const val IS_CORRUPTED_TAG = "IsCorrupted"
		
		const val MAX_STACK_SIZE = 16
	}
	
	enum class TokenType(val propertyValue: Float, private val translationKeySuffix: String) {
		NORMAL  (0F, "normal"),
		RARE    (1F, "rare"),
		SOLITARY(2F, "solitary");
		
		val genericTranslationKey
			get() = "item.hee.portal_token.$translationKeySuffix"
		
		val concreteTranslationKey
			get() = "item.hee.portal_token.$translationKeySuffix.concrete"
	}
	
	init {
		components.attach(this)
		
		addPropertyOverride(Resource.Custom("token_type")) { stack, _, _ ->
			getTokenType(stack).propertyValue + (if (stack.heeTagOrNull.hasKey(IS_CORRUPTED_TAG)) 0.5F else 0F)
		}
	}
	
	// Token construction
	
	fun forTerritory(type: TokenType, territory: TerritoryType) = ItemStack(this).also {
		with(it.heeTag) {
			putEnum(TYPE_TAG, type)
			putEnum(TERRITORY_TYPE_TAG, territory)
		}
	}
	
	// Token data
	
	fun getTokenType(stack: ItemStack): TokenType {
		return stack.heeTagOrNull?.getEnum<TokenType>(TYPE_TAG) ?: TokenType.NORMAL
	}
	
	fun getTerritoryType(stack: ItemStack): TerritoryType? {
		return stack.heeTagOrNull?.getEnum<TerritoryType>(TERRITORY_TYPE_TAG)
	}
	
	private fun getTerritoryIndex(stack: ItemStack): Int? {
		return stack.heeTagOrNull?.getIntegerOrNull(TERRITORY_INDEX_TAG)
	}
	
	private fun remapInstanceIndex(instance: TerritoryInstance, tokenType: TokenType, entity: Entity): TerritoryInstance? {
		if (tokenType != TokenType.SOLITARY) {
			return instance
		}
		
		return if (entity is EntityPlayer)
			TerritoryGlobalStorage.get().remapSolitaryIndex(instance, entity)
		else
			null
	}
	
	fun hasTerritoryInstance(stack: ItemStack): Boolean {
		return stack.heeTagOrNull.hasKey(TERRITORY_INDEX_TAG)
	}
	
	fun getOrCreateTerritoryInstance(stack: ItemStack, entity: Entity): TerritoryInstance? {
		val territory = getTerritoryType(stack) ?: return null
		val tokenType = getTokenType(stack)
		
		val index = getTerritoryIndex(stack) ?: TerritoryGlobalStorage.get().assignNewIndex(territory, tokenType).also { stack.heeTag.putInt(TERRITORY_INDEX_TAG, it) }
		val instance = TerritoryInstance(territory, index)
		
		return remapInstanceIndex(instance, tokenType, entity)
	}
	
	// Corruption
	
	override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, itemSlot: Int, isSelected: Boolean) {
		if (!world.isRemote) {
			updateCorruptedState(stack)
		}
	}
	
	fun updateCorruptedState(stack: ItemStack) {
		if (getTokenType(stack) != TokenType.RARE || stack.heeTagOrNull.hasKey(IS_CORRUPTED_TAG)) {
			return
		}
		
		val index = getTerritoryIndex(stack) ?: return
		val territory = getTerritoryType(stack) ?: return
		
		if (TerritoryGlobalStorage.get().forInstance(TerritoryInstance(territory, index))?.getComponent<VoidData>()?.isCorrupting == true) {
			stack.heeTag.putBoolean(IS_CORRUPTED_TAG, true)
		}
	}
	
	// Creative mode
	
	override fun onItemRightClick(world: World, player: EntityPlayer, hand: Hand): ActionResult<ItemStack> {
		val heldItem = player.getHeldItem(hand)
		val territory = getTerritoryType(heldItem)
		
		if (world.isRemote || !player.isCreative || territory == null) {
			return super.onItemRightClick(world, player, hand)
		}
		
		val tokenType = getTokenType(heldItem)
		
		val index = getTerritoryIndex(heldItem) ?: TerritoryGlobalStorage.get().assignNewIndex(territory, tokenType)
		val instance = remapInstanceIndex(TerritoryInstance(territory, index), tokenType, player)
		
		if (instance == null) {
			return ActionResult(FAIL, heldItem)
		}
		
		EntityPortalContact.shouldTeleport(player) // ignore the result but prevent immediately teleporting back
		val spawnInfo = instance.prepareSpawnPoint(player, clearanceRadius = 1)
		
		if (player.dimension === HEE.dim) {
			BlockVoidPortalInner.teleportEntity(player, spawnInfo)
		}
		else {
			DimensionTeleporter.changeDimension(player, DimensionType.THE_END, DimensionTeleporter.EndTerritoryPortal(spawnInfo))
		}
		
		return ActionResult(SUCCESS, heldItem)
	}
	
	override fun useOnBlock(world: World, pos: BlockPos, player: EntityPlayer, item: ItemStack, ctx: ItemUseContext): ActionResultType {
		if (!player.isCreative || player.isSneaking) {
			return PASS
		}
		
		if (world.isRemote) {
			return SUCCESS
		}
		
		val targetPos = pos.up()
		val territoryType = getTerritoryType(item)
		
		if (territoryType == null || ctx.face != UP || !BlockEditor.canEdit(targetPos, player, item) || world.selectExistingEntities.inBox<EntityTokenHolder>(AxisAlignedBB(targetPos)).any()) {
			return FAIL
		}
		
		world.addEntity(EntityTokenHolder(world, targetPos, getTokenType(item), territoryType))
		return SUCCESS
	}
	
	// Client
	
	override fun getDefaultTranslationKey(): String {
		return TokenType.NORMAL.genericTranslationKey
	}
	
	override fun getTranslationKey(stack: ItemStack): String {
		return getTokenType(stack).genericTranslationKey
	}
	
	override fun getDisplayName(stack: ItemStack): ITextComponent {
		return TranslationTextComponent(getTokenType(stack).concreteTranslationKey, TranslationTextComponent(getTerritoryType(stack)?.translationKey ?: TerritoryType.FALLBACK_TRANSLATION_KEY))
	}
	
	override fun fillItemGroup(tab: ItemGroup, items: NonNullList<ItemStack>) {
		if (isInGroup(tab)) {
			for(territory in TerritoryType.ALL) {
				if (!territory.isSpawn) {
					items.add(forTerritory(TokenType.NORMAL, territory))
				}
			}
		}
	}
	
	@Sided(Side.CLIENT)
	override fun addInformation(stack: ItemStack, world: World?, lines: MutableList<ITextComponent>, flags: ITooltipFlag) {
		val territory = getTerritoryType(stack)
		var insertEmptyLineAt = -1
		
		if (territory != null) {
			lines.add(TranslationTextComponent(territory.desc.difficulty.tooltipTranslationKey))
			insertEmptyLineAt = lines.size
		}
		
		if (flags.isAdvanced) {
			getTerritoryIndex(stack)?.let {
				lines.add(TranslationTextComponent("item.hee.portal_token.tooltip.advanced", it))
			}
		}
		
		if ((MC.currentScreen as? GuiPortalTokenStorage)?.canActivateToken(stack) == true) {
			lines.add(TranslationTextComponent("item.hee.portal_token.tooltip.activate"))
		}
		else if (MC.player?.let { it.isCreative && it.dimension === HEE.dim } == true) {
			lines.add(TranslationTextComponent("item.hee.portal_token.tooltip.creative.${if (hasTerritoryInstance(stack)) "teleport" else "generate"}"))
		}
		
		if (lines.size > insertEmptyLineAt) {
			lines.add(insertEmptyLineAt, StringTextComponent(""))
		}
		
		super.addInformation(stack, world, lines, flags)
	}
	
	@Sided(Side.CLIENT)
	object Color : IItemColor {
		private val WHITE = RGB(255u).i
		
		private fun getColors(stack: ItemStack): TerritoryColors? {
			return ModItems.PORTAL_TOKEN.getTerritoryType(stack)?.desc?.colors
		}
		
		override fun getColor(stack: ItemStack, tintIndex: Int) = when(tintIndex) {
			1    -> getColors(stack)?.tokenTop?.i ?: WHITE
			2    -> getColors(stack)?.tokenBottom?.i ?: WHITE
			else -> NO_TINT
		}
	}
}
