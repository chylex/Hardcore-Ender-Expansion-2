package chylex.hee.init
import chylex.hee.system.migration.vanilla.EntityPlayer
import chylex.hee.system.util.Pos
import chylex.hee.system.util.getTile
import net.minecraft.tileentity.TileEntity

object ModGuiHandler{
	
	// Types
	
	enum class GuiType(
		// val createInterface: (player: EntityPlayer, Int, Int, Int) -> Any?,
		// val createContainer: (player: EntityPlayer, Int, Int, Int) -> Any?
	){
		AMULET_OF_RECOVERY(
			// createInterface = { player, hand, _, _ -> GuiAmuletOfRecovery(player, Hand.values()[hand]) },
			// createContainer = { player, hand, _, _ -> ContainerAmuletOfRecovery(player, Hand.values()[hand]) }
		),
		
		SHULKER_BOX(
			// createInterface = { player, slot, _, _ -> GuiShulkerBox(player.inventory, ItemShulkerBoxOverride.Inventory(player, slot)) },
			// createContainer = { player, slot, _, _ -> ContainerShulkerBoxInInventory(player, ItemShulkerBoxOverride.Inventory(player, slot)) }
		),
		
		TRINKET_POUCH(
			// createInterface = { player, slot, _, _ -> GuiTrinketPouch(player, slot) },
			// createContainer = { player, slot, _, _ -> ContainerTrinketPouch(player, slot) }
		),
		
		PORTAL_TOKEN_STORAGE(
			// createInterface = forTileEntity<TileEntityVoidPortalStorage> { player, tile -> GuiPortalTokenStorage(player, tile) },
			// createContainer = forTileEntity<TileEntityVoidPortalStorage> { player, tile -> ContainerPortalTokenStorage(player, tile) }
		),
		
		BREWING_STAND(
			// createInterface = forTileEntity<TileEntityBrewingStandCustom> { player, tile -> GuiBrewingStandCustom(player.inventory, tile) },
			// createContainer = forTileEntity<TileEntityBrewingStandCustom> { player, tile -> ContainerBrewingStandCustom(player.inventory, tile) }
		),
		
		LOOT_CHEST(
			// createInterface = forTileEntity<TileEntityLootChest> { player, tile -> GuiLootChest(player, tile) },
			// createContainer = forTileEntity<TileEntityLootChest> { player, tile -> ContainerLootChest(player, tile) }
		);
		
		fun open(player: EntityPlayer, x: Int = 0, y: Int = 0, z: Int = 0){
			// UPDATE player.openGui(HEE, ordinal, player.world, x, y, z)
		}
	}
	
	// Utilities
	
	private inline fun <reified T : TileEntity> forTileEntity(crossinline mapTo: (EntityPlayer, T) -> Any): (player: EntityPlayer, Int, Int, Int) -> Any?{
		return { player, x, y, z -> Pos(x, y, z).getTile<T>(player.world)?.let { mapTo(player, it) } }
	}
	
	// Overrides
	/* UPDATE
	override fun getClientGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) =
		GuiType.values().getOrNull(id)?.createInterface?.invoke(player, x, y, z)
	
	override fun getServerGuiElement(id: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int) =
		GuiType.values().getOrNull(id)?.createContainer?.invoke(player, x, y, z)*/
}
