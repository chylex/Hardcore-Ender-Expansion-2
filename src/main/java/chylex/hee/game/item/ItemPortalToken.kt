package chylex.hee.game.item

import chylex.hee.HEE
import chylex.hee.client.gui.screen.GuiPortalTokenStorage
import chylex.hee.client.text.LocalizationStrategy
import chylex.hee.client.util.MC
import chylex.hee.game.Resource
import chylex.hee.game.block.BlockVoidPortalInner
import chylex.hee.game.entity.item.EntityTokenHolder
import chylex.hee.game.entity.util.EntityPortalContact
import chylex.hee.game.entity.util.selectExistingEntities
import chylex.hee.game.item.builder.HeeItemBuilder
import chylex.hee.game.item.components.ICreativeTabComponent
import chylex.hee.game.item.components.IItemNameComponent
import chylex.hee.game.item.components.ITickInInventoryComponent
import chylex.hee.game.item.components.ITooltipComponent
import chylex.hee.game.item.components.IUseItemOnAirComponent
import chylex.hee.game.item.components.PlayerUseItemOnBlockComponent
import chylex.hee.game.item.properties.ItemModel
import chylex.hee.game.item.properties.ItemTint
import chylex.hee.game.item.util.ItemProperty
import chylex.hee.game.territory.TerritoryType
import chylex.hee.game.territory.storage.VoidData
import chylex.hee.game.territory.system.TerritoryInstance
import chylex.hee.game.territory.system.properties.TerritoryColors
import chylex.hee.game.territory.system.properties.TerritoryDifficulty
import chylex.hee.game.territory.system.storage.TerritoryGlobalStorage
import chylex.hee.game.world.isInEndDimension
import chylex.hee.game.world.server.DimensionTeleporter
import chylex.hee.init.ModItems
import chylex.hee.system.heeTag
import chylex.hee.system.heeTagOrNull
import chylex.hee.util.forge.Side
import chylex.hee.util.forge.Sided
import chylex.hee.util.nbt.getEnum
import chylex.hee.util.nbt.getIntegerOrNull
import chylex.hee.util.nbt.hasKey
import chylex.hee.util.nbt.putEnum
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUseContext
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResultType
import net.minecraft.util.ActionResultType.FAIL
import net.minecraft.util.ActionResultType.PASS
import net.minecraft.util.ActionResultType.SUCCESS
import net.minecraft.util.Direction.UP
import net.minecraft.util.Hand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World

object ItemPortalToken : HeeItemBuilder() {
	private const val TYPE_TAG = "Type"
	private const val TERRITORY_TYPE_TAG = "Territory"
	private const val TERRITORY_INDEX_TAG = "Index"
	private const val IS_CORRUPTED_TAG = "IsCorrupted"
	
	private const val LANG_TOOLTIP_ACTIVATE = "item.hee.portal_token.tooltip.activate"
	private const val LANG_TOOLTIP_TERRITORY_INDEX = "item.hee.portal_token.tooltip.advanced"
	private const val LANG_TOOLTIP_CREATIVE_GENERATE = "item.hee.portal_token.tooltip.creative.generate"
	private const val LANG_TOOLTIP_CREATIVE_TELEPORT = "item.hee.portal_token.tooltip.creative.teleport"
	
	private const val MAX_STACK_SIZE = 16
	
	val BLANK
		get() = HeeItemBuilder { maxStackSize = MAX_STACK_SIZE }
	
	private val TOKEN_TYPE_PROPERTY = ItemProperty(Resource.Custom("token_type")) { stack ->
		getTokenType(stack).propertyValue + (if (stack.heeTagOrNull.hasKey(IS_CORRUPTED_TAG)) 0.5F else 0F)
	}
	
	init {
		localization = LocalizationStrategy.None
		localizationExtra[TokenType.NORMAL.genericTranslationKey] = "Portal Token"
		localizationExtra[TokenType.NORMAL.concreteTranslationKey] = "Portal Token (%s)"
		localizationExtra[TokenType.RARE.genericTranslationKey] = "Rare Portal Token"
		localizationExtra[TokenType.RARE.concreteTranslationKey] = "Rare Portal Token (%s)"
		localizationExtra[TokenType.SOLITARY.genericTranslationKey] = "Solitary Portal Token"
		localizationExtra[TokenType.SOLITARY.concreteTranslationKey] = "Solitary Portal Token (%s)"
		localizationExtra[TerritoryDifficulty.PEACEFUL.tooltipTranslationKey] = "§7«§2 Peaceful §7»"
		localizationExtra[TerritoryDifficulty.NEUTRAL.tooltipTranslationKey] = "§7«§e Neutral §7»"
		localizationExtra[TerritoryDifficulty.HOSTILE.tooltipTranslationKey] = "§7«§4 Hostile §7»"
		localizationExtra[TerritoryDifficulty.BOSS.tooltipTranslationKey] = "§7«§5 Boss §7»"
		localizationExtra[LANG_TOOLTIP_ACTIVATE] = "§6Right-click to activate"
		localizationExtra[LANG_TOOLTIP_TERRITORY_INDEX] = "§7Index: %s"
		localizationExtra[LANG_TOOLTIP_CREATIVE_GENERATE] = "§6Hold and right-click to generate (creative)"
		localizationExtra[LANG_TOOLTIP_CREATIVE_TELEPORT] = "§6Hold and right-click to teleport (creative)"
		
		model = ItemModel.WithOverrides(
			ItemModel.Layers("portal_token_outline", "portal_token_color_top", "portal_token_color_bottom"),
			TOKEN_TYPE_PROPERTY to mapOf(
				1.0F to ItemModel.Suffixed("_rare", ItemModel.Layers(
					"portal_token_outline",
					"portal_token_color_top",
					"portal_token_color_bottom",
					"portal_token_border_rare"
				)),
				1.5F to ItemModel.Suffixed("_rare_corrupted", ItemModel.Layers(
					"portal_token_outline",
					"portal_token_color_top",
					"portal_token_color_bottom",
					"portal_token_border_rare",
					"portal_token_corruption"
				)),
				2.0F to ItemModel.Suffixed("_solitary", ItemModel.Layers(
					"portal_token_outline",
					"portal_token_color_top",
					"portal_token_color_bottom",
					"portal_token_border_solitary"
				)),
			)
		)
		
		tint = object : ItemTint() {
			private fun getColors(stack: ItemStack): TerritoryColors? {
				return getTerritoryType(stack)?.desc?.colors
			}
			
			@Sided(Side.CLIENT)
			override fun tint(stack: ItemStack, tintIndex: Int) = when (tintIndex) {
				1    -> getColors(stack)?.tokenTop?.i ?: WHITE
				2    -> getColors(stack)?.tokenBottom?.i ?: WHITE
				else -> NO_TINT
			}
		}
		
		properties.add(TOKEN_TYPE_PROPERTY)
		
		maxStackSize = MAX_STACK_SIZE
		
		components.name = object : IItemNameComponent {
			override val defaultTranslationKey
				get() = TokenType.NORMAL.genericTranslationKey
			
			override fun getTranslationKey(stack: ItemStack): String {
				return getTokenType(stack).genericTranslationKey
			}
			
			override fun getDisplayName(stack: ItemStack): ITextComponent {
				return TranslationTextComponent(getTokenType(stack).concreteTranslationKey, TranslationTextComponent(getTerritoryType(stack)?.translationKey ?: TerritoryType.FALLBACK_TRANSLATION_KEY))
			}
		}
		
		components.tooltip.add(ITooltipComponent { lines, stack, advanced, _ ->
			val territory = getTerritoryType(stack)
			var insertEmptyLineAt = -1
			
			if (territory != null) {
				lines.add(TranslationTextComponent(territory.desc.difficulty.tooltipTranslationKey))
				insertEmptyLineAt = lines.size
			}
			
			if (advanced) {
				getTerritoryIndex(stack)?.let {
					lines.add(TranslationTextComponent(LANG_TOOLTIP_TERRITORY_INDEX, it))
				}
			}
			
			if ((MC.currentScreen as? GuiPortalTokenStorage)?.canActivateToken(stack) == true) {
				lines.add(TranslationTextComponent(LANG_TOOLTIP_ACTIVATE))
			}
			else if (MC.player?.let { it.isCreative && it.isInEndDimension } == true) {
				lines.add(TranslationTextComponent(if (hasTerritoryInstance(stack)) LANG_TOOLTIP_CREATIVE_TELEPORT else LANG_TOOLTIP_CREATIVE_GENERATE))
			}
			
			if (lines.size > insertEmptyLineAt) {
				lines.add(insertEmptyLineAt, StringTextComponent(""))
			}
		})
		
		components.creativeTab = ICreativeTabComponent { menu, _ ->
			for (territory in TerritoryType.ALL) {
				if (!territory.isSpawn) {
					menu.add(forTerritory(TokenType.NORMAL, territory))
				}
			}
		}
		
		components.useOnBlock = object : PlayerUseItemOnBlockComponent() {
			override fun use(world: World, pos: BlockPos, player: PlayerEntity, heldItem: ItemStack, context: ItemUseContext): ActionResultType {
				if (!player.isCreative || player.isSneaking) {
					return PASS
				}
				
				if (world.isRemote) {
					return SUCCESS
				}
				
				val targetPos = context.pos.up()
				val territoryType = getTerritoryType(heldItem)
				
				if (territoryType == null || context.face != UP || world.selectExistingEntities.inBox<EntityTokenHolder>(AxisAlignedBB(targetPos)).any()) {
					return FAIL
				}
				
				world.addEntity(EntityTokenHolder(world, targetPos, getTokenType(heldItem), territoryType))
				return SUCCESS
			}
		}
		
		components.useOnAir = object : IUseItemOnAirComponent {
			override fun use(world: World, player: PlayerEntity, hand: Hand, heldItem: ItemStack): ActionResult<ItemStack> {
				if (world.isRemote || !player.isCreative) {
					return ActionResult.resultPass(heldItem)
				}
				
				if (!teleport(player, heldItem)) {
					return ActionResult.resultFail(heldItem)
				}
				
				return ActionResult.resultSuccess(heldItem)
			}
		}
		
		components.tickInInventory.add(ITickInInventoryComponent { world, _, stack, _, _ ->
			if (!world.isRemote) {
				updateCorruptedState(stack)
			}
		})
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
	
	fun getTokenType(stack: ItemStack): TokenType {
		return stack.heeTagOrNull?.getEnum<TokenType>(TYPE_TAG) ?: TokenType.NORMAL
	}
	
	fun getTerritoryType(stack: ItemStack): TerritoryType? {
		return stack.heeTagOrNull?.getEnum<TerritoryType>(TERRITORY_TYPE_TAG)
	}
	
	private fun getTerritoryIndex(stack: ItemStack): Int? {
		return stack.heeTagOrNull?.getIntegerOrNull(TERRITORY_INDEX_TAG)
	}
	
	// Token construction
	
	fun forTerritory(type: TokenType, territory: TerritoryType) = ItemStack(ModItems.PORTAL_TOKEN).also {
		with(it.heeTag) {
			putEnum(TYPE_TAG, type)
			putEnum(TERRITORY_TYPE_TAG, territory)
		}
	}
	
	// Token data
	
	private fun remapInstanceIndex(instance: TerritoryInstance, tokenType: TokenType, entity: Entity): TerritoryInstance? {
		if (tokenType != TokenType.SOLITARY) {
			return instance
		}
		
		return if (entity is PlayerEntity)
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
	
	fun updateCorruptedState(stack: ItemStack) {
		if (getTokenType(stack) != TokenType.RARE || stack.heeTagOrNull.hasKey(IS_CORRUPTED_TAG)) {
			return
		}
		
		val index = getTerritoryIndex(stack) ?: return
		val territory = getTerritoryType(stack) ?: return
		
		if (TerritoryInstance(territory, index).getStorageComponent<VoidData>()?.isCorrupting == true) {
			stack.heeTag.putBoolean(IS_CORRUPTED_TAG, true)
		}
	}
	
	// Teleportation
	
	private fun teleport(player: PlayerEntity, heldItem: ItemStack): Boolean {
		val territory = getTerritoryType(heldItem) ?: return false
		val tokenType = getTokenType(heldItem)
		val index = getTerritoryIndex(heldItem) ?: TerritoryGlobalStorage.get().assignNewIndex(territory, tokenType)
		
		val instance = remapInstanceIndex(TerritoryInstance(territory, index), tokenType, player)
		if (instance == null) {
			return false
		}
		
		EntityPortalContact.shouldTeleport(player) // ignore the result but prevent immediately teleporting back
		val spawnInfo = instance.prepareSpawnPoint(player, clearanceRadius = 1)
		
		if (player.isInEndDimension) {
			BlockVoidPortalInner.teleportEntity(player, spawnInfo)
		}
		else {
			DimensionTeleporter.changeDimension(player, HEE.dim, DimensionTeleporter.EndTerritoryPortal(spawnInfo))
		}
		
		return true
	}
}
